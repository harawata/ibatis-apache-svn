package org.apache.ibatis.jdbc;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.jdbc.PooledDataSource;
import org.apache.ibatis.jdbc.Null;
import org.apache.ibatis.jdbc.SqlRunner;
import org.junit.*;

import java.sql.Connection;
import java.util.*;

public class SqlRunnerTest extends BaseDataTest {

  @Test
  public void shouldSelectOne() throws Exception {
    PooledDataSource ds = createPooledDataSource(JPETSTORE_PROPERTIES);
    runScript(ds, JPETSTORE_DDL);
    runScript(ds, JPETSTORE_DATA);
    Connection connection = ds.getConnection();
    try {
      SqlRunner exec = new SqlRunner(connection);
      Map row = exec.selectOne("SELECT * FROM PRODUCT WHERE PRODUCTID = ?", "FI-SW-01");
      Assert.assertEquals("FI-SW-01", row.get("PRODUCTID"));
    } finally {
      ds.forceCloseAll();
    }
  }

  @Test
  public void shouldSelectList() throws Exception {
    PooledDataSource ds = createPooledDataSource(JPETSTORE_PROPERTIES);
    runScript(ds, JPETSTORE_DDL);
    runScript(ds, JPETSTORE_DATA);
    Connection connection = ds.getConnection();
    try {
      SqlRunner exec = new SqlRunner(connection);
      List rows = exec.selectAll("SELECT * FROM PRODUCT");
      Assert.assertEquals(16, rows.size());
    } finally {
      ds.forceCloseAll();
    }
  }

  @Test
  public void shouldInsert() throws Exception {
    PooledDataSource ds = createPooledDataSource(BLOG_PROPERTIES);
    runScript(ds, BLOG_DDL);
    Connection connection = ds.getConnection();
    try {
      SqlRunner exec = new SqlRunner(connection);
      exec.setForceGeneratedKeySupport(true);
      int id = exec.insert("INSERT INTO author (username, password, email, bio) VALUES (?,?,?,?)", "someone", "******", "someone@apache.org", Null.LONGVARCHAR);
      Map row = exec.selectOne("SELECT * FROM author WHERE username = ?", "someone");
      connection.rollback();
      Assert.assertTrue(SqlRunner.NO_GENERATED_KEY != id);
      Assert.assertEquals("someone", row.get("USERNAME"));
    } finally {
      ds.forceCloseAll();
    }
  }

  @Test
  public void shouldUpdateCategory() throws Exception {
    PooledDataSource ds = createPooledDataSource(JPETSTORE_PROPERTIES);
    runScript(ds, JPETSTORE_DDL);
    runScript(ds, JPETSTORE_DATA);
    Connection connection = ds.getConnection();
    try {
      SqlRunner exec = new SqlRunner(connection);
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
    PooledDataSource ds = createPooledDataSource(JPETSTORE_PROPERTIES);
    runScript(ds, JPETSTORE_DDL);
    runScript(ds, JPETSTORE_DATA);
    Connection connection = ds.getConnection();
    try {
      SqlRunner exec = new SqlRunner(connection);
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
    PooledDataSource ds = createPooledDataSource(JPETSTORE_PROPERTIES);
    Connection connection = ds.getConnection();
    try {
      SqlRunner exec = new SqlRunner(connection);
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
