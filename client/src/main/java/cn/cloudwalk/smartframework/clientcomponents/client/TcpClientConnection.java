package cn.cloudwalk.smartframework.clientcomponents.client;

import java.io.IOException;

public interface TcpClientConnection extends TcpConnection {

    boolean isResponseAvailable(int timeout) throws IOException;

    void sendRequest(Object request) throws IOException;

    void flush() throws IOException;

}
