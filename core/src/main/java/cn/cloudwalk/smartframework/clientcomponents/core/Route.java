package cn.cloudwalk.smartframework.clientcomponents.core;

/**
 * 路由
 *
 * @since 1.0.0
 */
public interface Route {

    /**
     * 远程IP
     *
     * @return
     */
    String getHostIp();

    /**
     * 远程端口
     *
     * @return
     */
    int getHostPort();

}
