package com.ibatis.sqlmap;

import com.ibatis.common.resources.Resources;

import java.sql.*;
import java.util.*;

public class MultiResultSetTest extends BaseSqlMapTest {

  // SETUP & TEARDOWN

  protected void setUp() throws Exception {
    initSqlMap("com/ibatis/sqlmap/maps/DerbySqlMapConfig.xml", Resources.getResourceAsProperties("com/ibatis/sqlmap/maps/DerbySqlMapConfig.properties"));
    initScript("scripts/account-init.sql");
    initScript("scripts/derby-proc-init.sql");

  }

  public void testShouldRetrieveTwoSetsOfTwoAccountsFromMultipleResultMaps() throws Exception {
    Map persons = new HashMap();
    persons.put("1", new Integer(1));
    persons.put("2", new Integer(2));
    persons.put("3", new Integer(3));
    persons.put("4", new Integer(4));
    List results = sqlMap.queryForList("getMultiListsRm", persons);
    assertEquals(2, results.size());
    assertEquals(2, ((List) results.get(0)).size());
    assertEquals(2, ((List) results.get(1)).size());
  }

  public void testShouldRetrieveTwoSetsOfTwoAccountsFromMultipleResultClasses() throws Exception {
    Map persons = new HashMap();
    persons.put("1", new Integer(1));
    persons.put("2", new Integer(2));
    persons.put("3", new Integer(3));
    persons.put("4", new Integer(4));
    List results = sqlMap.queryForList("getMultiListsRc", persons);
    assertEquals(2, results.size());
    assertEquals(2, ((List) results.get(0)).size());
    assertEquals(2, ((List) results.get(1)).size());
  }

  public void testCallableStatementShouldReturnTwoResultSets() throws Exception {
    sqlMap.startTransaction();
    Connection conn = sqlMap.getCurrentConnection();
    CallableStatement cs = conn.prepareCall("{call MRESULTSET(?,?,?,?)}");
    cs.setInt(1, 1);
    cs.setInt(2, 2);
    cs.setInt(3, 3);
    cs.setInt(4, 4);
    cs.execute();
    ResultSet rs = cs.getResultSet();
    assertNotNull(rs);
    int found = 1;
    while (cs.getMoreResults()) {
      assertNotNull(cs.getResultSet());
      found++;
    }
    rs.close();
    cs.close();
    assertEquals("Didn't find second result set.", 2, found);
  }


}
