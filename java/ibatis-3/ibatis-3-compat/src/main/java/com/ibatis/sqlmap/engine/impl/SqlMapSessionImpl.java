package com.ibatis.sqlmap.engine.impl;

import com.ibatis.common.util.PaginatedList;
import com.ibatis.sqlmap.client.*;
import com.ibatis.sqlmap.client.event.RowHandler;
import com.ibatis.sqlmap.engine.builder.Ibatis2Configuration;
import com.ibatis.sqlmap.engine.execution.BatchException;
import com.ibatis.sqlmap.engine.mapping.statement.PaginatedDataList;
import com.ibatis.sqlmap.engine.transaction.*;
import org.apache.ibatis.executor.*;
import org.apache.ibatis.executor.result.ResultHandler;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.reflection.MetaObject;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public class SqlMapSessionImpl implements SqlMapSession {

  protected Ibatis2Configuration configuration;
  private TransactionManager transactionManager;

  public SqlMapSessionImpl(Ibatis2Configuration configuration) {
    this.configuration = configuration;
    this.transactionManager = configuration.getTransactionManager();
  }

  public TransactionManager getTransactionManager() {
    return transactionManager;
  }

  public SqlMapSessionImpl(Ibatis2Configuration configuration, Connection conn) {
    this(configuration);
    try {
      setUserConnection(conn);
    } catch (SQLException e) {
      throw new SqlMapException("Could not create SqlMapSession because the provided user " +
          "connection could not be set. Cause: " + e, e);
    }
  }

  public Object insert(String id) throws SQLException {
    return insert(id, null);
  }

  public Object insert(final String id, final Object parameterObject) throws SQLException {
    int key;
    boolean postSelectKey = configuration.isPostSelectKey(selectKeyIdFor(id));
    if (postSelectKey) {
      update(id, parameterObject);
      key = getKey(id, parameterObject);
    } else {
      key = getKey(id, parameterObject);
      update(id, parameterObject);
    }
    return key == Integer.MIN_VALUE ? null : key;
  }

  public int delete(String id, Object parameterObject) throws SQLException {
    return update(id, parameterObject);
  }

  public int delete(String id) throws SQLException {
    return update(id, null);
  }

  public int update(String id) throws SQLException {
    return update(id, null);
  }

  public int update(final String id, final Object parameterObject) throws SQLException {
    return (Integer) transactionManager.doInTransaction(new TransactionScope() {
      public Object execute(Transaction transaction) throws SQLException {
        transaction.setCommitRequired(true);
        MappedStatement ms = configuration.getMappedStatement(id);
        Executor executor = transaction.getExecutor();
        return executor.update(ms, wrapCollection(parameterObject));
      }
    });
  }

  public Object queryForObject(String id) throws SQLException {
    return queryForObject(id, null);
  }

  public Object queryForObject(String id, Object parameterObject, Object userObject) throws SQLException {
    Object systemObject = queryForObject(id, parameterObject);
    if (systemObject != null && userObject != null) {
      MappedStatement ms = configuration.getMappedStatement(id);
      for (ResultMap rm : ms.getResultMaps()) {
        for (ResultMapping mapping : rm.getPropertyResultMappings()) {
          MetaObject metaUserObject = MetaObject.forObject(userObject);
          MetaObject metaSystemObject = MetaObject.forObject(systemObject);
          String propName = mapping.getProperty();
          if (propName != null) {
            metaUserObject.setValue(propName, metaSystemObject.getValue(propName));
          }
        }
      }
    }
    return userObject;
  }

  public Object queryForObject(final String id, final Object parameterObject) throws SQLException {
    return transactionManager.doInTransaction(new TransactionScope() {
      public Object execute(Transaction transaction) throws SQLException {
        MappedStatement ms = configuration.getMappedStatement(id);
        Executor executor = transaction.getExecutor();
        List list = executor.query(ms, wrapCollection(parameterObject), Executor.NO_ROW_OFFSET, Executor.NO_ROW_LIMIT, null);
        if (list.size() == 1) {
          return list.get(0);
        } else if (list.size() > 1) {
          throw new SQLException("queryForObject() returned more than one row.");
        } else {
          return null;
        }
      }
    });
  }

  public List queryForList(final String id, final Object parameterObject, final int skip, final int max) throws SQLException {
    return (List) transactionManager.doInTransaction(new TransactionScope() {
      public Object execute(Transaction transaction) throws SQLException {
        Executor executor = transaction.getExecutor();
        MappedStatement ms = configuration.getMappedStatement(id);
        return executor.query(ms, wrapCollection(parameterObject), skip, max, null);
      }
    });
  }

  public List queryForList(String id, Object parameterObject) throws SQLException {
    return queryForList(id, parameterObject, Executor.NO_ROW_OFFSET, Executor.NO_ROW_LIMIT);
  }

  public List queryForList(String id, int skip, int max) throws SQLException {
    return queryForList(id, null, skip, max);
  }

  public List queryForList(String id) throws SQLException {
    return queryForList(id, null);
  }

  public PaginatedList queryForPaginatedList(final String id, final Object param, final int pageSize) throws SQLException {
    final SqlMapExecutor self = this;
    return (PaginatedList) transactionManager.doInTransaction(new TransactionScope() {
      public Object execute(Transaction transaction) throws SQLException {
        return new PaginatedDataList(self, id, wrapCollection(param), pageSize);
      }
    });
  }

  public PaginatedList queryForPaginatedList(String id, int pageSize) throws SQLException {
    return queryForPaginatedList(id, null, pageSize);
  }

  public void queryWithRowHandler(final String id, final Object parameterObject, final RowHandler rowHandler) throws SQLException {
    transactionManager.doInTransaction(new TransactionScope() {
      public Object execute(Transaction transaction) throws SQLException {
        MappedStatement ms = configuration.getMappedStatement(id);
        Executor executor = transaction.getExecutor();
        return executor.query(ms, wrapCollection(parameterObject), Executor.NO_ROW_OFFSET, Executor.NO_ROW_LIMIT, new ResultHandler() {
          public void handleResult(Object resultObject) {
            rowHandler.handleRow(resultObject);
          }
        });
      }
    });
  }

  public void queryWithRowHandler(String id, RowHandler rowHandler) throws SQLException {
    queryWithRowHandler(id, null, rowHandler);
  }

  public Map queryForMap(String id, Object parameterObject, String keyProp) throws SQLException {
    return queryForMap(id, parameterObject, keyProp, null);
  }

  public Map queryForMap(String id, Object parameterObject, String keyProp, String valueProp) throws SQLException {
    List results = queryForList(id, parameterObject);
    Map map = new HashMap();
    for (Object result : results) {
      MetaObject metaResult = MetaObject.forObject(result);
      Object key = metaResult.getValue(keyProp);
      if (valueProp == null) {
        map.put(key, result);
      } else {
        Object value = metaResult.getValue(valueProp);
        map.put(key, value);
      }
    }
    return map;
  }

  public void startBatch() throws SQLException {
  }

  public int executeBatch() throws SQLException {
    try {
      int n = 0;
      for (BatchResult br : transactionManager.getCurrentExecutor().flushStatements()) {
        for (int c : br.getUpdateCounts()) {
          n += c;
        }
      }
      return n;
    } catch (BatchExecutorException e) {
      throw new BatchException(e);
    }
  }

  public List executeBatchDetailed() throws SQLException, BatchException {
    try {
      return transactionManager.getCurrentExecutor().flushStatements();
    } catch (BatchExecutorException e) {
      throw new BatchException(e);
    }
  }

  public void startTransaction() throws SQLException {
    transactionManager.begin();
  }

  public void startTransaction(int transactionIsolation) throws SQLException {
    transactionManager.begin(transactionIsolation);
  }

  public void commitTransaction() throws SQLException {
    try {
      transactionManager.commit();
    } catch (BatchExecutorException e) {
      throw new BatchException(e);
    }
  }

  public void endTransaction() throws SQLException {
    transactionManager.end();
  }

  public void setUserConnection(Connection connection) throws SQLException {
    if (connection != null) {
      transactionManager.begin(connection);
    } else {
      transactionManager.end();
    }
  }

  public Connection getCurrentConnection() throws SQLException {
    return transactionManager.getCurrentConnection();
  }

  public DataSource getDataSource() {
    return configuration.getDataSource();
  }

  public void close() {
    try {
      if (transactionManager.isInTransaction()) {
        transactionManager.end();
      }
    } catch (SQLException e) {
      //Ignore.  There's nothing that can be done at this point.
    }
  }

  public static String selectKeyIdFor(String parentId) {
    return "__" + parentId + "-SelectKey";
  }

  //TODO:  This is a temporary implementation to get a green bar.  To be improved.
  private int getKey(String id, final Object parameterObject) throws SQLException {
    final MappedStatement keyStatement = configuration.getMappedStatement(selectKeyIdFor(id));
    int key = Integer.MIN_VALUE;
    if (keyStatement != null) {
      List results = (List) transactionManager.doInTransaction(new TransactionScope() {
        public Object execute(Transaction transaction) throws SQLException {
          transaction.setCommitRequired(true);
          Executor executor = transaction.getExecutor();
          return executor.query(keyStatement, parameterObject, Executor.NO_ROW_OFFSET, Executor.NO_ROW_LIMIT, null);
        }
      });
      try {
        key = (Integer) ((Map) results.get(0)).values().iterator().next();
      } catch (Exception e) {
        //Ignore...sometimes code sucks.  This is a good example.
      }
    }
    try {
      String property = keyStatement.getResultMaps().get(0).getResultMappings().get(0).getProperty();
      MetaObject.forObject(parameterObject).setValue(property, key);
    } catch (Exception e) {
      //Ignore...sometimes code sucks.  This is a good example.
    }
    return key;
  }

  private Object wrapCollection(Object parameterObject) {
    if (isCollection(parameterObject)) {
      Map map = new HashMap();
      map.put("_collection", parameterObject);
      parameterObject = map;
    }
    return parameterObject;
  }

  private boolean isCollection(Object object) {
    if (object == null) {
      return false;
    } else if (object instanceof Collection) {
      return true;
    } else if (object instanceof Iterator) {
      return true;
    } else if (object.getClass().isArray()) {
      return true;
    }
    return false;
  }


}
