package cn.cloudwalk.smartframework.clientcomponents.core;

public interface FutureCallback<T> {

    void completed(T result);

    void failed(Exception ex);

    void cancelled();

}
