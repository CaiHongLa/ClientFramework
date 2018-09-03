/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package cn.cloudwalk.smartframework.clientcomponents.client.pool;


import cn.cloudwalk.smartframework.clientcomponents.client.TcpClientConnection;
import cn.cloudwalk.smartframework.clientcomponents.client.conn.ConnectionShutdownException;
import cn.cloudwalk.smartframework.clientcomponents.client.conn.ManagedTcpClientConnection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @since 4.3
 */
public class CPoolProxy implements ManagedTcpClientConnection {

    private volatile CPoolEntry poolEntry;

    CPoolProxy(final CPoolEntry entry) {
        super();
        this.poolEntry = entry;
    }

    CPoolEntry getPoolEntry() {
        return this.poolEntry;
    }

    CPoolEntry detach() {
        final CPoolEntry local = this.poolEntry;
        this.poolEntry = null;
        return local;
    }

    ManagedTcpClientConnection getConnection() {
        final CPoolEntry local = this.poolEntry;
        if (local == null) {
            return null;
        }
        return local.getConnection();
    }

    ManagedTcpClientConnection getValidConnection() {
        final ManagedTcpClientConnection conn = getConnection();
        if (conn == null) {
            throw new ConnectionShutdownException();
        }
        return conn;
    }

    @Override
    public void close() throws IOException {
        final CPoolEntry local = this.poolEntry;
        if (local != null) {
            local.closeConnection();
        }
    }

    @Override
    public void shutdown() throws IOException {
        final CPoolEntry local = this.poolEntry;
        if (local != null) {
            local.shutdownConnection();
        }
    }

    @Override
    public boolean isOpen() {
        final CPoolEntry local = this.poolEntry;
        if (local != null) {
            return !local.isClosed();
        } else {
            return false;
        }
    }

    @Override
    public boolean isStale() {
        final TcpClientConnection conn = getConnection();
        if (conn != null) {
            return conn.isStale();
        } else {
            return true;
        }
    }

    @Override
    public void setSocketTimeout(final int timeout) {
        getValidConnection().setSocketTimeout(timeout);
    }

    @Override
    public int getSocketTimeout() {
        return getValidConnection().getSocketTimeout();
    }

    @Override
    public String getId() {
        return getValidConnection().getId();
    }

    @Override
    public void bind(final Socket socket) throws IOException {
        getValidConnection().bind(socket);
    }

    @Override
    public Socket getSocket() {
        return getValidConnection().getSocket();
    }


    @Override
    public boolean isResponseAvailable(final int timeout) throws IOException {
        return getValidConnection().isResponseAvailable(timeout);
    }

    @Override
    public void sendRequest(Object request) throws IOException {
        getValidConnection().sendRequest(request);
    }

    @Override
    public void flush() throws IOException {
        getValidConnection().flush();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return getValidConnection().getLocalAddress();
    }

    @Override
    public int getLocalPort() {
        return getValidConnection().getLocalPort();
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return getValidConnection().getRemoteAddress();
    }

    @Override
    public int getRemotePort() {
        return getValidConnection().getRemotePort();
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CPoolProxy{");
        final ManagedTcpClientConnection conn = getConnection();
        if (conn != null) {
            sb.append(conn);
        } else {
            sb.append("detached");
        }
        sb.append('}');
        return sb.toString();
    }

    public static TcpClientConnection newProxy(final CPoolEntry poolEntry) {
        return new CPoolProxy(poolEntry);
    }

    private static CPoolProxy getProxy(final TcpClientConnection conn) {
        if (!CPoolProxy.class.isInstance(conn)) {
            throw new IllegalStateException("Unexpected connection proxy class: " + conn.getClass());
        }
        return CPoolProxy.class.cast(conn);
    }

    public static CPoolEntry getPoolEntry(final TcpClientConnection proxy) {
        final CPoolEntry entry = getProxy(proxy).getPoolEntry();
        if (entry == null) {
            throw new ConnectionShutdownException();
        }
        return entry;
    }

    public static CPoolEntry detach(final TcpClientConnection conn) {
        return getProxy(conn).detach();
    }

}
