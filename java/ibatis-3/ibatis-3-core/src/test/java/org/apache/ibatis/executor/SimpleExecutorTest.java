package org.apache.ibatis.executor;

import org.junit.Test;
import org.apache.ibatis.transaction.Transaction;

import java.sql.Connection;

public class SimpleExecutorTest extends BaseExecutorTest {

  @Test
  public void dummy() {
  }

  protected Executor createExecutor(Transaction transaction) {
    return new SimpleExecutor(transaction);
  }
}
