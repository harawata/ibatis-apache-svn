/**
 * User: Clinton Begin
 * Date: Jun 23, 2003
 * Time: 8:28:58 PM
 */
package com.ibatis.common.util;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllUtilTests {

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(PaginatedArrayListTest.class);
    return suite;
  }


}
