package cn.cloudwalk.smartframework.clientcomponents.core.config;

import java.net.InetAddress;

public class RequestConfig implements Cloneable {

    public static final RequestConfig DEFAULT = new Builder().build();

    private final InetAddress localAddress;
    private final boolean staleConnectionCheckEnabled;
    private final int connectionRequestTimeout;
    private final int connectTimeout;
    private final int socketTimeout;

    protected RequestConfig() {
        this(null, false, 0, 0, 0);
    }

    RequestConfig(
            final InetAddress localAddress,
            final boolean staleConnectionCheckEnabled,
            final int connectionRequestTimeout,
            final int connectTimeout,
            final int socketTimeout) {
        super();
        this.localAddress = localAddress;
        this.staleConnectionCheckEnabled = staleConnectionCheckEnabled;
        this.connectionRequestTimeout = connectionRequestTimeout;
        this.connectTimeout = connectTimeout;
        this.socketTimeout = socketTimeout;
    }


    public InetAddress getLocalAddress() {
        return localAddress;
    }

    public boolean isStaleConnectionCheckEnabled() {
        return staleConnectionCheckEnabled;
    }


    public int getConnectionRequestTimeout() {
        return connectionRequestTimeout;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }


    @Override
    protected RequestConfig clone() throws CloneNotSupportedException {
        return (RequestConfig) super.clone();
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append(", localAddress=").append(localAddress);
        builder.append(", connectionRequestTimeout=").append(connectionRequestTimeout);
        builder.append(", connectTimeout=").append(connectTimeout);
        builder.append(", socketTimeout=").append(socketTimeout);
        builder.append("]");
        return builder.toString();
    }

    public static RequestConfig.Builder custom() {
        return new Builder();
    }

    @SuppressWarnings("deprecation")
    public static RequestConfig.Builder copy(final RequestConfig config) {
        return new Builder()
                .setLocalAddress(config.getLocalAddress())
                .setStaleConnectionCheckEnabled(config.isStaleConnectionCheckEnabled())
                .setConnectionRequestTimeout(config.getConnectionRequestTimeout())
                .setConnectTimeout(config.getConnectTimeout())
                .setSocketTimeout(config.getSocketTimeout());
    }

    public static class Builder {

        private InetAddress localAddress;
        private boolean staleConnectionCheckEnabled;
        private int connectionRequestTimeout;
        private int connectTimeout;
        private int socketTimeout;

        Builder() {
            super();
            this.staleConnectionCheckEnabled = false;
            this.connectionRequestTimeout = -1;
            this.connectTimeout = -1;
            this.socketTimeout = -1;
        }


        public Builder setLocalAddress(final InetAddress localAddress) {
            this.localAddress = localAddress;
            return this;
        }

        public Builder setStaleConnectionCheckEnabled(final boolean staleConnectionCheckEnabled) {
            this.staleConnectionCheckEnabled = staleConnectionCheckEnabled;
            return this;
        }

        public Builder setConnectionRequestTimeout(final int connectionRequestTimeout) {
            this.connectionRequestTimeout = connectionRequestTimeout;
            return this;
        }

        public Builder setConnectTimeout(final int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder setSocketTimeout(final int socketTimeout) {
            this.socketTimeout = socketTimeout;
            return this;
        }

        public RequestConfig build() {
            return new RequestConfig(
                    localAddress,
                    staleConnectionCheckEnabled,
                    connectionRequestTimeout,
                    connectTimeout,
                    socketTimeout);
        }

    }

}
