package cn.cloudwalk.smartframework.clientcomponents.client;

import cn.cloudwalk.smartframework.clientcomponents.client.config.RequestConfig;
import cn.cloudwalk.smartframework.clientcomponents.client.conn.ConnectionHolder;
import cn.cloudwalk.smartframework.clientcomponents.client.conn.ConnectionShutdownException;
import cn.cloudwalk.smartframework.clientcomponents.client.conn.TcpClientConnectionManager;
import cn.cloudwalk.smartframework.clientcomponents.client.route.TcpRoute;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MainClientExec implements ClientExecChain {

    private final TcpRequestExecutor requestExecutor;
    private final TcpClientConnectionManager connManager;

    /**
     * @since 4.4
     */
    public MainClientExec(
            final TcpRequestExecutor requestExecutor,
            final TcpClientConnectionManager connManager) {
        Args.notNull(requestExecutor, "HTTP request executor");
        Args.notNull(connManager, "Client connection manager");
        this.requestExecutor = requestExecutor;
        this.connManager = connManager;
    }


    @Override
    public Object execute(
            final TcpRoute route,
            final Object request) throws IOException {
        Args.notNull(route, "HTTP route");
        Args.notNull(request, "HTTP request");

        final ConnectionRequest connRequest = connManager.requestConnection(route, null);

        final RequestConfig config = RequestConfig.DEFAULT;

        final TcpClientConnection managedConn;
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

            Object response;

            if (!managedConn.isOpen()) {
                establishRoute(managedConn, route);
            }
            final int timeout = config.getSocketTimeout();
            if (timeout >= 0) {
                managedConn.setSocketTimeout(timeout);
            }

            response = requestExecutor.execute(request, managedConn, null);

            connHolder.setValidFor(3000, TimeUnit.MILLISECONDS);
            connHolder.markReusable();
            connHolder.releaseConnection();
            return response;
        } catch (final ConnectionShutdownException ex) {
            final InterruptedIOException ioex = new InterruptedIOException(
                    "Connection has been shut down");
            ioex.initCause(ex);
            throw ioex;
        } catch (final IOException ex) {
            connHolder.abortConnection();
            throw ex;
        } catch (final RuntimeException ex) {
            connHolder.abortConnection();
            throw ex;
        }
    }

    /**
     * Establishes the target route.
     */
    void establishRoute(
            final TcpClientConnection managedConn,
            final TcpRoute route) throws IOException {
        final RequestConfig config = RequestConfig.DEFAULT;
        final int timeout = config.getConnectTimeout();
        this.connManager.connect(
                managedConn,
                route,
                timeout > 0 ? timeout : 0,
                null);
    }

}
