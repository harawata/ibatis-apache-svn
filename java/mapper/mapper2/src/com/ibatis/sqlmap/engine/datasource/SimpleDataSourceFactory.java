/*
 * User: Clinton Begin
 * Date: Sep 23, 2002
 * Time: 8:56:22 PM
 */
package com.ibatis.sqlmap.engine.datasource;

import com.ibatis.common.jdbc.*;

import javax.sql.DataSource;
import java.util.*;

public class SimpleDataSourceFactory implements DataSourceFactory {

  private String[] expectedProperties = {
    "JDBC.Driver",
    "JDBC.ConnectionURL",
    "JDBC.Username",
    "JDBC.Password",
    "Pool.MaximumActiveConnections",
    "Pool.MaximumIdleConnections",
    "Pool.MaximumCheckoutTime",
    "Pool.TimeToWait",
    "Pool.PingQuery",
    "Pool.PingEnabled",
    "Pool.PingConnectionsOlderThan",
    "Pool.PingConnectionsNotUsedFor",
    "Pool.QuietMode"
  };

  private DataSource dataSource;

  public void initialize(Map map) {
    dataSource = new SimpleDataSource(map);
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public String[] getExpectedProperties() {
    return expectedProperties;
  }
}
