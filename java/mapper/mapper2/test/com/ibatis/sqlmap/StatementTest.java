/**
 * User: Clinton Begin
 * Date: May 17, 2003
 * Time: 8:46:39 PM
 */
package com.ibatis.sqlmap;

import com.ibatis.common.util.PaginatedList;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapSession;
import com.ibatis.sqlmap.client.event.RowHandler;
import testdomain.Account;
import testdomain.LineItem;
import testdomain.SuperAccount;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatementTest extends BaseSqlMapTest {

  // SETUP & TEARDOWN

  protected void setUp() throws Exception {
    initSqlMap("com/ibatis/sqlmap/maps/SqlMapConfig.xml", null);
    initScript("scripts/account-init.sql");
    initScript("scripts/order-init.sql");
    initScript("scripts/line_item-init.sql");
  }

  protected void tearDown() throws Exception {
  }

  // OBJECT QUERY TESTS

  public void testExecuteQueryForObjectViaColumnName() throws SQLException {
    Account account = (Account) sqlMap.queryForObject("getAccountViaColumnName", new Integer(1));
    assertAccount1(account);
  }


  public void testExecuteQueryForObjectViaColumnIndex() throws SQLException {
    Account account = (Account) sqlMap.queryForObject("getAccountViaColumnIndex", new Integer(1));
    assertAccount1(account);
  }

  public void testExecuteQueryForObjectViaResultClass() throws SQLException {
    Account account = (Account) sqlMap.queryForObject("getAccountViaResultClass", new Integer(1));
    assertAccount1(account);
  }

  public void testExecuteQueryForObjectAsHashMap() throws SQLException {
    Map account = (HashMap) sqlMap.queryForObject("getAccountAsHashMap", new Integer(1));
    assertAccount1(account);
  }

  public void testExecuteQueryForObjectAsHashMapResultClass() throws SQLException {
    Map account = (HashMap) sqlMap.queryForObject("getAccountAsHashMapResultClass", new Integer(1));
    assertAccount1(account);
  }

  public void testExecuteQueryForObjectWithSimpleResultClass() throws SQLException {
    String email = (String) sqlMap.queryForObject("getEmailAddressViaResultClass", new Integer(1));
    assertEquals("clinton.begin@ibatis.com", email);
  }

  public void testExecuteQueryForObjectWithSimpleResultMap() throws SQLException {
    String email = (String) sqlMap.queryForObject("getEmailAddressViaResultMap", new Integer(1));
    assertEquals("clinton.begin@ibatis.com", email);
  }

  public void testExecuteQueryForObjectWithResultObject() throws SQLException {
    Account account = new Account();
    Account testAccount = (Account) sqlMap.queryForObject("getAccountViaColumnName", new Integer(1), account);
    assertAccount1(account);
    assertTrue(account == testAccount);
  }

  public void testGetSubclass() throws SQLException {
    SuperAccount account = new SuperAccount();
    account.setId(1);
    account = (SuperAccount) sqlMap.queryForObject("getSuperAccount", account);
    assertAccount1(account);
  }

  // LIST QUERY TESTS

  public void testExecuteQueryForListWithResultMap() throws SQLException {
    List list = sqlMap.queryForList("getAllAccountsViaResultMap", null);

    assertAccount1((Account) list.get(0));
    assertEquals(5, list.size());
    assertEquals(1, ((Account) list.get(0)).getId());
    assertEquals(2, ((Account) list.get(1)).getId());
    assertEquals(3, ((Account) list.get(2)).getId());
    assertEquals(4, ((Account) list.get(3)).getId());
    assertEquals(5, ((Account) list.get(4)).getId());
  }

  public void testExecuteQueryForPaginatedList() throws SQLException {

    // Get List of all 5
    PaginatedList list = sqlMap.queryForPaginatedList("getAllAccountsViaResultMap", null, 2);

    // Test initial state (page 0)
    assertFalse(list.isPreviousPageAvailable());
    assertTrue(list.isNextPageAvailable());
    assertAccount1((Account) list.get(0));
    assertEquals(2, list.size());
    assertEquals(1, ((Account) list.get(0)).getId());
    assertEquals(2, ((Account) list.get(1)).getId());

    // Test illegal previous page (no effect, state should be same)
    list.previousPage();
    assertFalse(list.isPreviousPageAvailable());
    assertTrue(list.isNextPageAvailable());
    assertAccount1((Account) list.get(0));
    assertEquals(2, list.size());
    assertEquals(1, ((Account) list.get(0)).getId());
    assertEquals(2, ((Account) list.get(1)).getId());

    // Test next (page 1)
    list.nextPage();
    assertTrue(list.isPreviousPageAvailable());
    assertTrue(list.isNextPageAvailable());
    assertEquals(2, list.size());
    assertEquals(3, ((Account) list.get(0)).getId());
    assertEquals(4, ((Account) list.get(1)).getId());

    // Test next (page 2 -last)
    list.nextPage();
    assertTrue(list.isPreviousPageAvailable());
    assertFalse(list.isNextPageAvailable());
    assertEquals(1, list.size());
    assertEquals(5, ((Account) list.get(0)).getId());

    // Test previous (page 1)
    list.previousPage();
    assertTrue(list.isPreviousPageAvailable());
    assertTrue(list.isNextPageAvailable());
    assertEquals(2, list.size());
    assertEquals(3, ((Account) list.get(0)).getId());
    assertEquals(4, ((Account) list.get(1)).getId());

    // Test previous (page 0 -first)
    list.previousPage();
    assertFalse(list.isPreviousPageAvailable());
    assertTrue(list.isNextPageAvailable());
    assertAccount1((Account) list.get(0));
    assertEquals(2, list.size());
    assertEquals(1, ((Account) list.get(0)).getId());
    assertEquals(2, ((Account) list.get(1)).getId());

    // Test goto (page 0)
    list.gotoPage(0);
    assertFalse(list.isPreviousPageAvailable());
    assertTrue(list.isNextPageAvailable());
    assertEquals(2, list.size());
    assertEquals(1, ((Account) list.get(0)).getId());
    assertEquals(2, ((Account) list.get(1)).getId());

    // Test goto (page 1)
    list.gotoPage(1);
    assertTrue(list.isPreviousPageAvailable());
    assertTrue(list.isNextPageAvailable());
    assertEquals(2, list.size());
    assertEquals(3, ((Account) list.get(0)).getId());
    assertEquals(4, ((Account) list.get(1)).getId());

    // Test goto (page 2)
    list.gotoPage(2);
    assertTrue(list.isPreviousPageAvailable());
    assertFalse(list.isNextPageAvailable());
    assertEquals(1, list.size());
    assertEquals(5, ((Account) list.get(0)).getId());

    // Test illegal goto (page 0)
    list.gotoPage(3);
    assertTrue(list.isPreviousPageAvailable());
    assertFalse(list.isNextPageAvailable());
    assertEquals(0, list.size());

    list = sqlMap.queryForPaginatedList("getNoAccountsViaResultMap", null, 2);

    // Test empty list
    assertFalse(list.isPreviousPageAvailable());
    assertFalse(list.isNextPageAvailable());
    assertEquals(0, list.size());

    // Test next
    list.nextPage();
    assertFalse(list.isPreviousPageAvailable());
    assertFalse(list.isNextPageAvailable());
    assertEquals(0, list.size());

    // Test previous
    list.previousPage();
    assertFalse(list.isPreviousPageAvailable());
    assertFalse(list.isNextPageAvailable());
    assertEquals(0, list.size());

    // Test previous
    list.gotoPage(0);
    assertFalse(list.isPreviousPageAvailable());
    assertFalse(list.isNextPageAvailable());
    assertEquals(0, list.size());

    list = sqlMap.queryForPaginatedList("getFewAccountsViaResultMap", null, 2);

    assertFalse(list.isPreviousPageAvailable());
    assertFalse(list.isNextPageAvailable());
    assertEquals(1, list.size());

    // Test next
    list.nextPage();
    assertFalse(list.isPreviousPageAvailable());
    assertFalse(list.isNextPageAvailable());
    assertEquals(1, list.size());

    // Test previous
    list.previousPage();
    assertFalse(list.isPreviousPageAvailable());
    assertFalse(list.isNextPageAvailable());
    assertEquals(1, list.size());

    // Test previous
    list.gotoPage(0);
    assertFalse(list.isPreviousPageAvailable());
    assertFalse(list.isNextPageAvailable());
    assertEquals(1, list.size());

    // Test Even - Two Pages
    try {
      initScript("scripts/more-account-records.sql");
    } catch (Exception e) {
      fail(e.toString());
    }

    list = sqlMap.queryForPaginatedList("getAllAccountsViaResultMap", null, 5);

    assertEquals(5, list.size());

    list.nextPage();
    assertEquals(5, list.size());


    list.isPreviousPageAvailable();
    list.previousPage();
    assertEquals(5, list.size());


  }

  public void testExecuteQueryForListWithResultMapWithDynamicElement() throws SQLException {

    List list = sqlMap.queryForList("getAllAccountsViaResultMapWithDynamicElement", "LIKE");

    assertAccount1((Account) list.get(0));
    assertEquals(3, list.size());
    assertEquals(1, ((Account) list.get(0)).getId());
    assertEquals(2, ((Account) list.get(1)).getId());
    assertEquals(4, ((Account) list.get(2)).getId());

    list = sqlMap.queryForList("getAllAccountsViaResultMapWithDynamicElement", "=");

    assertEquals(0, list.size());

  }

  public void testExecuteQueryForListResultClass() throws SQLException {
    List list = sqlMap.queryForList("getAllAccountsViaResultClass", null);

    assertAccount1((Account) list.get(0));
    assertEquals(5, list.size());
    assertEquals(1, ((Account) list.get(0)).getId());
    assertEquals(2, ((Account) list.get(1)).getId());
    assertEquals(3, ((Account) list.get(2)).getId());
    assertEquals(4, ((Account) list.get(3)).getId());
    assertEquals(5, ((Account) list.get(4)).getId());
  }

  public void testExecuteQueryForListWithHashMapResultMap() throws SQLException {
    List list = sqlMap.queryForList("getAllAccountsAsHashMapViaResultMap", null);

    assertAccount1((Map) list.get(0));
    assertEquals(5, list.size());
    assertEquals(new Integer(1), ((Map) list.get(0)).get("id"));
    assertEquals(new Integer(2), ((Map) list.get(1)).get("id"));
    assertEquals(new Integer(3), ((Map) list.get(2)).get("id"));
    assertEquals(new Integer(4), ((Map) list.get(3)).get("id"));
    assertEquals(new Integer(5), ((Map) list.get(4)).get("id"));
  }

  public void testExecuteQueryForListWithHashMapResultClass() throws SQLException {
    List list = sqlMap.queryForList("getAllAccountsAsHashMapViaResultClass", null);

    assertAccount1((Map) list.get(0));
    assertEquals(5, list.size());
    assertEquals(new Integer(1), ((Map) list.get(0)).get("ID"));
    assertEquals(new Integer(2), ((Map) list.get(1)).get("ID"));
    assertEquals(new Integer(3), ((Map) list.get(2)).get("ID"));
    assertEquals(new Integer(4), ((Map) list.get(3)).get("ID"));
    assertEquals(new Integer(5), ((Map) list.get(4)).get("ID"));
  }

  public void testExecuteQueryForListWithSimpleResultClass() throws SQLException {
    List list = sqlMap.queryForList("getAllEmailAddressesViaResultClass", null);

    assertEquals("clinton.begin@ibatis.com", list.get(0));
    assertEquals(5, list.size());
  }

  public void testExecuteQueryForListWithSimpleResultMap() throws SQLException {
    List list = sqlMap.queryForList("getAllEmailAddressesViaResultMap", null);

    assertEquals("clinton.begin@ibatis.com", list.get(0));
    assertEquals(5, list.size());
  }

  public void testExecuteQueryForListWithSkipAndMax() throws SQLException {
    List list = sqlMap.queryForList("getAllAccountsViaResultMap", null, 2, 2);

    assertEquals(2, list.size());
    assertEquals(3, ((Account) list.get(0)).getId());
    assertEquals(4, ((Account) list.get(1)).getId());
  }

  public void testExecuteQueryForListWithRowHandler() throws SQLException {
    TestRowHandler handler = new TestRowHandler();
    List list = sqlMap.queryForList("getAllAccountsViaResultMap", null, handler);
    assertEquals(5, handler.getIndex());
    assertEquals(5, list.size());
    assertAccount1((Account) list.get(0));
    assertEquals(1, ((Account) list.get(0)).getId());
    assertEquals(2, ((Account) list.get(1)).getId());
    assertEquals(3, ((Account) list.get(2)).getId());
    assertEquals(4, ((Account) list.get(3)).getId());
    assertEquals(5, ((Account) list.get(4)).getId());

  }

  // MAP TESTS

  public void testExecuteQueryForMap() throws SQLException {
    Map map = sqlMap.queryForMap("getAllAccountsViaResultClass", null, "lastName");

    assertAccount1((Account) map.get("Begin"));
    assertEquals(5, map.size());
    assertEquals(1, ((Account) map.get("Begin")).getId());
    assertEquals(2, ((Account) map.get("Smith")).getId());
    assertEquals(3, ((Account) map.get("Jones")).getId());
    assertEquals(4, ((Account) map.get("Jackson")).getId());
    assertEquals(5, ((Account) map.get("Goodman")).getId());
  }

  public void testExecuteQueryForMapWithValueProperty() throws SQLException {
    Map map = sqlMap.queryForMap("getAllAccountsViaResultClass", null, "lastName", "firstName");

    assertEquals(5, map.size());
    assertEquals("Clinton", map.get("Begin"));
    assertEquals("Jim", map.get("Smith"));
    assertEquals("Elizabeth", map.get("Jones"));
    assertEquals("Bob", map.get("Jackson"));
    assertEquals("Amanda", map.get("Goodman"));
  }

  // UPDATE TESTS

  public void testInsertGeneratedKey() throws SQLException {
    LineItem item = new LineItem();

    item.setId(10);
    item.setItemCode("blah");
    item.setOrderId(333);
    item.setPrice(new BigDecimal("44.00"));
    item.setQuantity(1);

    Object key = sqlMap.insert("insertLineItem", item);

    assertEquals(new Integer(99), key);
    assertEquals(99, item.getId());

    Map param = new HashMap();
    param.put("orderId", new Integer(333));
    param.put("lineId", new Integer(10));
    LineItem testItem = (LineItem) sqlMap.queryForObject("getSpecificLineItem", param);
    assertNotNull(testItem);
    assertEquals(10, testItem.getId());
  }

  public void testInsertPreKey() throws SQLException {
    LineItem item = new LineItem();

    item.setId(10);
    item.setItemCode("blah");
    item.setOrderId(333);
    item.setPrice(new BigDecimal("44.00"));
    item.setQuantity(1);

    Object key = sqlMap.insert("insertLineItemPreKey", item);

    assertEquals(new Integer(99), key);
    assertEquals(99, item.getId());

    Map param = new HashMap();
    param.put("orderId", new Integer(333));
    param.put("lineId", new Integer(99));
    LineItem testItem = (LineItem) sqlMap.queryForObject("getSpecificLineItem", param);
    assertNotNull(testItem);
    assertEquals(99, testItem.getId());

  }

  public void testInsertNoKey() throws SQLException {
    LineItem item = new LineItem();

    item.setId(100);
    item.setItemCode("blah");
    item.setOrderId(333);
    item.setPrice(new BigDecimal("44.00"));
    item.setQuantity(1);

    Object key = sqlMap.insert("insertLineItemNoKey", item);

    assertNull(null, key);
    assertEquals(100, item.getId());

    Map param = new HashMap();
    param.put("orderId", new Integer(333));
    param.put("lineId", new Integer(100));
    LineItem testItem = (LineItem) sqlMap.queryForObject("getSpecificLineItem", param);
    assertNotNull(testItem);
    assertEquals(100, testItem.getId());

  }

  public void testExecuteUpdateWithParameterMap() throws SQLException {
    Account account = (Account) sqlMap.queryForObject("getAccountViaColumnName", new Integer(1));

    account.setId(6);
    account.setEmailAddress("new.clinton@ibatis.com");
    sqlMap.update("insertAccountViaParameterMap", account);

    account = (Account) sqlMap.queryForObject("getAccountViaColumnName", new Integer(6));

    assertEquals("new.clinton@ibatis.com", account.getEmailAddress());

  }

  public void testExecuteUpdateWithInlineParameters() throws SQLException {
    Account account = (Account) sqlMap.queryForObject("getAccountViaColumnName", new Integer(1));

    account.setEmailAddress("new.clinton@ibatis.com");
    sqlMap.update("updateAccountViaInlineParameters", account);

    account = (Account) sqlMap.queryForObject("getAccountViaColumnName", new Integer(1));

    assertEquals("new.clinton@ibatis.com", account.getEmailAddress());

  }

  public void testExecuteUpdateWithParameterClass() throws SQLException {
    Account account = new Account();
    account.setId(5);

    boolean checkForInvalidTypeFailedAppropriately = false;
    try {
      sqlMap.update("deleteAccount", new Object());
    } catch (SQLException e) {
      checkForInvalidTypeFailedAppropriately = true;
    }

    sqlMap.update("deleteAccount", account);

    account = (Account) sqlMap.queryForObject("getAccountViaColumnName", new Integer(5));

    assertNull(account);
    assertTrue(checkForInvalidTypeFailedAppropriately);
  }

  // DYNAMIC SQL AND CACHING

  public void testMappedStatementQueryWithCache() throws SQLException {
    List list = sqlMap.queryForList("getCachedAccountsViaResultMap", null);

    int firstId = System.identityHashCode(list);

    list = sqlMap.queryForList("getCachedAccountsViaResultMap", null);

    int secondId = System.identityHashCode(list);

    assertEquals(firstId, secondId);

    Account account = (Account) list.get(1);
    account.setEmailAddress("new.clinton@ibatis.com");
    sqlMap.update("updateAccountViaInlineParameters", account);

    list = sqlMap.queryForList("getCachedAccountsViaResultMap", null);

    int thirdId = System.identityHashCode(list);

    assertTrue(firstId != thirdId);

  }

  public void testFlushDataCache() throws SQLException {
    List list = sqlMap.queryForList("getCachedAccountsViaResultMap", null);

    int firstId = System.identityHashCode(list);

    list = sqlMap.queryForList("getCachedAccountsViaResultMap", null);

    int secondId = System.identityHashCode(list);

    assertEquals(firstId, secondId);

    sqlMap.flushDataCache();

    list = sqlMap.queryForList("getCachedAccountsViaResultMap", null);

    int thirdId = System.identityHashCode(list);

    assertTrue(firstId != thirdId);

  }

  public void testMappedStatementQueryWithThreadedCache() throws SQLException {

    Map results = new HashMap();

    TestCacheThread.startThread(sqlMap, results, "getCachedAccountsViaResultMap");
    Integer firstId = (Integer) results.get("id");

    TestCacheThread.startThread(sqlMap, results, "getCachedAccountsViaResultMap");
    Integer secondId = (Integer) results.get("id");

    assertTrue(firstId.equals(secondId));

    List list = (List) results.get("list");

    Account account = (Account) list.get(1);
    account.setEmailAddress("new.clinton@ibatis.com");
    sqlMap.update("updateAccountViaInlineParameters", account);

    list = sqlMap.queryForList("getCachedAccountsViaResultMap", null);

    int thirdId = System.identityHashCode(list);

    assertTrue(firstId.intValue() != thirdId);

  }

  public void testMappedStatementQueryWithThreadedReadWriteCache() throws SQLException {

    Map results = new HashMap();

    TestCacheThread.startThread(sqlMap, results, "getRWCachedAccountsViaResultMap");
    Integer firstId = (Integer) results.get("id");

    TestCacheThread.startThread(sqlMap, results, "getRWCachedAccountsViaResultMap");
    Integer secondId = (Integer) results.get("id");

    assertFalse(firstId.equals(secondId));

    List list = (List) results.get("list");

    Account account = (Account) list.get(1);
    account.setEmailAddress("new.clinton@ibatis.com");
    sqlMap.update("updateAccountViaInlineParameters", account);

    list = sqlMap.queryForList("getCachedAccountsViaResultMap", null);

    int thirdId = System.identityHashCode(list);

    assertTrue(firstId.intValue() != thirdId);

  }

  private static class TestCacheThread extends Thread {
    private SqlMapClient sqlMap;
    private Map results;
    private String statementName;

    public TestCacheThread(SqlMapClient sqlMap, Map results, String statementName) {
      this.sqlMap = sqlMap;
      this.results = results;
      this.statementName = statementName;
    }

    public void run() {
      try {
        SqlMapSession session = sqlMap.openSession();
        List list = session.queryForList(statementName, null);
        int firstId = System.identityHashCode(list);
        list = session.queryForList(statementName, null);
        int secondId = System.identityHashCode(list);
        assertEquals(firstId, secondId);
        results.put("id", new Integer(System.identityHashCode(list)));
        results.put("list", list);
        session.close();
      } catch (SQLException e) {
        throw new RuntimeException("Error.  Cause: " + e);
      }
    }

    public static void startThread(SqlMapClient sqlMap, Map results, String statementName) {
      Thread t = new TestCacheThread(sqlMap, results, statementName);
      t.start();
      try {
        t.join();
      } catch (InterruptedException e) {
        throw new RuntimeException("Error.  Cause: " + e);
      }
    }
  }

  public void testQueryDynamicSqlElement() throws SQLException {
    List list = sqlMap.queryForList("getDynamicOrderedEmailAddressesViaResultMap", "ACC_ID");

    assertEquals("clinton.begin@ibatis.com", (String) list.get(0));

    list = sqlMap.queryForList("getDynamicOrderedEmailAddressesViaResultMap", "ACC_FIRST_NAME");

    assertNull(list.get(0));

  }

  // INNER CLASSES

  public class TestRowHandler implements RowHandler {
    private int index = 0;

    public void handleRow(Object object, List list) {
      index++;
      assertEquals(index, ((Account) object).getId());
      list.add(object);
    }

    public int getIndex() {
      return index;
    }
  }

}
