package org.apache.ibatis.monarch.example;

import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;

import java.util.Properties;

public class TestPlugin implements Interceptor {

  public Object intercept(Invocation invocation) throws Throwable {
    return invocation.proceed();
  }

  public Object plugin(Object target) {
    return Plugin.wrap(target,this);
  }

  public void setProperties(Properties properties) {

  }

}
