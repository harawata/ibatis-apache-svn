package org.apache.ibatis.executor;

import org.junit.Test;

public class ErrorContextTest {

  @Test
  public void shouldShowProgressiveErrorContextBuilding() {
    ErrorContext context = new ErrorContext();
    context.set("somefile.xml", "some activity", "some object", "Here's more info.");
    context.toString().startsWith("*** The error occurred in somefile.xml.");
    context.reset();

    context.set("some activity", "some object", "Here's more info.");
    context.toString().startsWith("*** The error occurred while some activity.");
    context.reset();

    context.set("some object", "Here's more info.");
    context.toString().startsWith("*** Check some object.");
    context.reset();

    context.set("Here's more info.");
    context.toString().startsWith("*** Here's more info.");
    context.reset();

    context.set(new Exception("test"));
    context.toString().startsWith("*** Cause: java.lang.Exception: test");
    context.reset();

  }

}
