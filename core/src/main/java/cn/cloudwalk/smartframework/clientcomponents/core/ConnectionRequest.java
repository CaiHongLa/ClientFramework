package cn.cloudwalk.smartframework.clientcomponents.core;

import cn.cloudwalk.smartframework.clientcomponents.core.exception.ConnectionPoolTimeoutException;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 池化连接
 *
 * @see java.util.concurrent.Future
 * @since 1.0.0
 */
public interface ConnectionRequest extends Cancellable {

    /**
     * 从连接池得到的future中拿到连接
     *
     * @param timeout  超时时间
     * @param timeUnit 时间单位
     * @return {@link ClientConnection}
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws ConnectionPoolTimeoutException
     */
    ClientConnection get(long timeout, TimeUnit timeUnit) throws InterruptedException, ExecutionException, ConnectionPoolTimeoutException;

}
