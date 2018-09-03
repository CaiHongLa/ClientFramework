package cn.cloudwalk.smartframework.clientcomponents.client;

import java.io.Closeable;
import java.io.IOException;

public interface TcpConnection extends Closeable {

    @Override
    void close() throws IOException;

    boolean isOpen();

    boolean isStale();

    void setSocketTimeout(int timeout);

    int getSocketTimeout();

    void shutdown() throws IOException;

}
