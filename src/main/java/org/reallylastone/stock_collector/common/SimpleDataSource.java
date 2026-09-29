package org.reallylastone.stock_collector.common;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

public class SimpleDataSource implements DataSource {

    private final String url;
    private final String user;
    private final String password;
    private PrintWriter logWriter;
    private int loginTimeoutSeconds;

    public SimpleDataSource(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    @Override
    public PrintWriter getLogWriter() {
        return logWriter;
    }

    @Override
    public void setLogWriter(PrintWriter out) {
        this.logWriter = out;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }    @Override
    public void setLoginTimeout(int seconds) {
        this.loginTimeoutSeconds = seconds;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException(getClass().getName() + " is not a wrapper for " + iface.getName());
    }    @Override
    public int getLoginTimeout() {
        return loginTimeoutSeconds;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return false;
    }




}
