package cn.cloudwalk.smartframework.clientcomponents.netty;

import cn.cloudwalk.smartframework.transportcomponents.ThreadPool;
import cn.cloudwalk.smartframework.transportcomponents.support.transport.TransportContext;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FixedThreadPool implements ThreadPool {

    private Executor executor;

    @Override
    public Executor newExecutor(TransportContext transportContext) {
        return Executors.newFixedThreadPool(20);
    }

}
