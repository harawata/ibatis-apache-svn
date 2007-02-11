package com.ibatis.sqlmap;

import testdomain.Account;
import testdomain.PrivateAccount;

import java.sql.SQLException;

public class DirectFieldMappingTest extends BaseSqlMapTest {

    protected void setUp() throws Exception {
    initSqlMap("com/ibatis/sqlmap/maps/SqlMapConfig.xml", null);
    initScript("scripts/account-init.sql");
  }

  public void testInsertAndSelectDirectToFields() throws SQLException {
    Account account = newAccount6();

    sqlMap.insert("insertAccountFromFields", account);

    account = (Account) sqlMap.queryForObject("getAccountToFields", new Integer(6));

    assertAccount6(account);
    assertAccount6(account.getAccount());
  }
  
  public void testGetAccountWithPrivateConstructor() throws SQLException {
    Account account = newAccount6();

    sqlMap.insert("insertAccountFromFields", account);

    PrivateAccount pvt = (PrivateAccount) sqlMap.queryForObject("getAccountWithPrivateConstructor", new Integer(6));

    assertPrivateAccount6(pvt);
  }


}
