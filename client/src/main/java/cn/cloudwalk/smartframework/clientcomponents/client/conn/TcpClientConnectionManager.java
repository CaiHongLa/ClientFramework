package cn.cloudwalk.smartframework.clientcomponents.client.conn;

import cn.cloudwalk.smartframework.clientcomponents.client.ConnectionRequest;
import cn.cloudwalk.smartframework.clientcomponents.client.TcpClientConnection;
import cn.cloudwalk.smartframework.clientcomponents.client.route.TcpRoute;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public interface TcpClientConnectionManager {

    ConnectionRequest requestConnection(TcpRoute route, Object state);

    void releaseConnection(TcpClientConnection conn, Object newState, long validDuration, TimeUnit timeUnit);

    void connect(TcpClientConnection conn, TcpRoute route, int connectTimeout, Object context) throws IOException;

    void upgrade(TcpClientConnection conn, TcpRoute route, Object context) throws IOException;

    void routeComplete(TcpClientConnection conn, TcpRoute route, Object context) throws IOException;

    void closeIdleConnections(long idletime, TimeUnit tunit);

    void closeExpiredConnections();

    void shutdown();
}
