package org.apache.ibatis.migration;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.adhoc.AdHocExecutor;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.SimpleDataSource;
import org.junit.*;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class ScriptRunnerTest extends BaseDataTest {

  @Test
  public void shouldRunScriptsUsingConnection() throws Exception {
    SimpleDataSource ds = createSimpleDataSource(JPETSTORE_PROPERTIES);
    Connection conn = ds.getConnection();
    ScriptRunner runner = new ScriptRunner(conn, true, false);
    runner.setErrorLogWriter(null);
    runner.setLogWriter(null);
    runJPetStoreScripts(runner);
    assertProductsTableExistsAndLoaded();
  }

  @Test
  public void shouldRunScriptsUsingProperties() throws Exception {
    Properties props = Resources.getResourceAsProperties(JPETSTORE_PROPERTIES);
    ScriptRunner runner = new ScriptRunner(
        props.getProperty("driver"),
        props.getProperty("url"),
        props.getProperty("username"),
        props.getProperty("password"),
        true,
        false);
    runner.setErrorLogWriter(null);
    runner.setLogWriter(null);
    runJPetStoreScripts(runner);
    assertProductsTableExistsAndLoaded();
  }

  private void runJPetStoreScripts(ScriptRunner runner) throws IOException, SQLException {
    runScript(runner, JPETSTORE_DDL);
    runScript(runner, JPETSTORE_DATA);
  }

  private void assertProductsTableExistsAndLoaded() throws IOException, SQLException {
    SimpleDataSource ds = createSimpleDataSource(JPETSTORE_PROPERTIES);
    try {
      Connection conn = ds.getConnection();
      AdHocExecutor executor = new AdHocExecutor(conn);
      List<Map<String, Object>> products = executor.selectAll("SELECT * FROM PRODUCT");
      Assert.assertEquals(16, products.size());
    } finally {
      ds.forceCloseAll();
    }
  }

}
