package com.ibatis.sqlmap;

import java.util.List;

import testdomain.IItem;

public class ResultObjectFactoryTest extends BaseSqlMapTest {

  protected void setUp() throws Exception {
    initSqlMap("com/ibatis/sqlmap/maps/SqlMapConfig.xml", null);
    initScript("scripts/jpetstore-hsqldb-schema.sql");
    initScript("scripts/jpetstore-hsqldb-dataload.sql");
  }

  /**
   * This tests that the result object factory is working -
   * everything in the sql map is declared as an interface.
   *
   */
  public void test01() {
    try {
      List results =
          sqlMap.queryForList("getAllItemsROF");
      assertEquals(28, results.size());
      assertEquals(new Integer(1), ((IItem)results.get(2)).getSupplier().getSupplierId());
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

}
