package com.ibatis.sqlmap;

import com.ibatis.common.resources.Resources;


public class MultiResultSetTest extends BaseSqlMapTest {

  // SETUP & TEARDOWN

  protected void setUp() throws Exception {
      
    initSqlMap(
            "com/ibatis/sqlmap/maps/SqlMapConfig.xml", 
            Resources.getResourceAsProperties(
                    "com/ibatis/sqlmap/maps/DerbySqlMapConfig.properties"));
    
    initScript("scripts/account-init.sql");
    
  }

}
