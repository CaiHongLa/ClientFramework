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
import cn.cloudwalk.smartframework.clientcomponents.client.conn.ManagedTcpClientConnection;
import cn.cloudwalk.smartframework.clientcomponents.client.route.TcpRoute;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @since 4.3
 */
public class CPoolEntry extends PoolEntry<TcpRoute, ManagedTcpClientConnection> {

    private volatile boolean routeComplete;

    public CPoolEntry(
            final String id,
            final TcpRoute route,
            final ManagedTcpClientConnection conn,
            final long timeToLive, final TimeUnit tunit) {
        super(id, route, conn, timeToLive, tunit);
    }

    public void markRouteComplete() {
        this.routeComplete = true;
    }

    public boolean isRouteComplete() {
        return this.routeComplete;
    }

    public void closeConnection() throws IOException {
        final TcpClientConnection conn = getConnection();
        conn.close();
    }

    public void shutdownConnection() throws IOException {
        final TcpClientConnection conn = getConnection();
        conn.shutdown();
    }

    @Override
    public boolean isExpired(final long now) {
        final boolean expired = super.isExpired(now);
        return expired;
    }

    @Override
    public boolean isClosed() {
        final TcpClientConnection conn = getConnection();
        return !conn.isOpen();
    }

    @Override
    public void close() {
        try {
            closeConnection();
        } catch (final IOException ex) {
        }
    }

}
