package com.ibatis.dao;

import junit.framework.TestCase;
import com.ibatis.dao.client.DaoManagerBuilder;
import com.ibatis.dao.client.DaoManager;
import com.ibatis.dao.iface.AccountDao;
import com.ibatis.common.resources.Resources;
import com.ibatis.common.jdbc.ScriptRunner;

import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import testdomain.Account;

/**
 * <p/>
 * Date: Feb 29, 2004 12:33:57 PM
 * 
 * @author Clinton Begin
 */
public class SqlMapDaoTest extends BaseDaoTest {

  public void setUp() throws Exception {
    String resource = "com/ibatis/dao/sql-map-dao.xml";
    Reader reader = Resources.getResourceAsReader(resource);
    daoManager = DaoManagerBuilder.buildDaoManager(reader);
    initScript("scripts/account-init.sql");
  }

}
