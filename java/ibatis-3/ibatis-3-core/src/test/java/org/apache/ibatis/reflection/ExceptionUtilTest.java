package org.apache.ibatis.reflection;

import org.junit.*;

import java.lang.reflect.*;
import static org.junit.Assert.*;

public class ExceptionUtilTest {

  @Test
  public void shouldUnwrapThrowable() {
    Exception exception = new Exception();
    assertEquals(exception, ExceptionUtil.unwrapThrowable(exception));
    assertEquals(exception, ExceptionUtil.unwrapThrowable(new InvocationTargetException(exception, "test")));
    assertEquals(exception, ExceptionUtil.unwrapThrowable(new UndeclaredThrowableException(exception, "test")));
  }


}
