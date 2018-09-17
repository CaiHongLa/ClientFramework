package cn.cloudwalk.smartframework.clientcomponents;

import java.util.concurrent.CountDownLatch;

/**
 * @since 1.0.0
 */
public class Count {

    public static final CountDownLatch count = new CountDownLatch(1000000);
}
