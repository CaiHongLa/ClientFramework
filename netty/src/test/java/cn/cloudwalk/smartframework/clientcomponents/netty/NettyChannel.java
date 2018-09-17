package cn.cloudwalk.smartframework.clientcomponents.netty;

import cn.cloudwalk.smartframework.clientcomponents.Count;
import cn.cloudwalk.smartframework.transportcomponents.AbstractChannel;
import cn.cloudwalk.smartframework.transportcomponents.ChannelHandler;
import cn.cloudwalk.smartframework.transportcomponents.support.transport.TransportContext;
import cn.cloudwalk.smartframework.transportcomponents.support.transport.TransportException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 传输层Netty连接
 *
 * @author LIYANHUI
 * @since 1.0.0
 */
public class NettyChannel extends AbstractChannel {

    private static final Logger logger = LogManager.getLogger(NettyChannel.class);

    private static final ConcurrentMap<Channel, NettyChannel> CHANNEL_MAP = new ConcurrentHashMap<>();

    private final Channel channel;

    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    private NettyChannel(TransportContext transportContext, Channel channel, ChannelHandler handler) {
        super(transportContext, handler);
        this.channel = channel;
    }

    public static NettyChannel getOrAddChannel(TransportContext transportContext, Channel ch, ChannelHandler handler) {
        if (ch == null) {
            return null;
        }
        NettyChannel ret = CHANNEL_MAP.get(ch);
        if (ret == null) {
            NettyChannel nettyChannel = new NettyChannel(transportContext, ch, handler);
            if (ch.isActive()) {
                ret = CHANNEL_MAP.putIfAbsent(ch, nettyChannel);
            }
            if (ret == null) {
                ret = nettyChannel;
            }
        }
        return ret;
    }

    public static void removeChannelIfDisconnected(Channel ch) {
        if (ch != null && !ch.isActive()) {
            CHANNEL_MAP.remove(ch);
        }
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return (InetSocketAddress) channel.localAddress();
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return (InetSocketAddress) channel.remoteAddress();
    }

    @Override
    public boolean isConnected() {
        return !isClosed() && channel.isActive();
    }

    @Override
    public void send(Object message, boolean sent) throws TransportException {
        super.send(message, sent);

        final int timeout = 10 * 1000;
        ChannelFuture future;
        try {
            future = channel.writeAndFlush(message);
            if (sent) {
                future.addListener((ChannelFutureListener) future1 -> {
                    boolean success = future1.await(timeout);
                    if (!success) {
                        logger.error("send error");
                    } else {
                        Count.count.countDown();
                    }
                });
            }
            Throwable cause = future.cause();
            if (cause != null) {
                throw cause;
            }
        } catch (Throwable e) {
            throw new TransportException(this, "send message：" + message + " to " + getRemoteAddress() + ", error: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        try {
            super.close();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        try {
            removeChannelIfDisconnected(channel);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        try {
            attributes.clear();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        try {
            logger.info("close channel：" + channel);
            channel.close();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }

    @Override
    public void reset(TransportContext transportContext) {

    }

    @Override
    public boolean hasAttribute(String key) {
        return attributes.containsKey(key);
    }

    @Override
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    @Override
    public void setAttribute(String key, Object value) {
        if (value == null) {
            attributes.remove(key);
        } else {
            attributes.put(key, value);
        }
    }

    @Override
    public void removeAttribute(String key) {
        attributes.remove(key);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((channel == null) ? 0 : channel.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        NettyChannel other = (NettyChannel) obj;
        if (channel == null) {
            if (other.channel != null) {
                return false;
            }
        } else if (!channel.equals(other.channel)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "NettyChannel [channel=" + channel + "]";
    }
}
