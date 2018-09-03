package cn.cloudwalk.smartframework.clientcomponents.client;

import java.io.InterruptedIOException;

public class RequestAbortedException extends InterruptedIOException {

    private static final long serialVersionUID = -1L;

    public RequestAbortedException(final String message) {
        super(message);
    }

    public RequestAbortedException(final String message, final Throwable cause) {
        super(message);
        if (cause != null) {
            initCause(cause);
        }
    }
}
