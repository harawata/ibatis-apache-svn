package com.ibatis.sqlmap.engine.transaction;

import com.ibatis.sqlmap.engine.scope.*;
import com.ibatis.common.util.*;

import javax.sql.*;
import java.sql.*;

/**
 * User: Clinton Begin
 * Date: Sep 12, 2003
 * Time: 10:38:21 PM
 */
public class TransactionManager {

  private TransactionConfig transactionConfig;

  private Throttle txThrottle;

  public TransactionManager(TransactionConfig transactionConfig) {
    this.transactionConfig = transactionConfig;
    this.txThrottle = new Throttle(transactionConfig.getMaximumConcurrentTransactions());
  }

  public void begin(SessionScope session) throws SQLException, TransactionException {
    Transaction trans = session.getTransaction();
    TransactionState state = session.getTransactionState();
    if (state == TransactionState.STATE_STARTED) {
      throw new TransactionException("TransactionManager could not start a new transaction.  A transaction is already started.");
    }

    txThrottle.increment();

    try {
      trans = transactionConfig.newTransaction();
      session.setCommitRequired(false);
    } catch (SQLException e) {
      txThrottle.decrement();
      throw e;
    } catch (TransactionException e) {
      txThrottle.decrement();
      throw e;
    }

    session.setTransaction(trans);
    session.setTransactionState(TransactionState.STATE_STARTED);
  }

  public void commit(SessionScope session) throws SQLException, TransactionException {
    Transaction trans = session.getTransaction();
    TransactionState state = session.getTransactionState();
    if (state != TransactionState.STATE_STARTED) {
      throw new TransactionException("TransactionManager could not commit.  No transaction is started.");
    }
    if (session.isCommitRequired()) {
      trans.commit();
      session.setCommitRequired(false);
    }
    session.setTransactionState(TransactionState.STATE_COMMITTED);
  }

  public void end(SessionScope session) throws SQLException, TransactionException {
    Transaction trans = session.getTransaction();
    TransactionState state = session.getTransactionState();
    try {
      if (trans != null) {
        if (state != TransactionState.STATE_COMMITTED) {
          if (session.isCommitRequired()) {
            trans.rollback();
            session.setCommitRequired(false);
          }
        }
        trans.close();
      }
    } finally {

      if (state != TransactionState.STATE_ENDED) {
        txThrottle.decrement();
      }

      session.setTransaction(null);
      session.setTransactionState(TransactionState.STATE_ENDED);
    }
  }

  public DataSource getDataSource() {
    return transactionConfig.getDataSource();
  }

  public void setDataSource(DataSource ds) {
    transactionConfig.setDataSource(ds);
  }

}

