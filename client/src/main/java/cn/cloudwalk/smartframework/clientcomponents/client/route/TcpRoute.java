package cn.cloudwalk.smartframework.clientcomponents.client.route;

import java.net.InetSocketAddress;

public class TcpRoute implements Route, Cloneable {
    private final InetSocketAddress localAddress;

    private final InetSocketAddress targetAddress;

    public TcpRoute(final InetSocketAddress target, final InetSocketAddress local) {
        if(target == null){
            throw new IllegalArgumentException("target == null");
        }
        this.targetAddress = target;
        this.localAddress = local;
    }
    @Override
    public InetSocketAddress getTargetAddress() {
        return localAddress;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return targetAddress;
    }
}
