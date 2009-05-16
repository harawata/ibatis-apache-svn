package org.apache.ibatis.builder.annotation;

import org.apache.ibatis.mapping.*;
import org.apache.ibatis.builder.SqlSourceParser;

import java.lang.reflect.Method;

public class ProviderSqlSource implements SqlSource {

  private SqlSourceParser sqlSourceParser;
  private Class providerType;
  private Method providerMethod;
  private boolean providerTakesParameterObject;

  public ProviderSqlSource(Configuration config, Object provider) {
    try {
      this.sqlSourceParser = new SqlSourceParser(config);
      this.providerType = (Class) provider.getClass().getMethod("type").invoke(provider);
      String providerMethod = (String) provider.getClass().getMethod("method").invoke(provider);

      for (Method m : providerType.getMethods()) {
        if (providerMethod.equals(m.getName())) {
          if (m.getParameterTypes().length < 2
              && m.getReturnType() == String.class) {
            this.providerMethod = m;
            this.providerTakesParameterObject = m.getParameterTypes().length == 1;
          }
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Error creating SqlSource for SqlProvider.  Cause: " + e, e);
    }
  }

  public BoundSql getBoundSql(Object parameterObject) {
    SqlSource sqlSource = createSqlSource(parameterObject);
    return sqlSource.getBoundSql(parameterObject);
  }

  private SqlSource createSqlSource(Object parameterObject) {
    try {
      String sql;
      if (providerTakesParameterObject) {
        sql = (String) providerMethod.invoke(providerType.newInstance(), parameterObject);
      } else {
        sql = (String) providerMethod.invoke(providerType.newInstance());
      }
      Class parameterType = parameterObject == null ? Object.class : parameterObject.getClass();
      return sqlSourceParser.parse(sql, parameterType);
    } catch (Exception e) {
      throw new RuntimeException("Error invoking SqlProvider method ("
          + providerType.getName() + "." + providerMethod.getName()
          + ").  Cause: " + e, e);
    }
  }

}
