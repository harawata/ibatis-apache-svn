package com.ibatis.dao;

import com.ibatis.common.resources.Resources;
import com.ibatis.dao.client.DaoManagerBuilder;

import java.io.Reader;

/**
 * <p/>
 * Date: Feb 29, 2004 1:54:09 PM
 * 
 * @author Clinton Begin
 */
public class JdbcDaoTest extends BaseDaoTest {

  public void setUp() throws Exception {
    String resource = "com/ibatis/dao/jdbc-dao.xml";
    Reader reader = Resources.getResourceAsReader(resource);
    daoManager = DaoManagerBuilder.buildDaoManager(reader);
    initScript("scripts/account-init.sql");
  }

}
