package cn.cloudwalk.smartframework.clientcomponents.client;

import java.io.IOException;

public class TcpRequestExecutor {

    public static final int DEFAULT_WAIT_FOR_CONTINUE = 3000;

    private final int waitForContinue;

    /**
     * Creates new instance of HttpRequestExecutor.
     *
     * @since 4.3
     */
    public TcpRequestExecutor(final int waitForContinue) {
        super();
        this.waitForContinue = Args.positive(waitForContinue, "Wait for continue time");
    }

    public TcpRequestExecutor() {
        this(DEFAULT_WAIT_FOR_CONTINUE);
    }


    protected boolean canResponseHaveBody(final Object request,
                                          final Object response) {

        return true;
    }

    public Object execute(
            final Object request,
            final TcpClientConnection conn,
            final Object context) throws IOException {
        Args.notNull(request, "request");
        Args.notNull(conn, "Client connection");
        Args.notNull(context, "context");
        try {
            Object response = doSendRequest(request, conn, context);
            if (response == null) {
                response = doReceiveResponse(request, conn, context);
            }
            return response;
        } catch (final IOException ex) {
            closeConnection(conn);
            throw ex;
        } catch (final RuntimeException ex) {
            closeConnection(conn);
            throw ex;
        }
    }

    private static void closeConnection(final TcpClientConnection conn) {
        try {
            conn.close();
        } catch (final IOException ignore) {
        }
    }

    protected Object doSendRequest(
            final Object request,
            final TcpClientConnection conn,
            final Object context) throws IOException {
        Args.notNull(request, "HTTP request");
        Args.notNull(conn, "Client connection");
        Args.notNull(context, "HTTP context");

        Object response = null;

        conn.sendRequest(request);
        conn.flush();
        return response;
    }

    protected Object doReceiveResponse(
            final Object request,
            final TcpClientConnection conn,
            final Object context) throws IOException {
        Args.notNull(request, "HTTP request");
        Args.notNull(conn, "Client connection");
        Args.notNull(context, "HTTP context");
        Object response = null;
        int statusCode = 0;

        while (response == null) {


        } // while intermediate response

        return response;
    }

}
