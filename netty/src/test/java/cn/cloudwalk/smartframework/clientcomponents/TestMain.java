package cn.cloudwalk.smartframework.clientcomponents;

import cn.cloudwalk.smartframework.clientcomponents.client.CloseableClient;
import cn.cloudwalk.smartframework.clientcomponents.client.TcpRoute;
import cn.cloudwalk.smartframework.clientcomponents.tcp.message.NettyMessage;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

public class TestMain {

    public static void main(String[] args) throws IOException {
        AtomicLong count = new AtomicLong(0);
        int threadCount = 100;
        TcpRoute route = new TcpRoute("10.10.1.191", 8004);
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        long start = System.currentTimeMillis();
        for (int i = 0; i < threadCount; i++) {
            CloseableClient closeableClient = ClientBuilder.create().build();
            SendThread thread = new SendThread(closeableClient, route, countDownLatch, count);
            new Thread(thread).start();
        }
        while (countDownLatch.getCount() > 0){

        }
        System.out.println(System.currentTimeMillis() - start);
    }

    static class SendThread implements Runnable {

        private final CloseableClient closeableClient;
        private final TcpRoute route;
        private final CountDownLatch countDownLatch;
        private final NettyMessage message;
        private final AtomicLong count;

        public SendThread(CloseableClient closeableClient, TcpRoute route, CountDownLatch countDownLatch, AtomicLong count) {
            this.closeableClient = closeableClient;
            this.route = route;
            this.countDownLatch = countDownLatch;
            this.count = count;
            message = new NettyMessage();
            NettyMessage.NettyMessageHeader header = new NettyMessage.NettyMessageHeader();
            header.setSign((byte) 10);
            header.setType((byte) 1);
            message.setProtocolBody("{}");
            message.setProtocolHeader(header);
        }

        @Override
        public void run() {
            for (int i = 0; i < 100000; i++) {
                try {
                    closeableClient.execute(route, message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                System.out.println(count.incrementAndGet());

            }
            countDownLatch.countDown();
        }
    }
}
