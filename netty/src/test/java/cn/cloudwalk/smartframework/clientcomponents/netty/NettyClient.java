package cn.cloudwalk.smartframework.clientcomponents.netty;

import cn.cloudwalk.smartframework.transport.AbstractClient;
import cn.cloudwalk.smartframework.transport.ChannelHandler;
import cn.cloudwalk.smartframework.transport.support.transport.TransportContext;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * Netty客户端
 *
 * @author LIYANHUI
 * @since 1.0.0
 */
public class NettyClient extends AbstractClient {

    private static final Logger logger = LogManager.getLogger(NettyClient.class);

    private Bootstrap bootstrap;
    private volatile Channel channel;
    private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors() + 1;

    public NettyClient(final TransportContext transportContext, final ChannelHandler handler) {
        super(transportContext, AbstractClient.wrapChannelHandler(transportContext, handler));
    }

    @Override
    protected void doOpen() throws Throwable {
        final NettyClientHandler nettyClientHandler = new NettyClientHandler(getTransportContext(), this);
        bootstrap = new Bootstrap();
        if (Epoll.isAvailable()) {
            bootstrap.group(new EpollEventLoopGroup(AVAILABLE_PROCESSORS, new DefaultThreadFactory("NettyEpollClientWorker", true)));
            bootstrap.channel(EpollSocketChannel.class);
        } else {
            bootstrap.group(new NioEventLoopGroup(AVAILABLE_PROCESSORS, new DefaultThreadFactory("NettyNioClientWorker", true)));
            bootstrap.channel(NioSocketChannel.class);
        }
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

        if (getTimeout() < 3000) {
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000);
        } else {
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, getTimeout());
        }

        bootstrap.handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                NettyCodecAdapter adapter = new NettyCodecAdapter(getTransportContext(), getCodec(), NettyClient.this);
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast("decoder", adapter.getDecoder())
                        .addLast("encoder", adapter.getEncoder())
                        .addLast("handler", nettyClientHandler);
            }
        });
    }

    @Override
    protected void doConnect() throws Throwable {
        ChannelFuture future = bootstrap.connect(getConnectAddress());
        try {
            boolean ret = future.awaitUninterruptibly(getConnectTimeout(), TimeUnit.MILLISECONDS);

            if (ret && future.isSuccess()) {
                Channel newChannel = future.channel();
                try {
                    Channel oldChannel = NettyClient.this.channel;
                    if (oldChannel != null) {
                        try {
                            if (logger.isInfoEnabled()) {
                                logger.info("close old channel：" + oldChannel + "，create new channel：" + newChannel);
                            }
                            oldChannel.close();
                        } finally {
                            NettyChannel.removeChannelIfDisconnected(oldChannel);
                        }
                    }
                } finally {
                    if (NettyClient.this.isClosed()) {
                        try {
                            logger.warn("close new channel： " + newChannel + ", because client is closed！");
                            newChannel.close();
                        } finally {
                            NettyClient.this.channel = null;
                            NettyChannel.removeChannelIfDisconnected(newChannel);
                        }
                    } else {
                        NettyClient.this.channel = newChannel;
                    }
                }
            } else if (future.cause() != null) {
                throw future.cause();
            } else {
            }
        } finally {
        }
    }

    @Override
    protected void doDisConnect() throws Throwable {
        try {
            NettyChannel.removeChannelIfDisconnected(channel);
        } catch (Throwable t) {
            logger.warn(t.getMessage());
        }
    }

    @Override
    protected void doClose() throws Throwable {
        //不能关闭nioEventLoopGroup，会导致建立连接的时候发生Reject异常，Event Loop Shut Down？
//        if (nioEventLoopGroup != null) {
//            nioEventLoopGroup.shutdownGracefully();
//        }
    }

    @Override
    protected cn.cloudwalk.smartframework.transport.Channel getChannel() {
        Channel c = channel;
        if (c == null || !c.isActive()) {
            return null;
        }
        return NettyChannel.getOrAddChannel(getTransportContext(), c, this);
    }
}