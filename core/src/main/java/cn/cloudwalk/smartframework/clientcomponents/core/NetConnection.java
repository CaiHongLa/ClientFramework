package cn.cloudwalk.smartframework.clientcomponents.core;

import java.net.InetSocketAddress;

/**
 * @since 1.0.0
 */
public interface NetConnection {

    InetSocketAddress getLocalAddress();

    int getLocalPort();

    InetSocketAddress getRemoteAddress();

    int getRemotePort();
}
