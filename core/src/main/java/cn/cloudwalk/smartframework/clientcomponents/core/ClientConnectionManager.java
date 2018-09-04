package cn.cloudwalk.smartframework.clientcomponents.core;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 连接管理
 *
 * @since 1.0.0
 */
public interface ClientConnectionManager {

    /**
     * 拿到路由的连接
     *
     * @param route
     * @return
     */
    ConnectionRequest requestConnection(Route route);

    /**
     * 释放连接
     *
     * @param conn
     * @param validDuration
     * @param timeUnit
     */
    void releaseConnection(ClientConnection conn, long validDuration, TimeUnit timeUnit);

    /**
     * 建立连接
     *
     * @param conn
     * @param route
     * @param connectTimeout
     * @throws IOException
     */
    void connect(ClientConnection conn, Route route, int connectTimeout) throws IOException;

    /**
     * 关闭超时的连接
     *
     * @param idleTime
     * @param timeUnit
     */
    void closeIdleConnections(long idleTime, TimeUnit timeUnit);

    /**
     * 关闭过期连接
     */
    void closeExpiredConnections();

    /**
     * 关闭
     */
    void shutdown();
}
