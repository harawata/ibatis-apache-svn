package org.apache.ibatis.executor;

import org.junit.Test;
import org.apache.ibatis.transaction.Transaction;

import java.sql.Connection;

public class CachingBatchExecutorTest extends BaseExecutorTest {

  @Test
  public void dummy() {
  }

  protected Executor createExecutor(Transaction transaction) {
    return new CachingExecutor(new BatchExecutor(transaction));
  }

}