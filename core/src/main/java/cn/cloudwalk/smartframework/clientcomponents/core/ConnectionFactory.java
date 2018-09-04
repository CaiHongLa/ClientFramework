package cn.cloudwalk.smartframework.clientcomponents.core;

import java.io.IOException;

public interface ConnectionFactory<T, C> {

    C create(T route) throws IOException;

}
