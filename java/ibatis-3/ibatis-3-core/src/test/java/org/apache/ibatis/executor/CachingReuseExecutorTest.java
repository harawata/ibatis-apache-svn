package org.apache.ibatis.executor;

import org.junit.Test;

import java.sql.Connection;

public class CachingReuseExecutorTest extends BaseExecutorTest {

  @Test
  public void dummy() {
  }

  protected Executor createExecutor(Connection connection) {
    return new CachingExecutor(new ReuseExecutor(connection));
  }

}