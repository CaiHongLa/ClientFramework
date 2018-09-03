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

import cn.cloudwalk.smartframework.clientcomponents.client.conn.ManagedTcpClientConnection;
import cn.cloudwalk.smartframework.clientcomponents.client.route.TcpRoute;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @since 4.3
 */
public class CPool extends AbstractConnPool<TcpRoute, ManagedTcpClientConnection, CPoolEntry> {

    private static final AtomicLong COUNTER = new AtomicLong();

    private final long timeToLive;
    private final TimeUnit tunit;

    public CPool(
            final ConnFactory<TcpRoute, ManagedTcpClientConnection> connFactory,
            final int defaultMaxPerRoute, final int maxTotal,
            final long timeToLive, final TimeUnit tunit) {
        super(connFactory, defaultMaxPerRoute, maxTotal);
        this.timeToLive = timeToLive;
        this.tunit = tunit;
    }

    @Override
    protected CPoolEntry createEntry(final TcpRoute route, final ManagedTcpClientConnection conn) {
        final String id = Long.toString(COUNTER.getAndIncrement());
        return new CPoolEntry(id, route, conn, this.timeToLive, this.tunit);
    }

    @Override
    protected boolean validate(final CPoolEntry entry) {
        return !entry.getConnection().isStale();
    }

}
