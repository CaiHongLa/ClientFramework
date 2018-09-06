package cn.cloudwalk.smartframework.clientcomponents.client.conn;

import cn.cloudwalk.smartframework.clientcomponents.client.TcpRoute;
import cn.cloudwalk.smartframework.clientcomponents.client.pool.CPool;
import cn.cloudwalk.smartframework.clientcomponents.client.pool.CPoolEntry;
import cn.cloudwalk.smartframework.clientcomponents.client.pool.CPoolProxy;
import cn.cloudwalk.smartframework.clientcomponents.core.*;
import cn.cloudwalk.smartframework.clientcomponents.core.config.RequestConfig;
import cn.cloudwalk.smartframework.clientcomponents.core.exception.ConnectionPoolTimeoutException;
import cn.cloudwalk.smartframework.clientcomponents.core.util.Args;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class PoolingTcpClientConnectionManager implements ClientConnectionManager, ConnectionPoolControlAware<TcpRoute>, Closeable {

    private final AtomicBoolean isShutDown;

    private final CPool pool;
    private final ConfigData configData;

    private final ClientConnectionOperator connectionOperator;

    public PoolingTcpClientConnectionManager(AtomicBoolean isShutDown, ClientConnectionOperator connectionOperator, CPool pool, ConfigData configData) {
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
    public ConnectionRequest requestConnection(Route route) {
        final Future<CPoolEntry> future = this.pool.lease((TcpRoute) route, null, null);
        return new ConnectionRequest() {

            @Override
            public boolean cancel() {
                return future.cancel(true);
            }

            @Override
            public ClientConnection get(
                    final long timeout,
                    final TimeUnit timeUnit) throws InterruptedException, ExecutionException, ConnectionPoolTimeoutException {
                return leaseConnection(future, timeout, timeUnit);
            }

        };
    }

    protected ClientConnection leaseConnection(
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

    @Override
    public void releaseConnection(ClientConnection managedConn, long keepAlive, TimeUnit timeUnit) {
        Args.notNull(managedConn, "Managed connection");
        synchronized (managedConn) {
            final CPoolEntry entry = CPoolProxy.detach(managedConn);
            if (entry == null) {
                return;
            }
            final ManagedClientConnection conn = entry.getConnection();
            try {
                if (conn.isOpen()) {
                    final TimeUnit effectiveUnit = timeUnit != null ? timeUnit : TimeUnit.MILLISECONDS;
                    entry.setState(null);
                    entry.updateExpiry(keepAlive, effectiveUnit);
                }
            } finally {
                this.pool.release(entry, true);
            }
        }
    }

    @Override
    public void connect(ClientConnection managedConn, Route route) throws IOException {
        Args.notNull(managedConn, "Managed Connection");
        Args.notNull(route, "HTTP route");
        final ManagedClientConnection conn;
        synchronized (managedConn) {
            final CPoolEntry entry = CPoolProxy.getPoolEntry(managedConn);
            conn = entry.getConnection();
        }
        final InetSocketAddress host = route.getTargetAddress();
        RequestConfig socketConfig = this.configData.getRequestConfig(host);
        if (socketConfig == null) {
            socketConfig = this.configData.getDefaultRequestConfig();
        }
        if (socketConfig == null) {
            socketConfig = RequestConfig.DEFAULT;
        }
        this.connectionOperator.connect(conn, host, socketConfig);
    }

    @Override
    public void closeIdleConnections(long idleTime, TimeUnit timeUnit) {
        this.pool.closeIdle(idleTime, timeUnit);
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

    public void setRequestConfig(final InetSocketAddress host, final RequestConfig socketConfig) {
        this.configData.setRequestConfig(host, socketConfig);
    }


    public static class ConfigData {

        private final Map<InetSocketAddress, RequestConfig> requestConfigMap;
        private volatile RequestConfig defaultRequestConfig;

        public ConfigData() {
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

    public static class InternalConnectionFactory implements ConnectionFactory<TcpRoute, ManagedClientConnection> {
        private static final AtomicLong COUNTER = new AtomicLong();
        public InternalConnectionFactory() {
            super();
        }

        @Override
        public ManagedClientConnection create(final TcpRoute route) throws IOException {
            final String id = "tcp-outgoing-" + Long.toString(COUNTER.getAndIncrement());
            return new DefaultSocketConnection(id);
        }

    }


}
