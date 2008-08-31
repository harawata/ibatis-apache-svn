package org.apache.ibatis;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.PooledDataSource;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.*;
import java.sql.*;
import java.util.Properties;

public class BaseDataTest {

  public static final String BLOG_PROPERTIES = "databases/blog/blog-derby.properties";
  public static final String BLOG_DDL = "databases/blog/blog-derby-schema.sql";
  public static final String BLOG_DATA = "databases/blog/blog-derby-dataload.sql";

  public static final String JPETSTORE_PROPERTIES = "databases/jpetstore/jpetstore-hsqldb.properties";
  public static final String JPETSTORE_DDL = "databases/jpetstore/jpetstore-hsqldb-schema.sql";
  public static final String JPETSTORE_DATA = "databases/jpetstore/jpetstore-hsqldb-dataload.sql";

  public static PooledDataSource createPooledDataSource(String resource) throws IOException {
    Properties props = Resources.getResourceAsProperties(resource);
    PooledDataSource ds = new PooledDataSource();
    ds.setJdbcDriver(props.getProperty("driver"));
    ds.setJdbcUrl(props.getProperty("url"));
    ds.setJdbcUsername(props.getProperty("username"));
    ds.setJdbcPassword(props.getProperty("password"));
    return ds;
  }

  public static void runScript(DataSource ds, String resource) throws IOException, SQLException {
    Connection connection = ds.getConnection();
    try {
      ScriptRunner runner = new ScriptRunner(connection);
      runner.setAutoCommit(true);
      runner.setStopOnError(false);
      runner.setLogWriter(null);
      runScript(runner, resource);
    } finally {
      connection.close();
    }
  }

  public static void runScript(ScriptRunner runner, String resource) throws IOException, SQLException {
    Reader reader = Resources.getResourceAsReader(resource);
    try {
      runner.runScript(reader);
    } finally {
      reader.close();
    }
  }

  @Test
  public void dummy() {
  }

}
