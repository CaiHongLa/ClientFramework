package cn.cloudwalk.smartframework.clientcomponents;

import cn.cloudwalk.smartframework.clientcomponents.core.ManagedClient;
import cn.cloudwalk.smartframework.clientcomponents.core.util.Args;
import cn.cloudwalk.smartframework.transport.Client;
import cn.cloudwalk.smartframework.transport.support.transport.TransportException;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * @since 1.0.0
 */
public class ManageClientImpl implements ManagedClient {

    private final Client client;

    public ManageClientImpl(Client client) {
        this.client = client;
    }

    @Override
    public void send(Object request) throws IOException {
        Args.notNull(client, "client");
        try {
            client.send(request, true);
        } catch (TransportException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isClosed() {
        if (null == client) {
            return false;
        }
        return client.isClosed();
    }

    @Override
    public boolean isConnected() {
        if (null == client) {
            return false;
        }
        return client.isConnected();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        Args.notNull(client, "client");
        return client.getLocalAddress();
    }

    @Override
    public int getLocalPort() {
        return getLocalAddress().getPort();
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        Args.notNull(client, "client");
        return client.getRemoteAddress();
    }

    @Override
    public int getRemotePort() {
        return getRemoteAddress().getPort();
    }

    @Override
    public void close() throws IOException {
        if (null != client) {
            client.close();
        }
    }
}
