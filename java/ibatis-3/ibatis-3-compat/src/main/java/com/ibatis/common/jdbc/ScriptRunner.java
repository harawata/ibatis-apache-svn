package com.ibatis.common.jdbc;

import javax.sql.DataSource;
import java.sql.Connection;

public class ScriptRunner extends org.apache.ibatis.migration.ScriptRunner {

  public ScriptRunner(DataSource dataSource, boolean autoCommit, boolean stopOnError) {
    super(dataSource, autoCommit, stopOnError);
  }

  public ScriptRunner(Connection connection, boolean autoCommit, boolean stopOnError) {
    super(connection, autoCommit, stopOnError);
  }

  public ScriptRunner(String driver, String url, String username, String password, boolean autoCommit, boolean stopOnError) {
    super(driver, url, username, password, autoCommit, stopOnError);
  }
}
