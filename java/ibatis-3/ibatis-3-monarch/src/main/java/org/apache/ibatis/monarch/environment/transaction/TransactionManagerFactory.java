package org.apache.ibatis.monarch.environment.transaction;

import java.util.Properties;

public interface TransactionManagerFactory {

  void setProperties(Properties props);

  TransactionManager getTransactionManager();

}
