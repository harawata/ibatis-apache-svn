package com.ibatis.sqlmap;

import java.util.List;

public class RepeatingGroupMappingTest extends BaseSqlMapTest {

  protected void setUp() throws Exception {
    initSqlMap("com/ibatis/sqlmap/maps/SqlMapConfig.xml", null);
    initScript("scripts/jpetstore-hsqldb-schema.sql");
    initScript("scripts/jpetstore-hsqldb-dataload.sql");
  }

  public void testGroupBy() throws Exception {
    List list = sqlMap.queryForList("getAllCategories",null);
    assertEquals (5, list.size());
  }

}
