package cn.cloudwalk.smartframework.clientcomponents.netty;

import cn.cloudwalk.smartframework.transportcomponents.ChannelHandler;
import cn.cloudwalk.smartframework.transportcomponents.Client;
import cn.cloudwalk.smartframework.transportcomponents.Server;
import cn.cloudwalk.smartframework.transportcomponents.Transport;
import cn.cloudwalk.smartframework.transportcomponents.support.ProtocolConstants;
import cn.cloudwalk.smartframework.transportcomponents.support.transport.TransportContext;

/**
 * NettyTransport
 *
 * @author LIYANHUI
 * @since 1.0.0
 */
public class NettyTransport implements Transport {

    @Override
    public Server bind(TransportContext transportContext, ChannelHandler handler) {
        transportContext = transportContext.addParameter(ProtocolConstants.SERVER_ACCEPTS, transportContext.getParameter(ProtocolConstants.NETTY_SERVER_ACCEPTS));
        transportContext = transportContext.addParameter(ProtocolConstants.DISRUPTOR_CONSUMER_POOL_NAME, "netty_channel_disruptor_consumer_pool");
        return new NettyServer(transportContext, handler);
    }

    @Override
    public Client connect(TransportContext transportContext, ChannelHandler handler) {
        transportContext = transportContext
                .addParameter(ProtocolConstants.CLIENT_CONNECT_TIME, transportContext.getParameter(ProtocolConstants.RPC_CLIENT_CONNECT_TIME))
                .addParameter(ProtocolConstants.CLIENT_CONNECT_TIMEOUT, transportContext.getParameter(ProtocolConstants.RPC_CLIENT_CONNECT_TIMEOUT));
        return new NettyClient(transportContext, handler);
    }
}
