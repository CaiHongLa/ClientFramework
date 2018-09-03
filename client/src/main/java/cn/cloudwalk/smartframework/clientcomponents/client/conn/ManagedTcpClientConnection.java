package cn.cloudwalk.smartframework.clientcomponents.client.conn;

import cn.cloudwalk.smartframework.clientcomponents.client.TcpClientConnection;

import java.io.IOException;
import java.net.Socket;

public interface ManagedTcpClientConnection extends TcpClientConnection, TcpInetConnection {

    String getId();

    void bind(Socket socket) throws IOException;

    Socket getSocket();

}
