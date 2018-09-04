package cn.cloudwalk.smartframework.clientcomponents.tcp.message.enums;

/**
 * 消息标志枚举
 *
 * @author LIYANHUI
 * @since 1.0.0
 */
public enum Sign {

    /**
     * 未知消息
     */
    UNKNOWN() {
        @Override
        public byte byteValue() {
            return 0;
        }

        @Override
        public String stringValue() {
            return "0";
        }

        @Override
        public String description() {
            return "未知消息";
        }
    },

    /**
     * 设备注册
     */
    DEVICE_REG() {
        @Override
        public byte byteValue() {
            return 1;
        }

        @Override
        public String stringValue() {
            return "1";
        }

        @Override
        public String description() {
            return "设备注册";
        }
    },

    /**
     * 心跳消息
     */
    HEART_BEAT() {
        @Override
        public byte byteValue() {
            return 2;
        }

        @Override
        public String stringValue() {
            return "2";
        }

        @Override
        public String description() {
            return "心跳消息";
        }
    },

    /**
     * 人员下发
     */
    PEOPLE_PUT_DOWN() {
        @Override
        public byte byteValue() {
            return 3;
        }

        @Override
        public String stringValue() {
            return "3";
        }

        @Override
        public String description() {
            return "人员下发";
        }
    },

    /**
     * 访客机添加访客
     */
    VISITOR_ADD() {
        @Override
        public byte byteValue() {
            return 4;
        }

        @Override
        public String stringValue() {
            return "4";
        }

        @Override
        public String description() {
            return "访客机添加访客";
        }
    },

    /**
     * 告警消息下发
     */
    ALARM_PUT_DOWN() {
        @Override
        public byte byteValue() {
            return 5;
        }

        @Override
        public String stringValue() {
            return "5";
        }

        @Override
        public String description() {
            return "告警消息下发";
        }
    };

    /**
     * 获取byte值
     *
     * @return byte value
     */
    public abstract byte byteValue();

    /**
     * 获取String值
     *
     * @return String value
     */
    public abstract String stringValue();

    /**
     * 获取描述
     *
     * @return 描述
     */
    public abstract String description();

    @Override
    public String toString() {
        return "Sign{" +
                "value=" + this.stringValue() +
                "，description=" + this.description() +
                "}";
    }
}
