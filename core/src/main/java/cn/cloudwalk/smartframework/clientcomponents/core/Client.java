package cn.cloudwalk.smartframework.clientcomponents.core;

import java.io.IOException;

public interface Client {

    Object execute(Route route, Object request) throws IOException;

}
