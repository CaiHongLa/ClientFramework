package cn.cloudwalk.smartframework.clientcomponents.netty;

import cn.cloudwalk.smartframework.transport.Codec;
import cn.cloudwalk.smartframework.transport.support.transport.TransportContext;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.List;

/**
 * NettyCodecAdapter
 *
 * @author LIYANHUI
 * @since 1.0.0
 */
public class NettyCodecAdapter {
    private final ChannelHandler encoder = new InternalEncoder();

    private final ChannelHandler decoder = new InternalDecoder();

    private final Codec codec;
    private final cn.cloudwalk.smartframework.transport.ChannelHandler handler;
    private TransportContext transportContext;

    public NettyCodecAdapter(TransportContext transportContext, Codec codec, cn.cloudwalk.smartframework.transport.ChannelHandler handler) {
        this.transportContext = transportContext;
        this.codec = codec;
        this.handler = handler;
    }

    public ChannelHandler getEncoder() {
        return encoder;
    }

    public ChannelHandler getDecoder() {
        return decoder;
    }

    private class InternalEncoder extends MessageToByteEncoder {

        @Override
        protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
            Channel ch = ctx.channel();
            NettyChannel channel = NettyChannel.getOrAddChannel(transportContext, ch, handler);
            try {
                codec.encode(channel, out, msg);
            } finally {
                NettyChannel.removeChannelIfDisconnected(ch);
            }
        }
    }

    private class InternalDecoder extends ByteToMessageDecoder {

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf input, List<Object> out) throws Exception {

            NettyChannel channel = NettyChannel.getOrAddChannel(transportContext, ctx.channel(), handler);
            try {
                codec.decode(channel, input, out);
            } finally {
                NettyChannel.removeChannelIfDisconnected(ctx.channel());
            }
        }
    }

}
