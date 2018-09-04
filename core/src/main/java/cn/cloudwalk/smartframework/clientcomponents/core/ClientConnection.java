package cn.cloudwalk.smartframework.clientcomponents.core;

import java.io.IOException;

/**
 * 客户端连接
 *
 * @since 1.0.0
 */
public interface ClientConnection extends Connection {

    /**
     * 发送请求
     *
     * @param request 请求
     * @throws IOException
     */
    void sendRequest(Object request) throws IOException;

}
