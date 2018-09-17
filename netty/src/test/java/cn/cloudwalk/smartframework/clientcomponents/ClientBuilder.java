package cn.cloudwalk.smartframework.clientcomponents;

import cn.cloudwalk.smartframework.clientcomponents.client.CloseableClient;
import cn.cloudwalk.smartframework.clientcomponents.client.IdleConnectionEvictor;
import cn.cloudwalk.smartframework.clientcomponents.client.InternalClient;
import cn.cloudwalk.smartframework.clientcomponents.client.conn.PoolingTcpClientConnectionManager;
import cn.cloudwalk.smartframework.clientcomponents.client.pool.CPool;
import cn.cloudwalk.smartframework.clientcomponents.core.ClientConnectionManager;
import cn.cloudwalk.smartframework.clientcomponents.core.config.RequestConfig;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientBuilder {

    private ClientConnectionManager connManager;
    private RequestConfig defaultRequestConfig;
    private boolean evictExpiredConnections;
    private boolean evictIdleConnections;
    private long maxIdleTime;
    private TimeUnit maxIdleTimeUnit;
    private boolean systemProperties;
    private boolean redirectHandlingDisabled;
    private boolean automaticRetriesDisabled;
    private boolean contentCompressionDisabled;
    private boolean cookieManagementDisabled;
    private boolean authCachingDisabled;
    private boolean connectionStateDisabled;

    private int maxConnTotal = 0;
    private int maxConnPerRoute = 0;

    private long connTimeToLive = -1;
    private TimeUnit connTimeToLiveTimeUnit = TimeUnit.MILLISECONDS;

    private List<Closeable> closeables = new ArrayList<>();

    public static ClientBuilder create() {
        return new ClientBuilder();
    }

    protected ClientBuilder() {
        super();
    }

    public ClientBuilder setDefaultRequestConfig(RequestConfig defaultRequestConfig) {
        this.defaultRequestConfig = defaultRequestConfig;
        return this;
    }

    public final ClientBuilder setMaxConnTotal(final int maxConnTotal) {
        this.maxConnTotal = maxConnTotal;
        return this;
    }

    public final ClientBuilder setMaxConnPerRoute(final int maxConnPerRoute) {
        this.maxConnPerRoute = maxConnPerRoute;
        return this;
    }

    public final ClientBuilder setConnectionTimeToLive(final long connTimeToLive, final TimeUnit connTimeToLiveTimeUnit) {
        this.connTimeToLive = connTimeToLive;
        this.connTimeToLiveTimeUnit = connTimeToLiveTimeUnit;
        return this;
    }

    public final ClientBuilder setConnectionManager(
            final ClientConnectionManager connManager) {
        this.connManager = connManager;
        return this;
    }

    public final ClientBuilder disableConnectionState() {
        connectionStateDisabled = true;
        return this;
    }

    public final ClientBuilder evictExpiredConnections() {
        evictExpiredConnections = true;
        return this;
    }

    public final ClientBuilder evictIdleConnections(final long maxIdleTime, final TimeUnit maxIdleTimeUnit) {
        this.evictIdleConnections = true;
        this.maxIdleTime = maxIdleTime;
        this.maxIdleTimeUnit = maxIdleTimeUnit;
        return this;
    }

    protected void addCloseable(final Closeable closeable) {
        if (closeable == null) {
            return;
        }
        if (closeables == null) {
            closeables = new ArrayList<Closeable>();
        }
        closeables.add(closeable);
    }


    public CloseableClient build() {

        ClientConnectionManager connManagerCopy = this.connManager;
        if (connManagerCopy == null) {
            @SuppressWarnings("resource") final PoolingTcpClientConnectionManager poolingmgr = new PoolingTcpClientConnectionManager(
                    new AtomicBoolean(false),
                    new DefaultTcpClientConnectionOperator(),
                    new CPool(new PoolingTcpClientConnectionManager.InternalConnectionFactory(), 1000, 1000, 60000, TimeUnit.MILLISECONDS),
                    new PoolingTcpClientConnectionManager.ConfigData());
            if (defaultRequestConfig != null) {
                poolingmgr.setDefaultRequestConfig(defaultRequestConfig);
            }
            if (maxConnTotal > 0) {
                poolingmgr.setMaxTotal(maxConnTotal);
            }
            if (maxConnPerRoute > 0) {
                poolingmgr.setDefaultMaxPerRoute(maxConnPerRoute);
            }
            connManagerCopy = poolingmgr;
        }


        List<Closeable> closeablesCopy = closeables;

        ClientConnectionManager cm = connManagerCopy;
        final IdleConnectionEvictor connectionEvictor = new IdleConnectionEvictor(cm,
                maxIdleTime > 0 ? maxIdleTime : 10, maxIdleTimeUnit != null ? maxIdleTimeUnit : TimeUnit.SECONDS);
        closeablesCopy.add(new Closeable() {

            @Override
            public void close() throws IOException {
                connectionEvictor.shutdown();
            }

        });

        closeablesCopy.add(new Closeable() {

            @Override
            public void close() throws IOException {
                cm.shutdown();
            }

        });
        connectionEvictor.start();
        return new InternalClient(
                connManagerCopy,
                closeables);
    }
}
