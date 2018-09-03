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

package cn.cloudwalk.smartframework.clientcomponents.client;

import cn.cloudwalk.smartframework.clientcomponents.client.config.RequestConfig;
import cn.cloudwalk.smartframework.clientcomponents.client.conn.TcpClientConnectionManager;
import cn.cloudwalk.smartframework.clientcomponents.client.route.TcpRoute;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * Internal class.
 *
 * @since 4.3
 */
@SuppressWarnings("deprecation")
class InternalHttpClient extends CloseableTcpClient {


    private final ClientExecChain execChain;
    private final TcpClientConnectionManager connManager;
    private final RequestConfig defaultConfig;
    private final List<Closeable> closeables;


    public InternalHttpClient(
            final ClientExecChain execChain,
            final TcpClientConnectionManager connManager,
            final RequestConfig defaultConfig,
            final List<Closeable> closeables) {
        super();
        Args.notNull(execChain, "HTTP client exec chain");
        Args.notNull(connManager, "HTTP connection manager");
        this.execChain = execChain;
        this.connManager = connManager;
        this.defaultConfig = defaultConfig;
        this.closeables = closeables;
    }

    private TcpRoute determineRoute(
            final InetSocketAddress target,
            final Object request) {
        InetSocketAddress host = target;
        return new TcpRoute(host, null);
    }


    @Override
    protected Object doExecute(
            final InetSocketAddress target,
            final Object request) throws IOException {
        Args.notNull(request, "HTTP request");
        final TcpRoute route = determineRoute(target, request);
        return this.execChain.execute(route, request);
    }

    @Override
    public void close() {
        if (this.closeables != null) {
            for (final Closeable closeable: this.closeables) {
                try {
                    closeable.close();
                } catch (final IOException ex) {
                }
            }
        }
    }

}
