/**
 * User: Clinton Begin
 * Date: May 19, 2003
 * Time: 1:57:49 PM
 */
package com.ibatis.sqlmap;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllSqlMapTests {

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(TransactionTest.class);
    suite.addTestSuite(StatementTest.class);
    suite.addTestSuite(CacheStatementTest.class);
    suite.addTestSuite(ParameterMapTest.class);
    suite.addTestSuite(ResultMapTest.class);
    suite.addTestSuite(DynamicTest.class);
    suite.addTestSuite(DynamicPrependTest.class);
    suite.addTestSuite(ComplexTypeTest.class);
    suite.addTestSuite(XmlStatementTest.class);
    suite.addTestSuite(DomStatementTest.class);
    return suite;
  }

}
