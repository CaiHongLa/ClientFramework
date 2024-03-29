package cn.cloudwalk.smartframework.clientcomponents.core;

import java.io.IOException;

/**
 * 可被连接池管理的连接
 *
 * @since 1.0.0
 */
public interface ManagedClientConnection extends ClientConnection {

    String getId();

    /**
     * 绑定一个具体的可以发送数据的实例
     *
     * @param client
     * @throws IOException
     */
    void bind(ManagedClient client) throws IOException;

    ManagedClient getClient();

}
