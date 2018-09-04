package cn.cloudwalk.smartframework.clientcomponents.tcp.message.enums;

/**
 * 消息Type枚举
 *
 * @author LIYANHUI
 * @since 1.0.0
 */
public enum Type {

    /**
     * 文本消息
     */
    TEXT((byte) 1);

    byte value;

    Type(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Type{" +
                "value=" + value +
                "} ";
    }
}
