package cn.cloudwalk.smartframework.clientcomponents.client;

import cn.cloudwalk.smartframework.clientcomponents.core.Route;
import cn.cloudwalk.smartframework.clientcomponents.core.util.Args;
import cn.cloudwalk.smartframework.clientcomponents.core.util.LangUtils;

public class TcpRoute implements Route, Cloneable {

    private String ip;

    private int port;

    public TcpRoute(){}

    public TcpRoute(final String ip, final int port){
        Args.notNull(ip, "ip");
        Args.check(port > 0, "port may less than 0");
        this.ip = ip;
        this.port = port;
    }

    public void setHostIp(String ip) {
        this.ip = ip;
    }

    public void setHostPort(int port) {
        this.port = port;
    }

    @Override
    public String getHostIp() {
        return ip;
    }

    @Override
    public int getHostPort() {
        return port;
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof TcpRoute) {
            final TcpRoute that = (TcpRoute) obj;
            return LangUtils.equals(this.ip, that.ip) && LangUtils.equals(this.port, that.port);
        } else {
            return false;
        }
    }

    /**
     * Generates a hash code for this route.
     *
     * @return the hash code
     */
    @Override
    public final int hashCode() {
        int hash = LangUtils.HASH_SEED;
        hash = LangUtils.hashCode(hash, this.ip);
        hash = LangUtils.hashCode(hash, this.port);
        return hash;
    }

    /**
     * Obtains a description of this route.
     *
     * @return a human-readable representation of this route
     */
    @Override
    public final String toString() {
        return this.ip + "/" + this.port;
    }
}
