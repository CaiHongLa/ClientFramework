package cn.cloudwalk.smartframework.clientcomponents.core;

import cn.cloudwalk.smartframework.clientcomponents.core.entry.PoolEntry;

public interface PoolEntryCallback<T, C> {

    void process(PoolEntry<T, C> entry);

}
