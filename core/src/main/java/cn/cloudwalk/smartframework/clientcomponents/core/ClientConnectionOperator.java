package cn.cloudwalk.smartframework.clientcomponents.core;

import cn.cloudwalk.smartframework.clientcomponents.core.config.RequestConfig;

import java.io.IOException;

/**
 * 客户端连接操作器
 *
 * @since 1.0.0
 */
public interface ClientConnectionOperator {

    /**
     * 建立网络连接
     *
     * @param conn          建立连接后交给ManagedClientConnection管理
     * @param host          目标地址
     * @param requestConfig 配置
     * @throws IOException
     */
    void connect(ManagedClientConnection conn, Route host, RequestConfig requestConfig) throws IOException;
}
