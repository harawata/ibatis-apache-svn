package org.apache.ibatis.binding;

import org.apache.ibatis.mapping.Configuration;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parser.SqlSourceParser;

import java.lang.reflect.Method;
import java.util.List;

public class DynamicInlineSqlSource implements SqlSource {

  private SqlSourceParser sqlSourceParser;
  private Class providerType;
  private Method providerMethod;
  private boolean providerTakesParameterObject;

  public DynamicInlineSqlSource(Configuration config, Object provider) {
    try {
      this.sqlSourceParser = new SqlSourceParser(config);
      this.providerType = (Class)provider.getClass().getMethod("type").invoke(provider);
      String providerMethod = (String)provider.getClass().getMethod("method").invoke(provider);;
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

  public String getSql(Object parameterObject) {
    SqlSource sqlSource = createSqlSource(parameterObject);
    return sqlSource.getSql(parameterObject);
  }

  public List<ParameterMapping> getParameterMappings(Object parameterObject) {
    SqlSource sqlSource = createSqlSource(parameterObject);
    return sqlSource.getParameterMappings(parameterObject);
  }

  private SqlSource createSqlSource(Object parameterObject) {
    try {
      String sql;
      if (providerTakesParameterObject) {
        sql = (String) providerMethod.invoke(providerType.newInstance(), parameterObject);
      } else {
        sql = (String) providerMethod.invoke(providerType.newInstance());
      }
      return sqlSourceParser.parse(sql);
    } catch (Exception e) {
      throw new RuntimeException("Error invoking SqlProvider method ("
          + providerType.getName() + "." + providerMethod.getName()
          + ").  Cause: " + e, e);
    }
  }

}
