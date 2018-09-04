package cn.cloudwalk.smartframework.clientcomponents.core;

import java.net.InetSocketAddress;

/**
 * 路由
 *
 * @since 1.0.0
 */
public interface Route {

    /**
     * 远程地址
     *
     * @return
     */
    InetSocketAddress getTargetAddress();

    /**
     * 本机地址
     *
     * @return
     */
    InetSocketAddress getLocalAddress();
}
