package cn.cloudwalk.smartframework.clientcomponents.core;

import java.io.IOException;

/**
 * @since 1.0.0
 */
public interface ConnectionReleaseTrigger {

    void releaseConnection() throws IOException;

    void abortConnection() throws IOException;

}
