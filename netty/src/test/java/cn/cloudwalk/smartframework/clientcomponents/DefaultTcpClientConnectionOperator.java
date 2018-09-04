package cn.cloudwalk.smartframework.clientcomponents;

import cn.cloudwalk.smartframework.clientcomponents.netty.FixedThreadPool;
import cn.cloudwalk.smartframework.clientcomponents.netty.NettyTransport;
import cn.cloudwalk.smartframework.clientcomponents.tcp.NettyProtocol;
import cn.cloudwalk.smartframework.clientcomponents.tcp.message.NettyMessage;
import cn.cloudwalk.smartframework.clientcomponents.core.ClientConnectionOperator;
import cn.cloudwalk.smartframework.clientcomponents.core.ManagedClientConnection;
import cn.cloudwalk.smartframework.clientcomponents.core.config.RequestConfig;
import cn.cloudwalk.smartframework.transport.Channel;
import cn.cloudwalk.smartframework.transport.Client;
import cn.cloudwalk.smartframework.transport.exchange.ExchangeHandler;
import cn.cloudwalk.smartframework.transport.exchange.support.ExchangeHandlerAdapter;
import cn.cloudwalk.smartframework.transport.support.dispatcher.MessageDispatcher;
import cn.cloudwalk.smartframework.transport.support.transport.TransportContext;
import cn.cloudwalk.smartframework.transport.support.transport.TransportException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class DefaultTcpClientConnectionOperator implements ClientConnectionOperator {


    @Override
    public void connect(ManagedClientConnection conn, InetSocketAddress host, RequestConfig requestConfig) throws IOException {

        Map<String, String> parameters = new HashMap<>(50);
        TransportContext transportContext = new TransportContext(host.getHostName(), host.getPort(), parameters, new NettyProtocol.NettyCodec(), new NettyTransport(), new FixedThreadPool(), new MessageDispatcher());
        NettyProtocol protocol = new NettyProtocol(transportContext, requestHandler);
        protocol.bind();
        Client client = protocol.getClient();
        conn.bind(client);
    }


    /**
     * 消息处理
     */
    private final ExchangeHandler requestHandler = new ExchangeHandlerAdapter() {

        @Override
        public void connected(Channel channel) {
            System.out.println("connect");
        }

        @Override
        public void disconnected(Channel channel) {
            System.out.println("disconnect");
        }

        @Override
        public void send(Channel channel, Object message) {
            System.out.println("send");


        }

        @Override
        public void received(Channel channel, Object message) throws TransportException {
            NettyMessage nettyMessage = (NettyMessage) message;
//            System.out.println(nettyMessage);
        }

        @Override
        public void caught(Channel channel, Throwable throwable) {
            if (throwable instanceof TransportException) {
                channel.close();
            }
        }
    };
}