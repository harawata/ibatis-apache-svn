package org.apache.ibatis.executor;

import org.junit.Test;

public class ErrorContextTest {

  @Test
  public void shouldShowProgressiveErrorContextBuilding() {
    ErrorContext.set("somefile.xml", "some activity", "some object", "Here's more info.");
    ErrorContext.description().startsWith("*** The error occurred in somefile.xml.");
    ErrorContext.reset();

    ErrorContext.set("some activity", "some object", "Here's more info.");
    ErrorContext.description().startsWith("*** The error occurred while some activity.");
    ErrorContext.reset();

    ErrorContext.set("some object", "Here's more info.");
    ErrorContext.description().startsWith("*** Check some object.");
    ErrorContext.reset();

    ErrorContext.set("Here's more info.");
    ErrorContext.description().startsWith("*** Here's more info.");
    ErrorContext.reset();

    ErrorContext.set(new Exception("test"));
    ErrorContext.description().startsWith("*** Cause: java.lang.Exception: test");
    ErrorContext.reset();

  }

}
