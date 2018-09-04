package cn.cloudwalk.smartframework.clientcomponents.core.entry;

import cn.cloudwalk.smartframework.clientcomponents.core.util.Args;

import java.util.concurrent.TimeUnit;

public abstract class PoolEntry<T, C> {

    private final String id;
    private final T route;
    private final C conn;
    private final long created;
    private final long validityDeadline;

    private long updated;

    private long expiry;

    private volatile Object state;


    public PoolEntry(final String id, final T route, final C conn,
                     final long timeToLive, final TimeUnit tunit) {
        super();
        Args.notNull(route, "Route");
        Args.notNull(conn, "Connection");
        Args.notNull(tunit, "Time unit");
        this.id = id;
        this.route = route;
        this.conn = conn;
        this.created = System.currentTimeMillis();
        if (timeToLive > 0) {
            this.validityDeadline = this.created + tunit.toMillis(timeToLive);
        } else {
            this.validityDeadline = Long.MAX_VALUE;
        }
        this.expiry = this.validityDeadline;
    }


    public PoolEntry(final String id, final T route, final C conn) {
        this(id, route, conn, 0, TimeUnit.MILLISECONDS);
    }

    public String getId() {
        return this.id;
    }

    public T getRoute() {
        return this.route;
    }

    public C getConnection() {
        return this.conn;
    }

    public long getCreated() {
        return this.created;
    }


    public long getValidityDeadline() {
        return this.validityDeadline;
    }

    @Deprecated
    public long getValidUnit() {
        return this.validityDeadline;
    }

    public Object getState() {
        return this.state;
    }

    public void setState(final Object state) {
        this.state = state;
    }

    public synchronized long getUpdated() {
        return this.updated;
    }

    public synchronized long getExpiry() {
        return this.expiry;
    }

    public synchronized void updateExpiry(final long time, final TimeUnit tunit) {
        Args.notNull(tunit, "Time unit");
        this.updated = System.currentTimeMillis();
        final long newExpiry;
        if (time > 0) {
            newExpiry = this.updated + tunit.toMillis(time);
        } else {
            newExpiry = Long.MAX_VALUE;
        }
        this.expiry = Math.min(newExpiry, this.validityDeadline);
    }

    public synchronized boolean isExpired(final long now) {
        return now >= this.expiry;
    }

    public abstract void close();

    public abstract boolean isClosed();

    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("[id:");
        buffer.append(this.id);
        buffer.append("][route:");
        buffer.append(this.route);
        buffer.append("][state:");
        buffer.append(this.state);
        buffer.append("]");
        return buffer.toString();
    }

}
