package com.ibatis.sqlmap.engine.scope;

import com.ibatis.sqlmap.engine.transaction.*;
import com.ibatis.sqlmap.client.*;

/**
 * User: Clinton Begin
 * Date: Jan 2, 2004
 * Time: 8:51:02 AM
 */
public class SessionScope extends BaseScope {

  private static long nextId;

  private long id;

  // Used by Any
  private SqlMapClient sqlMapClient;
  private SqlMapExecutor sqlMapExecutor;
  private SqlMapTransactionManager sqlMapTxMgr;

  // Used by StandardSqlMapClient
  private Transaction userTransaction;

  // Used by TransactionManager
  private Transaction transaction;
  private TransactionState transactionState;

  // Used by StandardSqlMapClient and GeneralStatement
  private boolean inBatch;

  // Used by SqlExecutor
  private Object batch;

  private boolean ignoreDomRoot;

  private boolean commitRequired;

  public SessionScope() {
    reset();
  }

  public SqlMapClient getSqlMapClient() {
    return sqlMapClient;
  }

  public void setSqlMapClient(SqlMapClient sqlMapClient) {
    this.sqlMapClient = sqlMapClient;
  }

  public SqlMapExecutor getSqlMapExecutor() {
    return sqlMapExecutor;
  }

  public void setSqlMapExecutor(SqlMapExecutor sqlMapExecutor) {
    this.sqlMapExecutor = sqlMapExecutor;
  }

  public SqlMapTransactionManager getSqlMapTxMgr() {
    return sqlMapTxMgr;
  }

  public void setSqlMapTxMgr(SqlMapTransactionManager sqlMapTxMgr) {
    this.sqlMapTxMgr = sqlMapTxMgr;
  }

  public Transaction getUserTransaction() {
    return userTransaction;
  }

  public void setUserTransaction(Transaction userTransaction) {
    this.userTransaction = userTransaction;
  }

  public boolean isInBatch() {
    return inBatch;
  }

  public void setInBatch(boolean inBatch) {
    this.inBatch = inBatch;
  }

  public Transaction getTransaction() {
    return transaction;
  }

  public void setTransaction(Transaction transaction) {
    this.transaction = transaction;
  }

  public TransactionState getTransactionState() {
    return transactionState;
  }

  public void setTransactionState(TransactionState transactionState) {
    this.transactionState = transactionState;
  }

  public Object getBatch() {
    return batch;
  }

  public void setBatch(Object batch) {
    this.batch = batch;
  }

  public boolean isIgnoreDomRoot() {
    return ignoreDomRoot;
  }

  public void setIgnoreDomRoot(boolean ignoreDomRoot) {
    this.ignoreDomRoot = ignoreDomRoot;
  }

  public boolean isCommitRequired() {
    return commitRequired;
  }

  public void setCommitRequired(boolean commitRequired) {
    this.commitRequired = commitRequired;
  }

  public void reset() {
    super.reset();
    this.batch = null;
    sqlMapExecutor = null;
    sqlMapTxMgr = null;
    userTransaction = null;
    inBatch = false;
    transaction = null;
    transactionState = null;
    batch = null;
    ignoreDomRoot = false;
    id = getNextId();
  }

  public boolean equals(Object parameterObject) {
    if (this == parameterObject) return true;
    if (!(parameterObject instanceof SessionScope)) return false;

    final SessionScope sessionScope = (SessionScope) parameterObject;

    if (id != sessionScope.id) return false;

    return true;
  }

  public int hashCode() {
    return (int) (id ^ (id >>> 32));
  }

  public synchronized static long getNextId() {
    return nextId++;
  }
}
