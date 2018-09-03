package cn.cloudwalk.smartframework.clientcomponents.client;

import java.io.IOException;
import java.net.InetSocketAddress;

public interface TcpClient {

    Object execute(InetSocketAddress host, Object request) throws IOException;

}
