package cn.cloudwalk.smartframework.clientcomponents.client.pool;

import cn.cloudwalk.smartframework.clientcomponents.core.*;
import cn.cloudwalk.smartframework.clientcomponents.core.entry.PoolEntry;
import cn.cloudwalk.smartframework.clientcomponents.core.util.Args;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public abstract class AbstractConnPool<T, C, E extends PoolEntry<T, C>>
        implements ConnectionPool<T, E>, ConnectionPoolControlAware<T> {

    private final Lock lock;
    private final ConnectionFactory<T, C> connFactory;
    private final Map<T, RouteSpecificPool<T, C, E>> routeToPool;
    private final Set<E> leased;
    private final LinkedList<E> available;
    private final LinkedList<PoolEntryFuture<E>> pending;
    private final Map<T, Integer> maxPerRoute;

    private volatile boolean isShutDown;
    private volatile int defaultMaxPerRoute;
    private volatile int maxTotal;
    private volatile int validateAfterInactivity;

    public AbstractConnPool(
            final ConnectionFactory<T, C> connFactory,
            final int defaultMaxPerRoute,
            final int maxTotal) {
        super();
        this.connFactory = Args.notNull(connFactory, "Connection factory");
        this.defaultMaxPerRoute = Args.positive(defaultMaxPerRoute, "Max per route value");
        this.maxTotal = Args.positive(maxTotal, "Max total value");
        this.lock = new ReentrantLock();
        this.routeToPool = new HashMap<T, RouteSpecificPool<T, C, E>>();
        this.leased = new HashSet<E>();
        this.available = new LinkedList<E>();
        this.pending = new LinkedList<PoolEntryFuture<E>>();
        this.maxPerRoute = new HashMap<T, Integer>();
    }


    protected abstract E createEntry(T route, C conn);


    protected void onLease(final E entry) {
    }


    protected void onRelease(final E entry) {
    }


    protected void onReuse(final E entry) {
    }

    protected boolean validate(final E entry) {
        return true;
    }

    public boolean isShutdown() {
        return this.isShutDown;
    }


    public void shutdown() throws IOException {
        if (this.isShutDown) {
            return;
        }
        this.isShutDown = true;
        this.lock.lock();
        try {
            for (final E entry : this.available) {
                entry.close();
            }
            for (final E entry : this.leased) {
                entry.close();
            }
            for (final RouteSpecificPool<T, C, E> pool : this.routeToPool.values()) {
                pool.shutdown();
            }
            this.routeToPool.clear();
            this.leased.clear();
            this.available.clear();
        } finally {
            this.lock.unlock();
        }
    }

    private RouteSpecificPool<T, C, E> getPool(final T route) {
        RouteSpecificPool<T, C, E> pool = this.routeToPool.get(route);
        if (pool == null) {
            pool = new RouteSpecificPool<T, C, E>(route) {

                @Override
                protected E createEntry(final C conn) {
                    return AbstractConnPool.this.createEntry(route, conn);
                }

            };
            this.routeToPool.put(route, pool);
        }
        return pool;
    }


    @Override
    public Future<E> lease(final T route, final Object state, final FutureCallback<E> callback) {
        Args.notNull(route, "Route");
        return new PoolEntryFuture<E>(this.lock, callback) {

            @Override
            public E getPoolEntry(
                    final long timeout,
                    final TimeUnit tunit)
                    throws InterruptedException, TimeoutException, IOException {
                final E entry = getPoolEntryBlocking(route, state, timeout, tunit, this);
                onLease(entry);
                return entry;
            }

        };
    }


    public Future<E> lease(final T route, final Object state) {
        return lease(route, state, null);
    }

    private E getPoolEntryBlocking(
            final T route, final Object state,
            final long timeout, final TimeUnit tunit,
            final PoolEntryFuture<E> future)
            throws IOException, InterruptedException, TimeoutException {

        Date deadline = null;
        if (timeout > 0) {
            deadline = new Date
                    (System.currentTimeMillis() + tunit.toMillis(timeout));
        }

        this.lock.lock();
        try {
            final RouteSpecificPool<T, C, E> pool = getPool(route);
            E entry;
            while (true) {
                for (; ; ) {
                    entry = pool.getFree(state);
                    if (entry == null) {
                        break;
                    }
                    if (entry.isExpired(System.currentTimeMillis())) {
                        entry.close();
                    } else if (this.validateAfterInactivity > 0) {
                        if (entry.getUpdated() + this.validateAfterInactivity <= System.currentTimeMillis()) {
                            if (!validate(entry)) {
                                entry.close();
                            }
                        }
                    }
                    if (entry.isClosed()) {
                        this.available.remove(entry);
                        pool.free(entry, false);
                    } else {
                        break;
                    }
                }
                if (entry != null) {
                    this.available.remove(entry);
                    this.leased.add(entry);
                    onReuse(entry);
                    return entry;
                }

                // New connection is needed
                final int maxPerRoute = getMax(route);
                // Shrink the pool prior to allocating a new connection
                final int excess = Math.max(0, pool.getAllocatedCount() + 1 - maxPerRoute);
                if (excess > 0) {
                    for (int i = 0; i < excess; i++) {
                        final E lastUsed = pool.getLastUsed();
                        if (lastUsed == null) {
                            break;
                        }
                        lastUsed.close();
                        this.available.remove(lastUsed);
                        pool.remove(lastUsed);
                    }
                }

                if (pool.getAllocatedCount() < maxPerRoute) {
                    final int totalUsed = this.leased.size();
                    final int freeCapacity = Math.max(this.maxTotal - totalUsed, 0);
                    if (freeCapacity > 0) {
                        final int totalAvailable = this.available.size();
                        if (totalAvailable > freeCapacity - 1) {
                            final E lastUsed = this.available.removeLast();
                            lastUsed.close();
                            final RouteSpecificPool<T, C, E> otherpool = getPool(lastUsed.getRoute());
                            otherpool.remove(lastUsed);
                        }
                        final C conn = this.connFactory.create(route);
                        entry = pool.add(conn);
                        this.leased.add(entry);
                        return entry;
                    }
                }

                boolean success = false;
                try {
                    pool.queue(future);
                    this.pending.add(future);
                    success = future.await(deadline);
                } finally {
                    // In case of 'success', we were woken up by the
                    // connection pool and should now have a connection
                    // waiting for us, or else we're shutting down.
                    // Just continue in the loop, both cases are checked.
                    pool.unqueue(future);
                    this.pending.remove(future);
                }
                // check for spurious wakeup vs. timeout
                if (!success && (deadline != null) &&
                        (deadline.getTime() <= System.currentTimeMillis())) {
                    break;
                }
            }
            throw new TimeoutException("Timeout waiting for connection");
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public void release(final E entry, final boolean reusable) {
        this.lock.lock();
        try {
            if (this.leased.remove(entry)) {
                final RouteSpecificPool<T, C, E> pool = getPool(entry.getRoute());
                pool.free(entry, reusable);
                if (reusable && !this.isShutDown) {
                    this.available.addFirst(entry);
                    onRelease(entry);
                } else {
                    entry.close();
                }
                PoolEntryFuture<E> future = pool.nextPending();
                if (future != null) {
                    this.pending.remove(future);
                } else {
                    future = this.pending.poll();
                }
                if (future != null) {
                    future.wakeup();
                }
            }
        } finally {
            this.lock.unlock();
        }
    }

    private int getMax(final T route) {
        final Integer v = this.maxPerRoute.get(route);
        if (v != null) {
            return v.intValue();
        } else {
            return this.defaultMaxPerRoute;
        }
    }

    @Override
    public void setMaxTotal(final int max) {
        Args.positive(max, "Max value");
        this.lock.lock();
        try {
            this.maxTotal = max;
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public int getMaxTotal() {
        this.lock.lock();
        try {
            return this.maxTotal;
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public void setDefaultMaxPerRoute(final int max) {
        Args.positive(max, "Max per route value");
        this.lock.lock();
        try {
            this.defaultMaxPerRoute = max;
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public int getDefaultMaxPerRoute() {
        this.lock.lock();
        try {
            return this.defaultMaxPerRoute;
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public void setMaxPerRoute(final T route, final int max) {
        Args.notNull(route, "Route");
        Args.positive(max, "Max per route value");
        this.lock.lock();
        try {
            this.maxPerRoute.put(route, Integer.valueOf(max));
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public int getMaxPerRoute(final T route) {
        Args.notNull(route, "Route");
        this.lock.lock();
        try {
            return getMax(route);
        } finally {
            this.lock.unlock();
        }
    }

    public Set<T> getRoutes() {
        this.lock.lock();
        try {
            return new HashSet<T>(routeToPool.keySet());
        } finally {
            this.lock.unlock();
        }
    }


    protected void enumAvailable(final PoolEntryCallback<T, C> callback) {
        this.lock.lock();
        try {
            final Iterator<E> it = this.available.iterator();
            while (it.hasNext()) {
                final E entry = it.next();
                callback.process(entry);
                if (entry.isClosed()) {
                    final RouteSpecificPool<T, C, E> pool = getPool(entry.getRoute());
                    pool.remove(entry);
                    it.remove();
                }
            }
            purgePoolMap();
        } finally {
            this.lock.unlock();
        }
    }


    protected void enumLeased(final PoolEntryCallback<T, C> callback) {
        this.lock.lock();
        try {
            final Iterator<E> it = this.leased.iterator();
            while (it.hasNext()) {
                final E entry = it.next();
                callback.process(entry);
            }
        } finally {
            this.lock.unlock();
        }
    }

    private void purgePoolMap() {
        final Iterator<Map.Entry<T, RouteSpecificPool<T, C, E>>> it = this.routeToPool.entrySet().iterator();
        while (it.hasNext()) {
            final Map.Entry<T, RouteSpecificPool<T, C, E>> entry = it.next();
            final RouteSpecificPool<T, C, E> pool = entry.getValue();
            if (pool.getPendingCount() + pool.getAllocatedCount() == 0) {
                it.remove();
            }
        }
    }


    public void closeIdle(final long idletime, final TimeUnit tunit) {
        Args.notNull(tunit, "Time unit");
        long time = tunit.toMillis(idletime);
        if (time < 0) {
            time = 0;
        }
        final long deadline = System.currentTimeMillis() - time;
        enumAvailable(new PoolEntryCallback<T, C>() {

            @Override
            public void process(final PoolEntry<T, C> entry) {
                if (entry.getUpdated() <= deadline) {
                    entry.close();
                }
            }

        });
    }

    public void closeExpired() {
        final long now = System.currentTimeMillis();
        enumAvailable(new PoolEntryCallback<T, C>() {

            @Override
            public void process(final PoolEntry<T, C> entry) {
                if (entry.isExpired(now)) {
                    entry.close();
                }
            }

        });
    }


    public int getValidateAfterInactivity() {
        return this.validateAfterInactivity;
    }


    public void setValidateAfterInactivity(final int ms) {
        this.validateAfterInactivity = ms;
    }

    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("[leased: ");
        buffer.append(this.leased);
        buffer.append("][available: ");
        buffer.append(this.available);
        buffer.append("][pending: ");
        buffer.append(this.pending);
        buffer.append("]");
        return buffer.toString();
    }

}
