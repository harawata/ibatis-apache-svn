package com.ibatis.sqlmap;

import testdomain.Account;

import java.sql.SQLException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Clinton
 * Date: 24-May-2004
 * Time: 4:56:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class IterateTest extends BaseSqlMapTest {

  protected void setUp() throws Exception {
    initSqlMap("com/ibatis/sqlmap/maps/SqlMapConfig.xml", null);
    initScript("scripts/account-init.sql");
  }

  protected void tearDown() throws Exception {
  }

  // Iterate

  public void testIterate() throws SQLException {
    List params = Arrays.asList(new Integer[]{new Integer(1), new Integer(2), new Integer(3)});
    List list = sqlMap.queryForList("dynamicIterate", params);
    assertAccount1((Account) list.get(0));
    assertEquals(3, list.size());
  }

  public void testMultiIterate() throws SQLException {
    List params = Arrays.asList(new Integer[]{new Integer(1), new Integer(2), new Integer(3)});
    List list = sqlMap.queryForList("multiDynamicIterate", params);
    assertAccount1((Account) list.get(0));
    assertEquals(3, list.size());
  }

  // ARRAY

  public void testArrayPropertyIterate() throws SQLException {
    Account account = new Account();
    account.setIds(new int[]{1, 2, 3});
    List list = sqlMap.queryForList("dynamicQueryByExample", account);
    assertAccount1((Account) list.get(0));
    assertEquals(3, list.size());
  }

  // LIST IN MAP

  public void testListInMap() throws SQLException {
    List paramList = new Vector();
    paramList.add(new Integer(1));
    paramList.add(new Integer(2));
    paramList.add(new Integer(3));

    Map paramMap = new HashMap();
    paramMap.put("paramList",paramList);

    List list = sqlMap.queryForList("iterateListInMap", paramMap);
    assertAccount1((Account) list.get(0));
    assertEquals(3, list.size());
  }

  public void testListDirect() throws SQLException {
    List paramList = new Vector();
    paramList.add(new Integer(1));
    paramList.add(new Integer(2));
    paramList.add(new Integer(3));

    List list = sqlMap.queryForList("iterateListDirect", paramList);
    assertAccount1((Account) list.get(0));
    assertEquals(3, list.size());
  }

}
