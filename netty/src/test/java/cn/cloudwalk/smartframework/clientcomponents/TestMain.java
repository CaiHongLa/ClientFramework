package cn.cloudwalk.smartframework.clientcomponents;

import cn.cloudwalk.smartframework.clientcomponents.client.CloseableClient;
import cn.cloudwalk.smartframework.clientcomponents.client.TcpRoute;
import cn.cloudwalk.smartframework.clientcomponents.tcp.message.NettyMessage;

import java.io.IOException;
import java.net.InetSocketAddress;

public class TestMain {

    public static void main(String[] args) throws IOException {
        InetSocketAddress host = new InetSocketAddress("10.10.1.40", 8004);
        CloseableClient closeableClient = ClientBuilder.create().build();
        TcpRoute route = new TcpRoute(host, host);
        SendThread thread = new SendThread(closeableClient, route);
        NettyMessage response = new NettyMessage();
        NettyMessage.NettyMessageHeader header = new NettyMessage.NettyMessageHeader();
        header.setSign((byte) 10);
        header.setType((byte) 1);
        response.setProtocolBody("{}");
        response.setProtocolHeader(header);
        for(int i =0 ; i< 100; i++) {
//            closeableClient.execute(route, response);
            new Thread(thread).start();

        }
    }

    static class SendThread implements Runnable{

        private final CloseableClient closeableClient;
        private final TcpRoute route;
        public SendThread(CloseableClient closeableClient, TcpRoute route) {
            this.closeableClient = closeableClient;
            this.route = route;
        }

        @Override
        public void run() {
            NettyMessage response = new NettyMessage();
            NettyMessage.NettyMessageHeader header = new NettyMessage.NettyMessageHeader();
            header.setSign((byte) 10);
            header.setType((byte) 1);
            response.setProtocolBody("{}");
            response.setProtocolHeader(header);
            try {
                closeableClient.execute(route, response);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
