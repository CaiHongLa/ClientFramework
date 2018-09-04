package cn.cloudwalk.smartframework.clientcomponents.core;

import java.io.Closeable;
import java.io.IOException;

/**
 * @since 1.0.0
 */
public interface ManagedClient extends NetConnection, Closeable {

    void send(Object request) throws IOException;

    boolean isClosed();

    boolean isConnected();
}
