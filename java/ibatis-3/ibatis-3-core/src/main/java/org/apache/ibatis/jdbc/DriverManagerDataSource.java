package org.apache.ibatis.jdbc;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DriverManagerDataSource implements DataSource {

  private String jdbcDriver;
  private String jdbcUrl;
  private String jdbcUsername;
  private String jdbcPassword;

  private ClassLoader driverClassLoader;

  private boolean jdbcDefaultAutoCommit;
  private Properties jdbcDriverProperties;

  private boolean driverInitialized;

  public Connection getConnection() throws SQLException {
    initializeDriver();
    if (jdbcDriverProperties != null) {
      return DriverManager.getConnection(jdbcUrl, jdbcDriverProperties);
    } else if (jdbcUsername == null && jdbcPassword == null) {
      return DriverManager.getConnection(jdbcUrl);
    } else {
      return DriverManager.getConnection(jdbcUrl, jdbcUsername, jdbcPassword);
    }
  }

  public Connection getConnection(String username, String password) throws SQLException {
    return DriverManager.getConnection(jdbcUrl, username, password);
  }

  public void setLoginTimeout(int loginTimeout) throws SQLException {
    DriverManager.setLoginTimeout(loginTimeout);
  }

  public int getLoginTimeout() throws SQLException {
    return DriverManager.getLoginTimeout();
  }

  public void setLogWriter(PrintWriter logWriter) throws SQLException {
    DriverManager.setLogWriter(logWriter);
  }

  public PrintWriter getLogWriter() throws SQLException {
    return DriverManager.getLogWriter();
  }

  public ClassLoader getDriverClassLoader() {
    return driverClassLoader;
  }

  public void setDriverClassLoader(ClassLoader driverClassLoader) {
    this.driverClassLoader = driverClassLoader;
  }

  public String getJdbcDriver() {
    return jdbcDriver;
  }

  public void setJdbcDriver(String jdbcDriver) {
    this.jdbcDriver = jdbcDriver;
  }

  public String getJdbcUrl() {
    return jdbcUrl;
  }

  public void setJdbcUrl(String jdbcUrl) {
    this.jdbcUrl = jdbcUrl;
  }

  public String getJdbcUsername() {
    return jdbcUsername;
  }

  public void setJdbcUsername(String jdbcUsername) {
    this.jdbcUsername = jdbcUsername;
  }

  public String getJdbcPassword() {
    return jdbcPassword;
  }

  public void setJdbcPassword(String jdbcPassword) {
    this.jdbcPassword = jdbcPassword;
  }

  public boolean isJdbcDefaultAutoCommit() {
    return jdbcDefaultAutoCommit;
  }

  public void setJdbcDefaultAutoCommit(boolean jdbcDefaultAutoCommit) {
    this.jdbcDefaultAutoCommit = jdbcDefaultAutoCommit;
  }

  public Properties getJdbcDriverProperties() {
    return jdbcDriverProperties;
  }

  public void setJdbcDriverProperties(Properties jdbcDriverProperties) {
    this.jdbcDriverProperties = jdbcDriverProperties;
  }

  private void initializeDriver() {
    if (!driverInitialized) {
      driverInitialized = true;
      Class driverType;
      try {
        if (driverClassLoader != null) {
          driverType = Class.forName(jdbcDriver, true, driverClassLoader);
        } else {
          driverType = Class.forName(jdbcDriver);
        }
        DriverManager.registerDriver((Driver) driverType.newInstance());
      } catch (Exception e) {
        throw new RuntimeException("Error setting driver on SimpleDataSource. Cause: " + e, e);
      }
    }
  }

}
