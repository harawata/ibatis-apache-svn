/**
 * User: Clinton Begin
 * Date: May 31, 2003
 * Time: 2:11:03 PM
 */
package com.ibatis.sqlmap.engine.datasource;


import javax.sql.DataSource;
import java.util.*;

import org.apache.commons.dbcp.*;
import com.ibatis.sqlmap.client.*;

public class DbcpDataSourceFactory implements DataSourceFactory {

  private DataSource dataSource;

  private String[] expectedProperties = {
    "JDBC.Driver",
    "JDBC.ConnectionURL",
    "JDBC.Username",
    "JDBC.Password",
    "Pool.ValidationQuery",
    "Pool.MaximumActiveConnections",
    "Pool.MaximumIdleConnections",
    "Pool.MaximumWait",
    "Pool.LogAbandoned",
    "Pool.RemoveAbandoned",
    "Pool.RemoveAbandonedTimeout"
  };

  public void initialize(Map map) {
    try {
      String driver = (String) map.get("JDBC.Driver");
      String url = (String) map.get("JDBC.ConnectionURL");
      String username = (String) map.get("JDBC.Username");
      String password = (String) map.get("JDBC.Password");
      String validationQuery = (String) map.get("Pool.ValidationQuery");
      String maxActive = (String) map.get("Pool.MaximumActiveConnections");
      String maxIdle = (String) map.get("Pool.MaximumIdleConnections");
      String maxWait = (String) map.get("Pool.MaximumWait");
      String removeAbandoned = (String) map.get("Pool.RemoveAbandoned");
      String removeAbandonedTimeout = (String) map.get("Pool.RemoveAbandonedTimeout");
      String logAbandoned = (String) map.get("Pool.LogAbandoned");

      BasicDataSource basicDataSource = new BasicDataSource();

      basicDataSource.setUrl(url);
      basicDataSource.setDriverClassName(driver);
      basicDataSource.setUsername(username);
      basicDataSource.setPassword(password);

      if (notEmpty(validationQuery)) {
        basicDataSource.setValidationQuery(validationQuery);
      }

      if (notEmpty(maxActive)) {
        basicDataSource.setMaxActive(Integer.parseInt(maxActive));
      }

      if (notEmpty(maxIdle)) {
        basicDataSource.setMaxIdle(Integer.parseInt(maxIdle));
      }

      if (notEmpty(maxWait)) {
        basicDataSource.setMaxWait(Integer.parseInt(maxWait));
      }

      if (notEmpty(removeAbandonedTimeout)) {
        basicDataSource.setRemoveAbandonedTimeout(Integer.parseInt(removeAbandonedTimeout));
      }

      if (notEmpty(removeAbandoned)) {
        basicDataSource.setRemoveAbandoned("true".equals(removeAbandoned));
      }

      if (notEmpty(logAbandoned)) {
        basicDataSource.setLogAbandoned("true".equals(logAbandoned));
      }

      dataSource = basicDataSource;

    } catch (Exception e) {
      throw new SqlMapException("Error initializing DbcpDataSourceFactory.  Cause: " + e, e);
    }
  }

  private boolean notEmpty(String s) {
    return s != null && s.length() > 0;
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public String[] getExpectedProperties() {
    return expectedProperties;
  }

}



