package cn.cloudwalk.smartframework.clientcomponents.core;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * 连接 （代表一个可以进行数据传输的通道）
 *
 * @since 1.0.0
 * @see java.io.Closeable  可关闭的
 */
public interface Connection extends Closeable {

    /**
     * 是否打开可用
     *
     * @return
     */
    boolean isOpen();

    /**
     * 是否损坏
     *
     * @return
     */
    boolean isStale();

    /**
     * 停止
     *
     * @throws IOException
     */
    void shutdown() throws IOException;

    InetSocketAddress getLocalAddress();

    int getLocalPort();

    InetSocketAddress getRemoteAddress();

    int getRemotePort();

}
