package org.reallylastone.stock_collector.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ConnectionPool {
    private final BlockingQueue<Connection> pool;
    private final int poolSize;

    public ConnectionPool(String url, String user, String password, int size) throws SQLException {
        this.poolSize = size;
        this.pool = new LinkedBlockingQueue<>(size);

        for (int i = 0; i < size; i++) {
            pool.offer(DriverManager.getConnection(url, user, password));
        }
    }

    public Connection getConnection() throws InterruptedException {
        return pool.take();
    }

    public void releaseConnection(Connection conn) {
        if (conn != null) {
            pool.offer(conn);
        }
    }

    public void closeAllConnections() throws SQLException {
        for (int i = 0; i < poolSize; i++) {
            pool.poll().close();
        }
    }
}
