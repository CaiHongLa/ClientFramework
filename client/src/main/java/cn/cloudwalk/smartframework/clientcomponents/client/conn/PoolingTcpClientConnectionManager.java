package cn.cloudwalk.smartframework.clientcomponents.client.conn;

import cn.cloudwalk.smartframework.clientcomponents.client.Args;
import cn.cloudwalk.smartframework.clientcomponents.client.ConnectionRequest;
import cn.cloudwalk.smartframework.clientcomponents.client.TcpClientConnection;
import cn.cloudwalk.smartframework.clientcomponents.client.config.RequestConfig;
import cn.cloudwalk.smartframework.clientcomponents.client.pool.CPool;
import cn.cloudwalk.smartframework.clientcomponents.client.pool.CPoolEntry;
import cn.cloudwalk.smartframework.clientcomponents.client.pool.CPoolProxy;
import cn.cloudwalk.smartframework.clientcomponents.client.pool.ConnPoolControl;
import cn.cloudwalk.smartframework.clientcomponents.client.route.TcpRoute;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class PoolingTcpClientConnectionManager implements TcpClientConnectionManager, ConnPoolControl<TcpRoute>, Closeable {

    private final AtomicBoolean isShutDown;

    private final CPool pool;
    private final ConfigData configData;

    private final TcpClientConnectionOperator connectionOperator;

    public PoolingTcpClientConnectionManager(AtomicBoolean isShutDown, TcpClientConnectionOperator connectionOperator, CPool pool, ConfigData configData) {
        this.isShutDown = isShutDown;
        this.connectionOperator = connectionOperator;
        this.pool = pool;
        this.configData = configData;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            shutdown();
        } finally {
            super.finalize();
        }
    }


    @Override
    public void close() {
        shutdown();
    }

    @Override
    public void setMaxTotal(int max) {
        pool.setMaxTotal(max);
    }

    @Override
    public int getMaxTotal() {
        return pool.getMaxTotal();
    }

    @Override
    public void setDefaultMaxPerRoute(int max) {
        pool.setDefaultMaxPerRoute(max);
    }

    @Override
    public int getDefaultMaxPerRoute() {
        return pool.getDefaultMaxPerRoute();
    }

    @Override
    public void setMaxPerRoute(TcpRoute route, int max) {
        pool.setMaxPerRoute(route, max);
    }

    @Override
    public int getMaxPerRoute(TcpRoute route) {
        return pool.getMaxPerRoute(route);
    }

    @Override
    public ConnectionRequest requestConnection(TcpRoute route, Object state) {
        final Future<CPoolEntry> future = this.pool.lease(route, state, null);
        return new ConnectionRequest() {

            @Override
            public boolean cancel() {
                return future.cancel(true);
            }

            @Override
            public TcpClientConnection get(
                    final long timeout,
                    final TimeUnit tunit) throws InterruptedException, ExecutionException, ConnectionPoolTimeoutException {
                return leaseConnection(future, timeout, tunit);
            }

        };

    }

    @Override
    public void releaseConnection(TcpClientConnection managedConn, Object state, long keepalive, TimeUnit timeUnit) {
        Args.notNull(managedConn, "Managed connection");
        synchronized (managedConn) {
            final CPoolEntry entry = CPoolProxy.detach(managedConn);
            if (entry == null) {
                return;
            }
            final ManagedTcpClientConnection conn = entry.getConnection();
            try {
                if (conn.isOpen()) {
                    final TimeUnit effectiveUnit = timeUnit != null ? timeUnit : TimeUnit.MILLISECONDS;
                    entry.setState(state);
                    entry.updateExpiry(keepalive, effectiveUnit);
                }
            } finally {
                this.pool.release(entry, conn.isOpen() && entry.isRouteComplete());
            }
        }
    }

    @Override
    public void connect(TcpClientConnection managedConn, TcpRoute route, int connectTimeout, Object context) throws IOException {
        Args.notNull(managedConn, "Managed Connection");
        Args.notNull(route, "HTTP route");
        final ManagedTcpClientConnection conn;
        synchronized (managedConn) {
            final CPoolEntry entry = CPoolProxy.getPoolEntry(managedConn);
            conn = entry.getConnection();
        }
        final InetSocketAddress host = route.getTargetAddress();
        final InetSocketAddress localAddress = route.getLocalAddress();
        RequestConfig socketConfig = this.configData.getRequestConfig(host);
        if (socketConfig == null) {
            socketConfig = this.configData.getDefaultRequestConfig();
        }
        if (socketConfig == null) {
            socketConfig = RequestConfig.DEFAULT;
        }
        this.connectionOperator.connect(
                conn, host, localAddress, connectTimeout, socketConfig, context);
    }

    @Override
    public void upgrade(TcpClientConnection managedConn, TcpRoute route, Object context) throws IOException {
        Args.notNull(managedConn, "Managed Connection");
        Args.notNull(route, "HTTP route");
        final ManagedTcpClientConnection conn;
        synchronized (managedConn) {
            final CPoolEntry entry = CPoolProxy.getPoolEntry(managedConn);
            conn = entry.getConnection();
        }
        this.connectionOperator.upgrade(conn, route.getTargetAddress(), context);
    }

    @Override
    public void routeComplete(TcpClientConnection managedConn, TcpRoute route, Object context) throws IOException {
        Args.notNull(managedConn, "Managed Connection");
        Args.notNull(route, "HTTP route");
        synchronized (managedConn) {
            final CPoolEntry entry = CPoolProxy.getPoolEntry(managedConn);
            entry.markRouteComplete();
        }
    }

    @Override
    public void closeIdleConnections(long idletime, TimeUnit tunit) {
        this.pool.closeIdle(idletime, tunit);
    }

    @Override
    public void closeExpiredConnections() {
        this.pool.closeExpired();
    }

    @Override
    public void shutdown() {
        if (this.isShutDown.compareAndSet(false, true)) {
            try {
                this.pool.shutdown();
            } catch (final IOException ex) {
            }
        }
    }

    public Set<TcpRoute> getRoutes() {
        return this.pool.getRoutes();
    }

    public RequestConfig getDefaultRequestConfig() {
        return this.configData.getDefaultRequestConfig();
    }

    public void setDefaultRequestConfig(final RequestConfig defaultRequestConfig) {
        this.configData.setDefaultRequestConfig(defaultRequestConfig);
    }

    public RequestConfig getRequestConfig(final InetSocketAddress host) {
        return this.configData.getRequestConfig(host);
    }

    public void setRequestVonfig(final InetSocketAddress host, final RequestConfig socketConfig) {
        this.configData.setRequestConfig(host, socketConfig);
    }

    protected TcpClientConnection leaseConnection(
            final Future<CPoolEntry> future,
            final long timeout,
            final TimeUnit tunit) throws InterruptedException, ExecutionException, ConnectionPoolTimeoutException {
        final CPoolEntry entry;
        try {
            entry = future.get(timeout, tunit);
            if (entry == null || future.isCancelled()) {
                throw new InterruptedException();
            }
            return CPoolProxy.newProxy(entry);
        } catch (final TimeoutException ex) {
            throw new ConnectionPoolTimeoutException("Timeout waiting for connection from pool");
        }
    }

    static class ConfigData {

        private final Map<InetSocketAddress, RequestConfig> requestConfigMap;
        private volatile RequestConfig defaultRequestConfig;

        ConfigData() {
            super();
            this.requestConfigMap = new ConcurrentHashMap<>();
        }

        public RequestConfig getDefaultRequestConfig() {
            return this.defaultRequestConfig;
        }

        public void setDefaultRequestConfig(final RequestConfig defaultRequestConfig) {
            this.defaultRequestConfig = defaultRequestConfig;
        }

        public RequestConfig getRequestConfig(final InetSocketAddress host) {
            return this.requestConfigMap.get(host);
        }

        public void setRequestConfig(final InetSocketAddress host, final RequestConfig socketConfig) {
            this.requestConfigMap.put(host, socketConfig);
        }

    }


}
