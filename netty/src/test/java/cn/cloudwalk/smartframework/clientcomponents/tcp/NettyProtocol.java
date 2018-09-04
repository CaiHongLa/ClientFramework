package cn.cloudwalk.smartframework.clientcomponents.tcp;

import cn.cloudwalk.smartframework.clientcomponents.tcp.message.NettyMessage;
import cn.cloudwalk.smartframework.common.util.TextUtil;
import cn.cloudwalk.smartframework.transport.AbstractProtocol;
import cn.cloudwalk.smartframework.transport.Channel;
import cn.cloudwalk.smartframework.transport.Client;
import cn.cloudwalk.smartframework.transport.Codec;
import cn.cloudwalk.smartframework.transport.exchange.ExchangeHandler;
import cn.cloudwalk.smartframework.transport.exchange.support.Exchangers;
import cn.cloudwalk.smartframework.transport.support.ProtocolConstants;
import cn.cloudwalk.smartframework.transport.support.transport.TransportContext;
import io.netty.buffer.ByteBuf;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Netty服务协议
 *
 * @author LIYANHUI
 * @since 1.0.0
 */
public class NettyProtocol extends AbstractProtocol {

    private Client client;
    private TransportContext transportContext;
    private ExchangeHandler requestHandler;

    public NettyProtocol(TransportContext transportContext, ExchangeHandler requestHandler) {
        this.transportContext = transportContext;
        this.transportContext = this.transportContext
                .addParameter(ProtocolConstants.EXCHANGE_HEART_BEAT_TIME, this.transportContext.getParameter(ProtocolConstants.NETTY_EXCHANGE_HEART_BEAT_TIME))
                .addParameter(ProtocolConstants.EXCHANGE_HEART_BEAT_TIMEOUT, this.transportContext.getParameter(ProtocolConstants.NETTY_EXCHANGE_HEART_BEAT_TIMEOUT))
                .addParameter(ProtocolConstants.FIXED_THREAD_POOL_CORE_SIZE, this.transportContext.getParameter(ProtocolConstants.NETTY_FIXED_THREAD_POOL_CORE_SIZE))
                .addParameter(ProtocolConstants.FIXED_THREAD_POOL_QUEUE_SIZE, this.transportContext.getParameter(ProtocolConstants.NETTY_FIXED_THREAD_POOL_QUEUE_SIZE))
                .addParameter(ProtocolConstants.DISRUPTOR_SWITCH, this.transportContext.getParameter(ProtocolConstants.NETTY_DISRUPTOR_SWITCH));
        this.requestHandler = requestHandler;
    }

    @Override
    public int getDefaultPort() {
        return transportContext.getPort();
    }

    @Override
    public void bind() {
        if (client == null) {
            client = Exchangers.connect(transportContext, requestHandler);
        } else {
            client.reset(transportContext);
        }
    }

    public Client getClient(){
        return client;
    }


    @Override
    public Codec getChannelCodec() {
        return transportContext.getCodec();
    }

    public static class NettyCodec implements Codec {
        private static final Logger logger = LogManager.getLogger(NettyCodec.class);

        @Override
        public void decode(Channel channel, ByteBuf in, List<Object> out) {
            while (in.readableBytes() >= NettyMessage.NettyMessageHeader.PROTOCOL_HEADER_LENGTH) {
                in.markReaderIndex();
                byte startSign_7E = in.readByte(); //7E 1
                byte startSign_E7 = in.readByte(); //E7 1
                if (startSign_7E == NettyMessage.NettyMessageHeader.startSign[0] &&
                        startSign_E7 == NettyMessage.NettyMessageHeader.startSign[1]) {
                    logger.debug("Read the message start flag! Start decoding！");
                    long timeStamp = in.readLong(); //timeStamp 8
                    byte type = in.readByte(); //type 1
                    byte sign = in.readByte(); //sign 1
                    int length = in.readInt(); //length 4
                    if (in.readableBytes() < length) {
                        //不够组包 继续等待
                        in.resetReaderIndex();
                        return;
                    }
                    NettyMessage protocol = new NettyMessage();
                    if (length > 0) {
                        ByteBuf bodyByteBuf = in.readBytes(length);
                        byte[] array = new byte[length];
                        bodyByteBuf.getBytes(bodyByteBuf.readerIndex(), array, 0, length);
                        String body = StringUtils.newStringUtf8(array);
                        protocol.setProtocolBody(body);
                        bodyByteBuf.release();
                    }
                    NettyMessage.NettyMessageHeader header = new NettyMessage.NettyMessageHeader();
                    header.setSign(sign);
                    header.setType(type);
                    header.setTimeStamp(timeStamp);
                    protocol.setProtocolHeader(header);
                    out.add(protocol);
                    logger.debug("Message decoding completed！");
                } else {
                    //需要丢弃消息（消息头错误的消息视为无效消息） 否则永远读不到下一条消息
                    logger.debug("Unread the message start flag! Starting to discard invalid messages!");
                    in.readBytes(10); //错误消息跳过某些字段 直接读取消息体长度
                    int length = in.readInt(); //length 4
                    if (in.readableBytes() < length) {
                        //不够组包 继续等待
                        in.resetReaderIndex();
                        return;
                    }
                    in.readBytes(length); //不需要组包
                    logger.debug("Discard invalid message completion!");
                }
            }
        }

        @Override
        public void encode(Channel channel, ByteBuf byteBuf, Object msg) {
            NettyMessage protocol = (NettyMessage) msg;
            NettyMessage.NettyMessageHeader header = protocol.getProtocolHeader();
            byteBuf.writeByte(NettyMessage.NettyMessageHeader.startSign[0]);
            byteBuf.writeByte(NettyMessage.NettyMessageHeader.startSign[1]);
            byteBuf.writeLong(header.getTimeStamp());
            byteBuf.writeByte(header.getType());
            byteBuf.writeByte(header.getSign());
            String body = protocol.getProtocolBody();
            if (TextUtil.isNotEmpty(body)) {
                byte[] bytes = StringUtils.getBytesUtf8(body);
                byteBuf.writeInt(bytes.length); //消息体长度在这里写入
                byteBuf.writeBytes(bytes);
            } else {
                byteBuf.writeInt(0);
            }
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (client != null) {
            client.close();
        }
    }
}
