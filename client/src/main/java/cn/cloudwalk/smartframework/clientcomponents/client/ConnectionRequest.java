package cn.cloudwalk.smartframework.clientcomponents.client;

import cn.cloudwalk.smartframework.clientcomponents.client.concurrent.Cancellable;
import cn.cloudwalk.smartframework.clientcomponents.client.conn.ConnectionPoolTimeoutException;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public interface ConnectionRequest extends Cancellable {

    TcpClientConnection get(long timeout, TimeUnit tunit)
            throws InterruptedException, ExecutionException, ConnectionPoolTimeoutException;

}
