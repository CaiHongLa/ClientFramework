package cn.cloudwalk.smartframework.clientcomponents;

import cn.cloudwalk.smartframework.clientcomponents.client.CloseableClient;
import cn.cloudwalk.smartframework.clientcomponents.client.TcpRoute;
import cn.cloudwalk.smartframework.clientcomponents.tcp.message.NettyMessage;

import java.io.IOException;

public class TestMain {

    public static void main(String[] args) throws IOException, InterruptedException {
        TcpRoute route = new TcpRoute("192.168.10.40", 8004);
        long start = System.currentTimeMillis();
        CloseableClient closeableClient = ClientBuilder.create().build();
        for(int i = 0 ; i < 10 ; i++) {
            SendThread thread = new SendThread(closeableClient, route);
            new Thread(thread).start();
        }


        Count.count.await();

        System.out.println(System.currentTimeMillis() - start);
    }

    static class SendThread implements Runnable {

        private final CloseableClient closeableClient;
        private final TcpRoute route;
        private final NettyMessage message;

        public SendThread(CloseableClient closeableClient, TcpRoute route) {
            this.closeableClient = closeableClient;
            this.route = route;
            message = new NettyMessage();
            NettyMessage.NettyMessageHeader header = new NettyMessage.NettyMessageHeader();
            header.setSign((byte) 10);
            header.setType((byte) 1);
            message.setProtocolBody("{}");
            message.setProtocolHeader(header);

        }


        @Override
        public void run() {
            for (int i = 0; i < 1000000; i++) {
                try {
                    closeableClient.execute(route, message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
