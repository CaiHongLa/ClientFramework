package cn.cloudwalk.smartframework.clientcomponents.client.pool;

import cn.cloudwalk.smartframework.clientcomponents.client.TcpRoute;
import cn.cloudwalk.smartframework.clientcomponents.core.ConnectionFactory;
import cn.cloudwalk.smartframework.clientcomponents.core.ManagedClientConnection;
import cn.cloudwalk.smartframework.clientcomponents.core.pool.AbstractConnPool;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;


public class CPool extends AbstractConnPool<TcpRoute, ManagedClientConnection, CPoolEntry> {

    private static final AtomicLong COUNTER = new AtomicLong();

    private final long timeToLive;
    private final TimeUnit tunit;

    public CPool(
            final ConnectionFactory<TcpRoute, ManagedClientConnection> connFactory,
            final int defaultMaxPerRoute, final int maxTotal,
            final long timeToLive, final TimeUnit tunit) {
        super(connFactory, defaultMaxPerRoute, maxTotal);
        this.timeToLive = timeToLive;
        this.tunit = tunit;
    }

    @Override
    protected CPoolEntry createEntry(final TcpRoute route, final ManagedClientConnection conn) {
        final String id = Long.toString(COUNTER.getAndIncrement());
        return new CPoolEntry(id, route, conn, this.timeToLive, this.tunit);
    }

    @Override
    protected boolean validate(final CPoolEntry entry) {
        return !entry.getConnection().isStale();
    }

}
