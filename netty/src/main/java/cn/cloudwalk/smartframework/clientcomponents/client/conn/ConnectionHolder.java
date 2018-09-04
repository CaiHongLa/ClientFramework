package cn.cloudwalk.smartframework.clientcomponents.client.conn;

import cn.cloudwalk.smartframework.clientcomponents.core.Cancellable;
import cn.cloudwalk.smartframework.clientcomponents.core.ClientConnection;
import cn.cloudwalk.smartframework.clientcomponents.core.ClientConnectionManager;
import cn.cloudwalk.smartframework.clientcomponents.core.ConnectionReleaseTrigger;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConnectionHolder implements ConnectionReleaseTrigger, Cancellable, Closeable {

    private final ClientConnectionManager manager;
    private final ClientConnection connection;
    private final AtomicBoolean released;
    private volatile boolean reusable;
    private volatile Object state;
    private volatile long validDuration;
    private volatile TimeUnit timeUnit;

    public ConnectionHolder(
            final ClientConnectionManager manager,
            final ClientConnection connection) {
        super();
        this.manager = manager;
        this.connection = connection;
        this.released = new AtomicBoolean(false);
    }

    public boolean isReusable() {
        return this.reusable;
    }

    public void markReusable() {
        this.reusable = true;
    }

    public void markNonReusable() {
        this.reusable = false;
    }

    public void setState(final Object state) {
        this.state = state;
    }

    public void setValidFor(final long duration, final TimeUnit timeUnit) {
        synchronized (this.connection) {
            this.validDuration = duration;
            this.timeUnit = timeUnit;
        }
    }

    private void releaseConnection(final boolean reusable) {
        if (this.released.compareAndSet(false, true)) {
            synchronized (this.connection) {
                if (reusable) {
                    this.manager.releaseConnection(this.connection, this.validDuration, this.timeUnit);
                } else {
                    try {
                        this.connection.close();
                    } catch (final IOException ex) {

                    } finally {
                        this.manager.releaseConnection(this.connection, 0, TimeUnit.MILLISECONDS);
                    }
                }
            }
        }
    }

    @Override
    public void releaseConnection() {
        releaseConnection(this.reusable);
    }

    @Override
    public void abortConnection() {
        if (this.released.compareAndSet(false, true)) {
            synchronized (this.connection) {
                try {
                    this.connection.shutdown();
                } catch (final IOException ex) {
                } finally {
                    this.manager.releaseConnection(this.connection, 0, TimeUnit.MILLISECONDS);
                }
            }
        }
    }

    @Override
    public boolean cancel() {
        final boolean alreadyReleased = this.released.get();
        abortConnection();
        return !alreadyReleased;
    }

    public boolean isReleased() {
        return this.released.get();
    }

    @Override
    public void close() throws IOException {
        releaseConnection(false);
    }

}
