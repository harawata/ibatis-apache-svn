package com.ibatis.dao.engine.transaction.ojb;

import com.ibatis.dao.client.DaoException;
import com.ibatis.dao.client.DaoTransaction;
import org.apache.ojb.broker.PersistenceBroker;

public class OjbBrokerDaoTransaction
    implements DaoTransaction {

  private PersistenceBroker broker;

  public OjbBrokerDaoTransaction(final PersistenceBroker brk) {

    broker = brk;

    try {
      broker.beginTransaction();
    } catch (final Throwable t) {
      throw new DaoException(t);
    }
  }

  public void commit() {
    try {
      broker.commitTransaction();
    } catch (final Throwable t) {
      throw new DaoException(t);
    }
  }

  public void rollback() {
    try {
      broker.abortTransaction();
    } catch (final Throwable t) {
      throw new DaoException(t);
    }
  }

  public PersistenceBroker getBroker() {
    return broker;
  }

}
