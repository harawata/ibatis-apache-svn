package org.apache.ibatis.executor;

import org.junit.Test;

import java.sql.Connection;

public class ReuseExecutorTest extends BaseExecutorTest {

  @Test
  public void dummy() {
  }

  protected Executor createExecutor(Connection connection) {
    return new ReuseExecutor(connection);
  }
}
