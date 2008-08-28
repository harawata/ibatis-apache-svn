package org.apache.ibatis.adhoc;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.jdbc.SimpleDataSource;
import org.junit.*;

import java.sql.Connection;
import java.util.*;

public class AdHocExecutorTest extends BaseDataTest {

  @Test
  public void shouldSelectOne() throws Exception {
    SimpleDataSource ds = createSimpleDataSource(JPETSTORE_PROPERTIES);
    runScript(ds, JPETSTORE_DDL);
    runScript(ds, JPETSTORE_DATA);
    Connection connection = ds.getConnection();
    try {
      AdHocExecutor exec = new AdHocExecutor(connection);
      Map row = exec.selectOne("SELECT * FROM PRODUCT WHERE PRODUCTID = ?", "FI-SW-01");
      Assert.assertEquals("FI-SW-01", row.get("PRODUCTID"));
    } finally {
      ds.forceCloseAll();
    }
  }

  @Test
  public void shouldSelectList() throws Exception {
    SimpleDataSource ds = createSimpleDataSource(JPETSTORE_PROPERTIES);
    runScript(ds, JPETSTORE_DDL);
    runScript(ds, JPETSTORE_DATA);
    Connection connection = ds.getConnection();
    try {
      AdHocExecutor exec = new AdHocExecutor(connection);
      List rows = exec.selectAll("SELECT * FROM PRODUCT");
      Assert.assertEquals(16, rows.size());
    } finally {
      ds.forceCloseAll();
    }
  }

  @Test
  public void shouldInsert() throws Exception {
    SimpleDataSource ds = createSimpleDataSource(BLOG_PROPERTIES);
    runScript(ds, BLOG_DDL);
    Connection connection = ds.getConnection();
    try {
      AdHocExecutor exec = new AdHocExecutor(connection, true);
      int id = exec.insert("INSERT INTO author (username, password, email, bio) VALUES (?,?,?,?)", "someone", "******", "someone@apache.org", Null.LONGVARCHAR);
      Map row = exec.selectOne("SELECT * FROM author WHERE username = ?", "someone");
      connection.rollback();
      Assert.assertTrue(AdHocExecutor.NO_GENERATED_KEY != id);
      Assert.assertEquals("someone", row.get("USERNAME"));
    } finally {
      ds.forceCloseAll();
    }
  }

  @Test
  public void shouldUpdateCategory() throws Exception {
    SimpleDataSource ds = createSimpleDataSource(JPETSTORE_PROPERTIES);
    runScript(ds, JPETSTORE_DDL);
    runScript(ds, JPETSTORE_DATA);
    Connection connection = ds.getConnection();
    try {
      AdHocExecutor exec = new AdHocExecutor(connection);
      int count = exec.update("update product set category = ? where productid = ?", "DOGS", "FI-SW-01");
      Map row = exec.selectOne("SELECT * FROM PRODUCT WHERE PRODUCTID = ?", "FI-SW-01");
      Assert.assertEquals("DOGS", row.get("CATEGORY"));
      Assert.assertEquals(1, count);
    } finally {
      ds.forceCloseAll();
    }
  }

  @Test
  public void shouldDeleteOne() throws Exception {
    SimpleDataSource ds = createSimpleDataSource(JPETSTORE_PROPERTIES);
    runScript(ds, JPETSTORE_DDL);
    runScript(ds, JPETSTORE_DATA);
    Connection connection = ds.getConnection();
    try {
      AdHocExecutor exec = new AdHocExecutor(connection);
      int count = exec.delete("delete from item");
      List rows = exec.selectAll("SELECT * FROM ITEM");
      Assert.assertEquals(28, count);
      Assert.assertEquals(0, rows.size());
    } finally {
      ds.forceCloseAll();
    }
  }

  @Test
  public void shouldDemonstrateDDLThroughRunMethod() throws Exception {
    SimpleDataSource ds = createSimpleDataSource(JPETSTORE_PROPERTIES);
    Connection connection = ds.getConnection();
    try {
      AdHocExecutor exec = new AdHocExecutor(connection);
      exec.run("CREATE TABLE BLAH(ID INTEGER)");
      exec.run("insert into BLAH values (1)");
      List rows = exec.selectAll("SELECT * FROM BLAH");
      exec.run("DROP TABLE BLAH");
      Assert.assertEquals(1, rows.size());
    } finally {
      ds.forceCloseAll();
    }
  }


}
