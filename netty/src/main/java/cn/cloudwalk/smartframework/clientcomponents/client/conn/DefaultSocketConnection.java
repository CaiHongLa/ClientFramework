package cn.cloudwalk.smartframework.clientcomponents.client.conn;

import cn.cloudwalk.smartframework.clientcomponents.core.ManagedClientConnection;
import cn.cloudwalk.smartframework.transport.Client;
import cn.cloudwalk.smartframework.transport.support.transport.TransportException;

import java.io.IOException;
import java.net.InetSocketAddress;

public class DefaultSocketConnection implements ManagedClientConnection {

    private Client client;
    private final String id;

    public DefaultSocketConnection(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void bind(Client client) throws IOException {
        this.client = client;
    }

    @Override
    public Client getClient() {
        return client;
    }

    @Override
    public void sendRequest(Object request) throws IOException {
        try {
            client.send(request);
        } catch (TransportException e) {
            e.printStackTrace();
        }
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return client.getLocalAddress();
    }

    @Override
    public int getLocalPort() {
        return client.getLocalAddress().getPort();
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return client.getRemoteAddress();
    }

    @Override
    public int getRemotePort() {
        return client.getRemoteAddress().getPort();
    }

    @Override
    public void close() throws IOException {
        if(client != null) {
            client.close();
        }
    }

    @Override
    public boolean isOpen() {
        if(client == null){
            return false;
        }
        return client.isConnected();
    }

    @Override
    public boolean isStale() {
        return client.isClosed();
    }

    @Override
    public void shutdown() throws IOException {
        close();
    }
}
