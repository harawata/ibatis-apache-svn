package com.ibatis.sqlmap;

import com.ibatis.common.resources.Resources;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MultiResultSetTest extends BaseSqlMapTest {

  // SETUP & TEARDOWN

  protected void setUp() throws Exception {
      
    initSqlMap(
            "com/ibatis/sqlmap/maps/DerbySqlMapConfig.xml", 
            Resources.getResourceAsProperties(
                    "com/ibatis/sqlmap/maps/DerbySqlMapConfig.properties"));
    
    initScript("scripts/account-init.sql");
    initScript("scripts/derby-proc-init.sql");
    
  }
  
  /**
   * This test should return 2 lists of 2 accounts each
   * 
   */
  public void test01() {
    
    Map persons = new HashMap();
    persons.put("accountId1",new Integer(1));
    persons.put("accountId2",new Integer(2));
    persons.put("accountId3",new Integer(3));
    persons.put("accountId4",new Integer(4));
      
    try {
      List results = 
          sqlMap.queryForList("getMultiListsRm", persons);
      assertFalse(results.isEmpty());
      // need assertion to check for list in list with 2 accounts each
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }
  
  

}
