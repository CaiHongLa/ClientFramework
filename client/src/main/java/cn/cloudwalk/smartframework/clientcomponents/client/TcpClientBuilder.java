package cn.cloudwalk.smartframework.clientcomponents.client;

import cn.cloudwalk.smartframework.clientcomponents.client.config.RequestConfig;
import cn.cloudwalk.smartframework.clientcomponents.client.conn.PoolingTcpClientConnectionManager;
import cn.cloudwalk.smartframework.clientcomponents.client.conn.TcpClientConnectionManager;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TcpClientBuilder {

    private TcpRequestExecutor requestExecutor;
    private TcpClientConnectionManager connManager;
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

    private List<Closeable> closeables;

    public static TcpClientBuilder create() {
        return new TcpClientBuilder();
    }

    protected TcpClientBuilder() {
        super();
    }

    public TcpClientBuilder setRequestExecutor(TcpRequestExecutor requestExecutor) {
        this.requestExecutor = requestExecutor;
        return this;
    }

    public TcpClientBuilder setDefaultRequestConfig(RequestConfig defaultRequestConfig) {
        this.defaultRequestConfig = defaultRequestConfig;
        return this;
    }

    public final TcpClientBuilder setMaxConnTotal(final int maxConnTotal) {
        this.maxConnTotal = maxConnTotal;
        return this;
    }

    public final TcpClientBuilder setMaxConnPerRoute(final int maxConnPerRoute) {
        this.maxConnPerRoute = maxConnPerRoute;
        return this;
    }

    public final TcpClientBuilder setConnectionTimeToLive(final long connTimeToLive, final TimeUnit connTimeToLiveTimeUnit) {
        this.connTimeToLive = connTimeToLive;
        this.connTimeToLiveTimeUnit = connTimeToLiveTimeUnit;
        return this;
    }

    public final TcpClientBuilder setConnectionManager(
            final TcpClientConnectionManager connManager) {
        this.connManager = connManager;
        return this;
    }

    public final TcpClientBuilder disableConnectionState() {
        connectionStateDisabled = true;
        return this;
    }

    public final TcpClientBuilder evictExpiredConnections() {
        evictExpiredConnections = true;
        return this;
    }

    public final TcpClientBuilder evictIdleConnections(final long maxIdleTime, final TimeUnit maxIdleTimeUnit) {
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


    protected ClientExecChain createMainExec(
            final TcpRequestExecutor requestExec,
            final TcpClientConnectionManager connManager) {
        return new MainClientExec(
                requestExec,
                connManager);
    }

    /**
     * For internal use.
     */
    protected ClientExecChain decorateMainExec(final ClientExecChain mainExec) {
        return mainExec;
    }

    /**
     * For internal use.
     */
    protected ClientExecChain decorateProtocolExec(final ClientExecChain protocolExec) {
        return protocolExec;
    }


    public CloseableTcpClient build() {

        TcpRequestExecutor requestExecCopy = this.requestExecutor;
        if (requestExecCopy == null) {
            requestExecCopy = new TcpRequestExecutor();
        }
        TcpClientConnectionManager connManagerCopy = this.connManager;
        if (connManagerCopy == null) {
            @SuppressWarnings("resource") final PoolingTcpClientConnectionManager poolingmgr = new PoolingTcpClientConnectionManager(
                    null,
                    null,
                    null,
                    null);
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

        ClientExecChain execChain = createMainExec(
                requestExecCopy,
                connManagerCopy);

        execChain = decorateMainExec(execChain);
        List<Closeable> closeablesCopy = closeables != null ? new ArrayList<>(closeables) : null;

        TcpClientConnectionManager cm = connManagerCopy;
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
        return new InternalHttpClient(
                execChain,
                connManagerCopy,
                defaultRequestConfig != null ? defaultRequestConfig : RequestConfig.DEFAULT,
                closeables);
    }
}
