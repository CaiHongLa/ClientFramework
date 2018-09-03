package cn.cloudwalk.smartframework.clientcomponents.client.route;

import java.net.InetSocketAddress;

public interface Route {

    InetSocketAddress getTargetAddress();

    InetSocketAddress getLocalAddress();
}
