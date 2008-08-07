package org.apache.ibatis.reflection;

import org.junit.*;

import java.lang.reflect.*;

public class ExceptionUtilTest {

  @Test
  public void shouldUnwrapThrowable() {
    Exception exception = new Exception();
    Assert.assertEquals(exception, ExceptionUtil.unwrapThrowable(exception));
    Assert.assertEquals(exception, ExceptionUtil.unwrapThrowable(new InvocationTargetException(exception, "test")));
    Assert.assertEquals(exception, ExceptionUtil.unwrapThrowable(new UndeclaredThrowableException(exception, "test")));
  }


}
