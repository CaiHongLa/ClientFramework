package cn.cloudwalk.smartframework.clientcomponents;

import cn.cloudwalk.smartframework.clientcomponents.core.ClientConnectionOperator;
import cn.cloudwalk.smartframework.clientcomponents.core.ManagedClient;
import cn.cloudwalk.smartframework.clientcomponents.core.ManagedClientConnection;
import cn.cloudwalk.smartframework.clientcomponents.core.Route;
import cn.cloudwalk.smartframework.clientcomponents.core.config.RequestConfig;
import cn.cloudwalk.smartframework.clientcomponents.netty.FixedThreadPool;
import cn.cloudwalk.smartframework.clientcomponents.netty.NettyTransport;
import cn.cloudwalk.smartframework.clientcomponents.tcp.NettyProtocol;
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
import java.util.HashMap;
import java.util.Map;

public class DefaultTcpClientConnectionOperator implements ClientConnectionOperator {


    @Override
    public void connect(ManagedClientConnection conn, Route route, RequestConfig requestConfig) throws IOException {

        Map<String, String> parameters = new HashMap<>(50);
        TransportContext transportContext = new TransportContext(route.getHostIp(), route.getHostPort(), parameters, new NettyProtocol.NettyCodec(), new NettyTransport(), new FixedThreadPool(), new MessageDispatcher());
        NettyProtocol protocol = new NettyProtocol(transportContext, requestHandler);
        protocol.bind();
        Client client = protocol.getClient();
        ManagedClient client0 = new ManageClientImpl(client);
        conn.bind(client0);
    }


    /**
     * 消息处理
     */
    private final ExchangeHandler requestHandler = new ExchangeHandlerAdapter() {
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
}
