package cn.cloudwalk.smartframework.clientcomponents.client;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;

public abstract class CloseableTcpClient implements TcpClient, Closeable {

    protected abstract Object doExecute(InetSocketAddress target, Object request) throws IOException;

    @Override
    public Object execute(InetSocketAddress host, Object request) throws IOException {
        return doExecute(host, request);
    }
}
