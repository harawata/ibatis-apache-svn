package com.ibatis.dao.engine.transaction.ojb;


import com.ibatis.dao.client.DaoTransaction;
import com.ibatis.dao.engine.transaction.DaoTransactionManager;
import org.apache.ojb.broker.PersistenceBroker;
import org.apache.ojb.broker.PersistenceBrokerFactory;

import java.util.Properties;

public class OjbBrokerTransactionManager implements DaoTransactionManager {

  private PersistenceBroker broker;

  public void configure(final Properties properties) {
    broker = PersistenceBrokerFactory.defaultPersistenceBroker();
  }

  public DaoTransaction startTransaction() {
    return new OjbBrokerDaoTransaction(broker);
  }

  public void commitTransaction(final DaoTransaction trans) {
    ((OjbBrokerDaoTransaction) trans).commit();
  }

  public void rollbackTransaction(final DaoTransaction trans) {
    ((OjbBrokerDaoTransaction) trans).rollback();
  }
}
