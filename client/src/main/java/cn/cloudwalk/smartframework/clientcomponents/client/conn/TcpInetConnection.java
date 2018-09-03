package cn.cloudwalk.smartframework.clientcomponents.client.conn;

import cn.cloudwalk.smartframework.clientcomponents.client.TcpConnection;

import java.net.InetSocketAddress;

public interface TcpInetConnection extends TcpConnection {

    InetSocketAddress getLocalAddress();

    int getLocalPort();

    InetSocketAddress getRemoteAddress();

    int getRemotePort();

}
