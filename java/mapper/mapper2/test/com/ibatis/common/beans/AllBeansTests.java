/**
 * User: Clinton Begin
 * Date: Aug 24, 2003
 * Time: 10:13:08 AM
 */
package com.ibatis.common.beans;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllBeansTests {

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(PropertyAccessPlanTest.class);
    return suite;
  }


}
