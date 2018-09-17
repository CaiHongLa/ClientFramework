package cn.cloudwalk.smartframework.clientcomponents.netty;

import cn.cloudwalk.smartframework.transportcomponents.AbstractServer;
import cn.cloudwalk.smartframework.transportcomponents.Channel;
import cn.cloudwalk.smartframework.transportcomponents.ChannelHandler;
import cn.cloudwalk.smartframework.transportcomponents.Server;
import cn.cloudwalk.smartframework.transportcomponents.support.ChannelHandlers;
import cn.cloudwalk.smartframework.transportcomponents.support.ProtocolConstants;
import cn.cloudwalk.smartframework.transportcomponents.support.transport.TransportContext;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

/**
 * NettyServer
 *
 * @author LIYANHUI
 * @since 1.0.0
 */
public class NettyServer extends AbstractServer implements Server {

    private static final Logger logger = LogManager.getLogger(NettyServer.class);
    private Map<String, Channel> channels;
    private ServerBootstrap bootstrap;
    private io.netty.channel.Channel channel;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors() + 1;

    public NettyServer(TransportContext transportContext, ChannelHandler handler) {
        super(transportContext, ChannelHandlers.wrapServer(transportContext, handler));
    }

    @Override
    protected void doOpen() {
        bootstrap = new ServerBootstrap();
        if (Epoll.isAvailable()) {
            bossGroup = new EpollEventLoopGroup(getTransportContext().getParameter(ProtocolConstants.NETTY_SERVER_BOSS_THREAD_SIZE, AVAILABLE_PROCESSORS),
                    new DefaultThreadFactory("NettyEpollServerBoss", true));
            workerGroup = new EpollEventLoopGroup(getTransportContext().getParameter(ProtocolConstants.NETTY_SERVER_WORKER_THREAD_SIZE, AVAILABLE_PROCESSORS),
                    new DefaultThreadFactory("NettyEpollServerWorker", true));
            bootstrap.channel(EpollServerSocketChannel.class);
        } else {
            bossGroup = new NioEventLoopGroup(getTransportContext().getParameter(ProtocolConstants.NETTY_SERVER_BOSS_THREAD_SIZE, AVAILABLE_PROCESSORS),
                    new DefaultThreadFactory("NettyNioServerBoss", true));
            workerGroup = new NioEventLoopGroup(getTransportContext().getParameter(ProtocolConstants.NETTY_SERVER_WORKER_THREAD_SIZE, AVAILABLE_PROCESSORS),
                    new DefaultThreadFactory("NettyNioServerWorker", true));
            bootstrap.channel(NioServerSocketChannel.class);
        }
        final NettyServerHandler nettyServerHandler = new NettyServerHandler(getTransportContext(), this);
        channels = nettyServerHandler.getChannels();
        bootstrap.group(bossGroup, workerGroup)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_RCVBUF, 32 * 1024)
                .option(ChannelOption.SO_SNDBUF, 32 * 1024)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childHandler(new ChannelInitializer<io.netty.channel.Channel>() {
                    @Override
                    protected void initChannel(io.netty.channel.Channel ch) throws Exception {
                        NettyCodecAdapter adapter = new NettyCodecAdapter(getTransportContext(), getCodec(), NettyServer.this);
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("pre_decoder", new LengthFieldBasedFrameDecoder(4 * 1024 * 1024 , 14 , 4))
                                .addLast("decoder", adapter.getDecoder())
                                .addLast("encoder", adapter.getEncoder())
                                .addLast("handler", nettyServerHandler);
                    }
                });

        ChannelFuture channelFuture = bootstrap.bind(getBindAddress());
        channelFuture.syncUninterruptibly();
        channel = channelFuture.channel();
    }

    @Override
    protected void doClose() throws Throwable {
        try {
            if (channel != null) {
                channel.close();
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
        try {
            Collection<Channel> channels = getChannels();
            if (channels != null && channels.size() > 0) {
                for (Channel channel : channels) {
                    try {
                        channel.close();
                    } catch (Throwable e) {
                        logger.warn(e.getMessage(), e);
                    }
                }
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
        try {
            if (bootstrap != null) {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
        try {
            if (channels != null) {
                channels.clear();
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
    }

    @Override
    public boolean isBind() {
        return channel.isActive();
    }

    @Override
    public Collection<Channel> getChannels() {
        Collection<Channel> chs = new HashSet<>();
        for (Channel channel : this.channels.values()) {
            if (channel.isConnected()) {
                chs.add(channel);
            } else {
                channels.remove(channel.getRemoteAddress().toString());
            }
        }
        return chs;
    }

    @Override
    public Channel getChannel(InetSocketAddress inetSocketAddress) {
        return channels.get(inetSocketAddress.toString());
    }
}