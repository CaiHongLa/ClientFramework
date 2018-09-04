package cn.cloudwalk.smartframework.clientcomponents.client;

import cn.cloudwalk.smartframework.clientcomponents.core.Client;
import cn.cloudwalk.smartframework.clientcomponents.core.Route;

import java.io.Closeable;
import java.io.IOException;

public abstract class CloseableClient implements Client, Closeable {

    protected abstract Object doExecute(Route route, Object request) throws IOException;

    @Override
    public Object execute(Route route, Object request) throws IOException {
        return doExecute(route, request);
    }

}
