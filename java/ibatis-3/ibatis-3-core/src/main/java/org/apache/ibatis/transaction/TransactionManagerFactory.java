package org.apache.ibatis.transaction;

import java.util.Properties;

public interface TransactionManagerFactory {

  void setProperties(Properties props);

  TransactionManager getTransactionManager();

}
