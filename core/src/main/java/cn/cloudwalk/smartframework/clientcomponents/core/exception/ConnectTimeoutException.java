package cn.cloudwalk.smartframework.clientcomponents.core.exception;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;

public class ConnectTimeoutException extends InterruptedIOException {

    private static final long serialVersionUID = -1L;

    private final InetAddress host;

    public ConnectTimeoutException() {
        super();
        this.host = null;
    }

    public ConnectTimeoutException(final String message) {
        super(message);
        this.host = null;
    }

    public ConnectTimeoutException(
            final IOException cause,
            final InetAddress host) {
        super("Connect to " +
                (host != null ? host.getHostAddress() : "remote host") +
                ((cause != null && cause.getMessage() != null) ?
                        " failed: " + cause.getMessage() : " timed out"));
        this.host = host;
        initCause(cause);
    }

    public InetAddress getHost() {
        return host;
    }

}
