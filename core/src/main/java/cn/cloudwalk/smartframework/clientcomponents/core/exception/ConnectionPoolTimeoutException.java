package cn.cloudwalk.smartframework.clientcomponents.core.exception;

public class ConnectionPoolTimeoutException extends ConnectTimeoutException {

    private static final long serialVersionUID = -1L;

    public ConnectionPoolTimeoutException() {
        super();
    }

    public ConnectionPoolTimeoutException(final String message) {
        super(message);
    }

}
