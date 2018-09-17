package cn.cloudwalk.smartframework.clientcomponents.tcp.message;

/**
 * 自有协议栈
 *
 * @author LIYANHUI
 * @since 1.0.0
 */
public class NettyMessage {

    /**
     * 消息体
     */
    private String protocolBody;

    /**
     * 消息头
     */
    private NettyMessageHeader protocolHeader;

    public String getProtocolBody() {
        return protocolBody;
    }

    public void setProtocolBody(String protocolBody) {
        this.protocolBody = protocolBody;
    }

    public NettyMessageHeader getProtocolHeader() {
        return protocolHeader;
    }

    public void setProtocolHeader(NettyMessageHeader protocolHeader) {
        this.protocolHeader = protocolHeader;
    }

    @Override
    public String toString() {
        return "NettyProtocol{" +
                "protocolBody='" + protocolBody + '\'' +
                ", protocolHeader=" + protocolHeader +
                "} ";
    }

    /**
     * 框架自有协议栈的头部
     * <p>
     * <p>
     * 每个消息的消息头由16个字节来表示，每条完整的消息都应该具有正确  的消息头，具体定义如下：
     * <pre>
     *   +------------------------------------------------------------------------------------------------------+
     *   |    字段名称     |   含义           |  字段长度（Byte）|                      说明                       |
     *   +------------------------------------------------------------------------------------------------------+
     *   |   startSign    | 开始标志          |         2      |     固定为0x7EE7 用于跟踪调试使用，代表一条消息的开始|
     *   |   timeStamp    | 时间戳（单位：秒） |         8      |      消息头生成时间 代表产生消息头的时间信息         |
     *   |   type         | 消息类型          |         4      |      消息的类型 文件、文本等                      |
     *   |   sign         | 消息标志          |         1      |      消息的标志 注册、激活、心跳等                 |
     *   |   length       | 消息体长度        |         1      |      Body部分的总字节长度 消息体的字节长度          |
     *   +------------------------------------------------------------------------------------------------------+
     * </pre>
     *
     * @author LIYANHUI
     * @since 1.0.0
     */
    public static final class NettyMessageHeader {

        /**
         * 头部总长度16byte
         */
        public static final int PROTOCOL_HEADER_LENGTH = 16;

        /**
         * 头部开始标志，0X7EE7
         */
        public static final byte[] startSign = new byte[]{
                Byte.parseByte("7E", 16), (byte) Integer.parseInt("E7", 16)
        };

        /**
         * 消息头产生的时间戳
         */
        private long timeStamp = System.currentTimeMillis();

        /**
         * 消息类型
         */
        private byte type;

        /**
         * 消息标志，标志消息属于什么模块
         */
        private byte sign;

        public byte getType() {
            return type;
        }

        public void setType(byte type) {
            this.type = type;
        }

        public byte getSign() {
            return sign;
        }

        public void setSign(byte sign) {
            this.sign = sign;
        }

        public long getTimeStamp() {
            return timeStamp;
        }

        public void setTimeStamp(long timeStamp) {
            this.timeStamp = timeStamp;
        }

    }
}
