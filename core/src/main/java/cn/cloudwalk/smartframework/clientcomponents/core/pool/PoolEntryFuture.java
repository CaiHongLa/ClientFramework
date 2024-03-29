package cn.cloudwalk.smartframework.clientcomponents.core.pool;

import cn.cloudwalk.smartframework.clientcomponents.core.FutureCallback;
import cn.cloudwalk.smartframework.clientcomponents.core.util.Args;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

abstract class PoolEntryFuture<T> implements Future<T> {

    private final Lock lock;
    private final FutureCallback<T> callback;
    private final Condition condition;
    private volatile boolean cancelled;
    private volatile boolean completed;
    private T result;

    PoolEntryFuture(final Lock lock, final FutureCallback<T> callback) {
        super();
        this.lock = lock;
        this.condition = lock.newCondition();
        this.callback = callback;
    }

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        this.lock.lock();
        try {
            if (this.completed) {
                return false;
            }
            this.completed = true;
            this.cancelled = true;
            if (this.callback != null) {
                this.callback.cancelled();
            }
            this.condition.signalAll();
            return true;
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public boolean isDone() {
        return this.completed;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        try {
            return get(0, TimeUnit.MILLISECONDS);
        } catch (final TimeoutException ex) {
            throw new ExecutionException(ex);
        }
    }

    @Override
    public T get(
            final long timeout,
            final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        Args.notNull(unit, "Time unit");
        this.lock.lock();
        try {
            if (this.completed) {
                return this.result;
            }
            this.result = getPoolEntry(timeout, unit);
            this.completed = true;
            if (this.callback != null) {
                this.callback.completed(this.result);
            }
            return result;
        } catch (final IOException ex) {
            this.completed = true;
            this.result = null;
            if (this.callback != null) {
                this.callback.failed(ex);
            }
            throw new ExecutionException(ex);
        } finally {
            this.lock.unlock();
        }
    }

    protected abstract T getPoolEntry(
            long timeout, TimeUnit unit) throws IOException, InterruptedException, TimeoutException;

    public boolean await(final Date deadline) throws InterruptedException {
        this.lock.lock();
        try {
            if (this.cancelled) {
                throw new InterruptedException("Operation interrupted");
            }
            final boolean success;
            if (deadline != null) {
                success = this.condition.awaitUntil(deadline);
            } else {
                this.condition.await();
                success = true;
            }
            if (this.cancelled) {
                throw new InterruptedException("Operation interrupted");
            }
            return success;
        } finally {
            this.lock.unlock();
        }

    }

    public void wakeup() {
        this.lock.lock();
        try {
            this.condition.signalAll();
        } finally {
            this.lock.unlock();
        }
    }

}
