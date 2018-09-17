package cn.cloudwalk.smartframework.clientcomponents;

import cn.cloudwalk.smartframework.clientcomponents.client.TcpRoute;
import cn.cloudwalk.smartframework.clientcomponents.netty.FixedThreadPool;
import cn.cloudwalk.smartframework.clientcomponents.netty.NettyTransport;
import cn.cloudwalk.smartframework.clientcomponents.tcp.NettyProtocol;
import cn.cloudwalk.smartframework.clientcomponents.tcp.message.NettyMessage;
import cn.cloudwalk.smartframework.transportcomponents.Channel;
import cn.cloudwalk.smartframework.transportcomponents.Client;
import cn.cloudwalk.smartframework.transportcomponents.exchange.ExchangeHandler;
import cn.cloudwalk.smartframework.transportcomponents.exchange.support.ExchangeHandlerAdapter;
import cn.cloudwalk.smartframework.transportcomponents.support.dispatcher.MessageDispatcher;
import cn.cloudwalk.smartframework.transportcomponents.support.transport.TransportContext;
import cn.cloudwalk.smartframework.transportcomponents.support.transport.TransportException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

public class TestMain2 {

    public static void main(String[] args) throws IOException, InterruptedException {
        AtomicLong count = new AtomicLong(0);
        int threadCount = 10;
        int clientClient = 1000;
        int size = clientClient / threadCount;
        TcpRoute route = new TcpRoute("192.168.10.40", 8004);
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        long start = System.currentTimeMillis();
        List<Client> clients = new ArrayList<>();
        for (int i = 0; i < clientClient; i++) {
            Map<String, String> parameters = new HashMap<>(50);
            TransportContext transportContext = new TransportContext(route.getHostIp(), route.getHostPort(), parameters, new NettyProtocol.NettyCodec(), new NettyTransport(), new FixedThreadPool(), new MessageDispatcher());
            NettyProtocol protocol = new NettyProtocol(transportContext, requestHandler);
            protocol.bind();
            Client client = protocol.getClient();
            clients.add(client);
        }


        int currentIndex = 0;
        for (int i = 0; i < threadCount; i++) {
            SendThread thread = new SendThread(route, countDownLatch, count, clients.subList(currentIndex, currentIndex + size -1));
            new Thread(thread).start();
            currentIndex = currentIndex + size;
        }

        countDownLatch.await();

        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(System.currentTimeMillis() - start);
    }


    /**
     * 消息处理
     */
    private static final ExchangeHandler requestHandler = new ExchangeHandlerAdapter() {
        private Logger logger = LogManager.getLogger();

        @Override
        public void connected(Channel channel) {
        }

        @Override
        public void disconnected(Channel channel) {
        }

        @Override
        public void send(Channel channel, Object message) {

        }

        @Override
        public void received(Channel channel, Object message) throws TransportException {
        }

        @Override
        public void caught(Channel channel, Throwable throwable) {
            if (throwable instanceof TransportException) {
            }
        }
    };


    static class SendThread implements Runnable {

        //        private final CloseableClient closeableClient;
        private final TcpRoute route;
        private final CountDownLatch countDownLatch;
        private final NettyMessage message;
        private final AtomicLong count;
        private final List<Client> clients;

        public SendThread(TcpRoute route, CountDownLatch countDownLatch, AtomicLong count, List<Client> clients) {
//            this.closeableClient = closeableClient;
            this.route = route;
            this.countDownLatch = countDownLatch;
            this.count = count;
            this.clients = clients;
            message = new NettyMessage();
            NettyMessage.NettyMessageHeader header = new NettyMessage.NettyMessageHeader();
            header.setSign((byte) 10);
            header.setType((byte) 1);
            message.setProtocolBody("{}");
            message.setProtocolHeader(header);

        }


        @Override
        public void run() {
            for(Client client : clients){
                for(int i = 0; i < 10000; i++) {
                    try {
                        client.send(message, true);
                    } catch (TransportException e) {
                        e.printStackTrace();
                    }
                }
            }
            countDownLatch.countDown();
        }
    }
}
