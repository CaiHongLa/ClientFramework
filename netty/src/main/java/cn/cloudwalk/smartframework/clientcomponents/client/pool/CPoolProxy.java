package cn.cloudwalk.smartframework.clientcomponents.client.pool;


import cn.cloudwalk.smartframework.clientcomponents.core.ClientConnection;
import cn.cloudwalk.smartframework.clientcomponents.core.ManagedClientConnection;
import cn.cloudwalk.smartframework.clientcomponents.core.exception.ConnectionShutdownException;
import cn.cloudwalk.smartframework.transport.Client;

import java.io.IOException;
import java.net.InetSocketAddress;


public class CPoolProxy implements ManagedClientConnection {

    private volatile CPoolEntry poolEntry;

    CPoolProxy(final CPoolEntry entry) {
        super();
        this.poolEntry = entry;
    }

    CPoolEntry getPoolEntry() {
        return this.poolEntry;
    }

    CPoolEntry detach() {
        final CPoolEntry local = this.poolEntry;
        this.poolEntry = null;
        return local;
    }

    ManagedClientConnection getConnection() {
        final CPoolEntry local = this.poolEntry;
        if (local == null) {
            return null;
        }
        return local.getConnection();
    }

    ManagedClientConnection getValidConnection() {
        final ManagedClientConnection conn = getConnection();
        if (conn == null) {
            throw new ConnectionShutdownException();
        }
        return conn;
    }

    @Override
    public void close() throws IOException {
        final CPoolEntry local = this.poolEntry;
        if (local != null) {
            local.closeConnection();
        }
    }

    @Override
    public void shutdown() throws IOException {
        final CPoolEntry local = this.poolEntry;
        if (local != null) {
            local.shutdownConnection();
        }
    }

    @Override
    public boolean isOpen() {
        final CPoolEntry local = this.poolEntry;
        if (local != null) {
            return !local.isClosed();
        } else {
            return false;
        }
    }

    @Override
    public boolean isStale() {
        final ClientConnection conn = getConnection();
        if (conn != null) {
            return conn.isStale();
        } else {
            return true;
        }
    }


    @Override
    public String getId() {
        return getValidConnection().getId();
    }

    @Override
    public void bind(final Client client) throws IOException {
        getValidConnection().bind(client);
    }

    @Override
    public Client getClient() {
        return getValidConnection().getClient();
    }

    @Override
    public void sendRequest(Object request) throws IOException {
        getValidConnection().sendRequest(request);
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return getValidConnection().getLocalAddress();
    }

    @Override
    public int getLocalPort() {
        return getValidConnection().getLocalPort();
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return getValidConnection().getRemoteAddress();
    }

    @Override
    public int getRemotePort() {
        return getValidConnection().getRemotePort();
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CPoolProxy{");
        final ManagedClientConnection conn = getConnection();
        if (conn != null) {
            sb.append(conn);
        } else {
            sb.append("detached");
        }
        sb.append('}');
        return sb.toString();
    }

    public static ClientConnection newProxy(final CPoolEntry poolEntry) {
        return new CPoolProxy(poolEntry);
    }

    private static CPoolProxy getProxy(final ClientConnection conn) {
        if (!(conn instanceof CPoolProxy)) {
            throw new IllegalStateException("Unexpected connection proxy class: " + conn.getClass());
        }
        return (CPoolProxy) conn;
    }

    public static CPoolEntry getPoolEntry(final ClientConnection proxy) {
        final CPoolEntry entry = getProxy(proxy).getPoolEntry();
        if (entry == null) {
            throw new ConnectionShutdownException();
        }
        return entry;
    }

    public static CPoolEntry detach(final ClientConnection conn) {
        return getProxy(conn).detach();
    }

}
