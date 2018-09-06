package cn.cloudwalk.smartframework.clientcomponents.core.config;

import java.util.HashMap;
import java.util.Map;

public class RequestConfig implements Cloneable {

    public static final RequestConfig DEFAULT = new Builder().build();

    private final int connectionRequestTimeout;
    private final int maxTotal;
    private final int maxPerRoute;
    private final int maxTimeToLive;
    private final Map<String, String> params;

    protected RequestConfig() {
        this(0, 5000, 5000, 60000, new HashMap<>());
    }

    RequestConfig(
            final int connectionRequestTimeout, int maxPerRoute, int maxTotal, int maxTimeToLive, Map<String, String> params) {
        super();
        this.connectionRequestTimeout = connectionRequestTimeout;
        this.params = params;
        this.maxPerRoute = maxPerRoute;
        this.maxTotal = maxTotal;
        this.maxTimeToLive = maxTimeToLive;
    }

    public int getConnectionRequestTimeout() {
        return connectionRequestTimeout;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public int getMaxTotal() {
        return maxTotal;
    }

    public int getMaxPerRoute() {
        return maxPerRoute;
    }

    public int getMaxTimeToLive() {
        return maxTimeToLive;
    }

    @Override
    protected RequestConfig clone() throws CloneNotSupportedException {
        return (RequestConfig) super.clone();
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append(", connectionRequestTimeout=").append(connectionRequestTimeout);
        builder.append(", params=").append(params);
        builder.append("]");
        return builder.toString();
    }

    public static RequestConfig.Builder custom() {
        return new Builder();
    }

    @SuppressWarnings("deprecation")
    public static RequestConfig.Builder copy(final RequestConfig config) {
        return new Builder()
                .setConnectionRequestTimeout(config.getConnectionRequestTimeout());
    }

    public static class Builder {

        private int connectionRequestTimeout;
        private int maxTotal;
        private int maxPerRoute;
        private int maxTimeToLive;
        private Map<String, String> params;

        Builder() {
            super();
            this.connectionRequestTimeout = -1;
            this.params = new HashMap<>();
        }


        public Builder setConnectionRequestTimeout(final int connectionRequestTimeout) {
            this.connectionRequestTimeout = connectionRequestTimeout;
            return this;
        }

        public Builder setMaxTotal(final int maxTotal){
            this.maxTotal = maxTotal;
            return this;
        }

        public Builder setMaxPerRoute(final int maxPerRoute){
            this.maxPerRoute = maxPerRoute;
            return this;
        }

        public Builder setMaxTimeToLive(final int maxTimeToLive){
            this.maxTimeToLive = maxTimeToLive;
            return this;
        }

        public Builder setParams(Map<String, String> params){
            this.params = params;
            return this;
        }

        public Builder addParams(String key, String value){
            this.params.put(key, value);
            return this;
        }

        public RequestConfig build() {
            return new RequestConfig(connectionRequestTimeout, maxPerRoute, maxTotal, maxTimeToLive, params);
        }

    }

}
