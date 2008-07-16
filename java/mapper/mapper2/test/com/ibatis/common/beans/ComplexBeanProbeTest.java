package com.ibatis.common.beans;

import junit.framework.TestCase;

public class ComplexBeanProbeTest extends TestCase {

	public void testSetObject() {
		Test myTest = new Test();
		Probe probe = ProbeFactory.getProbe(myTest);
		probe.setObject(myTest, "myInt", Integer.valueOf(1));
		assertEquals(1, myTest.getMyInt());
		probe.setObject(myTest, "myInt", Integer.valueOf(2));
		assertEquals(2, myTest.getMyInt());
		try {
			probe.setObject(myTest, "myInt", null);
			fail();
		} catch (RuntimeException e) {
			assertTrue(e.getMessage().contains("'myInt' to value 'null'"));
		}
		try {
			probe.setObject(myTest, "myInt", Float.valueOf(1.2f));
			fail();
		} catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("'myInt' to value '1.2'"));
		}
	}

	public class Test {

		int	myInt;

		public int getMyInt() {
			return myInt;
		}

		public void setMyInt(int myInt) {
			this.myInt = myInt;
		}
	}

}
