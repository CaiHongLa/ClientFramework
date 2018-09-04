package cn.cloudwalk.smartframework.clientcomponents.core;

import java.util.concurrent.Future;


public interface ConnectionPool<T, E> {

    Future<E> lease(final T route, final Object state, final FutureCallback<E> callback);

    void release(E entry, boolean reusable);

}
