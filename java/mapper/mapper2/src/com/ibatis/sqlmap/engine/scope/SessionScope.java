/*
 *  Copyright 2004 Clinton Begin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.ibatis.sqlmap.engine.scope;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapExecutor;
import com.ibatis.sqlmap.client.SqlMapTransactionManager;
import com.ibatis.sqlmap.engine.transaction.Transaction;
import com.ibatis.sqlmap.engine.transaction.TransactionState;

public class SessionScope extends BaseScope {

  private static long nextId;

  private long id;

  // Used by Any
  private SqlMapClient sqlMapClient;
  private SqlMapExecutor sqlMapExecutor;
  private SqlMapTransactionManager sqlMapTxMgr;

  // Used by TransactionManager
  private Transaction transaction;
  private TransactionState transactionState;

  // Used by SqlMapExecutorDelegate.setUserProvidedTransaction()
  private TransactionState savedTransactionState;

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

  public void saveTransactionState() {
    savedTransactionState = transactionState;
  }

  public void recallTransactionState() {
    transactionState = savedTransactionState;
  }
}
