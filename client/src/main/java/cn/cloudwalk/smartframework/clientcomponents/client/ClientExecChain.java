package cn.cloudwalk.smartframework.clientcomponents.client;

import cn.cloudwalk.smartframework.clientcomponents.client.route.TcpRoute;

import java.io.IOException;

public interface ClientExecChain {

    Object execute(
            TcpRoute route,
            Object request) throws IOException;
}
