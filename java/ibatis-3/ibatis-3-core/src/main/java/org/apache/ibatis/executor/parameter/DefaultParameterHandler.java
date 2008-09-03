package org.apache.ibatis.executor.parameter;

import org.apache.ibatis.mapping.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.type.*;
import org.apache.ibatis.executor.ExecutorException;

import java.sql.*;
import java.util.List;

public class DefaultParameterHandler implements ParameterHandler {

  private final TypeHandlerRegistry typeHandlerRegistry;

  private final MappedStatement mappedStatement;
  private final Object parameterObject;

  public DefaultParameterHandler(MappedStatement mappedStatement, Object parameterObject) {
    this.mappedStatement = mappedStatement;
    this.typeHandlerRegistry = mappedStatement.getConfiguration().getTypeHandlerRegistry();
    this.parameterObject = parameterObject;
  }

  public Object getParameterObject() {
    return parameterObject;
  }

  public void setParameters(PreparedStatement ps)
      throws SQLException {
    List<ParameterMapping> parameterMappings = mappedStatement.getDynamicParameterMappings(parameterObject);
    if (parameterMappings != null) {
      MetaObject metaObject = parameterObject == null ? null : MetaObject.forObject(parameterObject);
      for (int i = 0; i < parameterMappings.size(); i++) {
        ParameterMapping parameterMapping = parameterMappings.get(i);
        if (parameterMapping.getMode() != ParameterMode.OUT) {
          Object value;
          if (parameterObject == null) {
            value = null;
          } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
            value = parameterObject;
          } else {
            value = metaObject == null ? null : metaObject.getValue(parameterMapping.getProperty());
          }
          TypeHandler typeHandler = parameterMapping.getTypeHandler();
          if (typeHandler == null) {
            throw new ExecutorException("There was no TypeHandler found for parameter " + parameterMapping.getProperty() + " of statement " + mappedStatement.getId());
          }
          typeHandler.setParameter(ps, i + 1, value, parameterMapping.getJdbcType());
        }
      }
    }
  }

}
