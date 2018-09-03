package cn.cloudwalk.smartframework.clientcomponents.client.conn;

import cn.cloudwalk.smartframework.clientcomponents.client.config.RequestConfig;

import java.io.IOException;
import java.net.InetSocketAddress;

public interface TcpClientConnectionOperator {

    void connect(
            ManagedTcpClientConnection conn,
            InetSocketAddress host,
            InetSocketAddress localAddress,
            int connectTimeout,
            RequestConfig requestConfig,
            Object context) throws IOException;

    void upgrade(
            ManagedTcpClientConnection conn,
            InetSocketAddress host,
            Object context) throws IOException;
}
