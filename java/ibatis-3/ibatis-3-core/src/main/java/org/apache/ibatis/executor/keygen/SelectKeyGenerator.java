package org.apache.ibatis.executor.keygen;

import org.apache.ibatis.executor.*;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.reflection.MetaObject;

import java.sql.Statement;

public class SelectKeyGenerator implements KeyGenerator {
  public static final String SELECT_KEY_SUFFIX = "!selectKey";

  public void processGeneratedKeys(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
    try {
      final Configuration configuration = ms.getConfiguration();
      if (parameter != null) {
        String keyStatementName = ms.getId() + SELECT_KEY_SUFFIX;
        if (configuration.hasStatement(keyStatementName)) {
          MappedStatement keyStatement = configuration.getMappedStatement(keyStatementName);
          if (keyStatement != null) {
            String keyProperty = keyStatement.getKeyProperty();
            final MetaObject metaParam = MetaObject.forObject(parameter);
            if (keyProperty != null && metaParam.hasSetter(keyProperty)) {
              // Do not close keyExecutor.
              // The transaction will be closed by parent executor.
              Executor keyExecutor = configuration.newExecutor(executor.getTransaction(), ExecutorType.SIMPLE);
              Object value = keyExecutor.query(ms, parameter, Executor.NO_ROW_OFFSET, Executor.NO_ROW_LIMIT, Executor.NO_RESULT_HANDLER);
              metaParam.setValue(keyProperty, value);
            }
          }
        }
      }
    } catch (Exception e) {
      throw new ExecutorException("Error selecting key or setting result to parameter object. Cause: " + e, e);
    }
  }

}
