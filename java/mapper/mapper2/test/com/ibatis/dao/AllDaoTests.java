package com.ibatis.dao;

import junit.framework.TestSuite;
import junit.framework.Test;
import com.ibatis.common.util.Stopwatch;

/**
 * <p/>
 * Date: Feb 29, 2004 2:04:04 PM
 * 
 * @author Clinton Begin
 */
public class AllDaoTests {

  public static void main(String[] args) throws Exception {
    junit.textui.TestRunner.run(suite());
  }

  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(SqlMapDaoTest.class);
    suite.addTestSuite(JdbcDaoTest.class);
    return suite;
  }

}
