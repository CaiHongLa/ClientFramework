package cn.cloudwalk.smartframework.clientcomponents.client.pool;


import cn.cloudwalk.smartframework.clientcomponents.client.TcpRoute;
import cn.cloudwalk.smartframework.clientcomponents.core.ClientConnection;
import cn.cloudwalk.smartframework.clientcomponents.core.ManagedClientConnection;
import cn.cloudwalk.smartframework.clientcomponents.core.entry.PoolEntry;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


public class CPoolEntry extends PoolEntry<TcpRoute, ManagedClientConnection> {

    private volatile boolean routeComplete;

    public CPoolEntry(
            final String id,
            final TcpRoute route,
            final ManagedClientConnection conn,
            final long timeToLive, final TimeUnit tunit) {
        super(id, route, conn, timeToLive, tunit);
    }

    public void markRouteComplete() {
        this.routeComplete = true;
    }

    public boolean isRouteComplete() {
        return this.routeComplete;
    }

    public void closeConnection() throws IOException {
        final ClientConnection conn = getConnection();
        conn.close();
    }

    public void shutdownConnection() throws IOException {
        final ClientConnection conn = getConnection();
        conn.shutdown();
    }

    @Override
    public boolean isExpired(final long now) {
        final boolean expired = super.isExpired(now);
        return expired;
    }

    @Override
    public boolean isClosed() {
        final ClientConnection conn = getConnection();
        return !conn.isOpen();
    }

    @Override
    public void close() {
        try {
            closeConnection();
        } catch (final IOException ex) {
        }
    }

}
