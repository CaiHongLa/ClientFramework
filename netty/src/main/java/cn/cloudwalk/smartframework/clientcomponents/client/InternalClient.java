package cn.cloudwalk.smartframework.clientcomponents.client;

import cn.cloudwalk.smartframework.clientcomponents.client.conn.ConnectionHolder;
import cn.cloudwalk.smartframework.clientcomponents.core.ClientConnection;
import cn.cloudwalk.smartframework.clientcomponents.core.ClientConnectionManager;
import cn.cloudwalk.smartframework.clientcomponents.core.ConnectionRequest;
import cn.cloudwalk.smartframework.clientcomponents.core.Route;
import cn.cloudwalk.smartframework.clientcomponents.core.config.RequestConfig;
import cn.cloudwalk.smartframework.clientcomponents.core.exception.ConnectionShutdownException;
import cn.cloudwalk.smartframework.clientcomponents.core.exception.RequestAbortedException;
import cn.cloudwalk.smartframework.clientcomponents.core.util.Args;

import java.io.Closeable;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


@SuppressWarnings("deprecation")
public class InternalClient extends CloseableClient {


    private final ClientConnectionManager connManager;
    private final RequestConfig defaultConfig;
    private final List<Closeable> closeables;


    public InternalClient(
            final ClientConnectionManager connManager,
            final RequestConfig defaultConfig,
            final List<Closeable> closeables) {
        Args.notNull(connManager, "HTTP connection manager");
        this.connManager = connManager;
        this.defaultConfig = defaultConfig;
        this.closeables = closeables;
    }


    @Override
    public void close() {
        if (this.closeables != null) {
            for (final Closeable closeable : this.closeables) {
                try {
                    closeable.close();
                } catch (final IOException ex) {
                }
            }
        }
    }

    @Override
    protected Object doExecute(Route route, Object request) throws IOException {
        Args.notNull(route, " route");
        Args.notNull(request, " request");

        final ConnectionRequest connRequest = connManager.requestConnection(route);

        final RequestConfig config = RequestConfig.DEFAULT;

        final ClientConnection managedConn;
        try {
            final int timeout = config.getConnectionRequestTimeout();
            managedConn = connRequest.get(timeout > 0 ? timeout : 0, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new RequestAbortedException("Request aborted", interrupted);
        } catch (final ExecutionException ex) {
            Throwable cause = ex.getCause();
            if (cause == null) {
                cause = ex;
            }
            throw new RequestAbortedException("Request execution failed", cause);
        }

        if (config.isStaleConnectionCheckEnabled()) {
            // validate connection
            if (managedConn.isOpen()) {
                if (managedConn.isStale()) {
                    managedConn.close();
                }
            }
        }

        final ConnectionHolder connHolder = new ConnectionHolder(this.connManager, managedConn);
        try {

            if (!managedConn.isOpen()) {
                establishRoute(managedConn, route);
            }

            managedConn.sendRequest(request);
            connHolder.setValidFor(3000, TimeUnit.MILLISECONDS);
            connHolder.markReusable();
            connHolder.releaseConnection();
            return null;
        } catch (final ConnectionShutdownException ex) {
            final InterruptedIOException ioex = new InterruptedIOException(
                    "Connection has been shut down");
            ioex.initCause(ex);
            throw ioex;
        } catch (final IOException | RuntimeException ex) {
            connHolder.abortConnection();
            throw ex;
        }
    }


    private void establishRoute(
            final ClientConnection managedConn,
            final Route route) throws IOException {
        final int timeout = defaultConfig.getConnectTimeout();
        this.connManager.connect(
                managedConn,
                route,
                timeout > 0 ? timeout : 0);
    }
}
