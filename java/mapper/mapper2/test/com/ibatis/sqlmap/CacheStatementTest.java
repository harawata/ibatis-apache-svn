package com.ibatis.sqlmap;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapSession;
import testdomain.Account;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p/>
 * Date: May 8, 2004 3:49:53 PM
 * 
 * @author Clinton Begin
 */
public class CacheStatementTest extends BaseSqlMapTest {

  // SETUP & TEARDOWN

  protected void setUp() throws Exception {
    initSqlMap("com/ibatis/sqlmap/maps/SqlMapConfig.xml", null);
    initScript("scripts/account-init.sql");
  }

  protected void tearDown() throws Exception {
  }

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
        //assertEquals(firstId, secondId);
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


}
