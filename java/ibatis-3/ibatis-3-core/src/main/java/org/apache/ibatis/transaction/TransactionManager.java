package org.apache.ibatis.transaction;

import java.sql.Connection;

public interface TransactionManager {
  //TODO:  Create transaction class, wrap connection passed to executor

  Transaction newTransaction(Connection conn);

}
