package com.ibatis.common.jdbc;

import org.apache.ibatis.jdbc.UnpooledDataSource;

import javax.sql.DataSource;
import java.sql.*;

public class ScriptRunner extends org.apache.ibatis.migration.ScriptRunner {

  public ScriptRunner(Connection connection, boolean autoCommit, boolean stopOnError) {
    super(connection);
    setAutoCommit(autoCommit);
    setStopOnError(stopOnError);
  }

  public ScriptRunner(String driver, String url, String username, String password, boolean autoCommit, boolean stopOnError) throws SQLException {
    super(new UnpooledDataSource(driver, url, username, password).getConnection());
    setAutoCommit(autoCommit);
    setStopOnError(stopOnError);
  }
}
