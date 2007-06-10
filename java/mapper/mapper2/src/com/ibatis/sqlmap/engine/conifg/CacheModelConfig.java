package com.ibatis.sqlmap.engine.conifg;

import com.ibatis.sqlmap.engine.cache.*;
import com.ibatis.sqlmap.engine.impl.*;
import com.ibatis.sqlmap.engine.scope.*;
import com.ibatis.sqlmap.engine.type.*;

import java.util.Properties;

public class CacheModelConfig {
  private ErrorContext errorContext;
  private CacheModel cacheModel;
  private Properties properties;

  CacheModelConfig(SqlMapConfiguration config, String id, String type, Boolean readOnly, Boolean serialize) {
    this.errorContext = config.getErrorContext();
    this.cacheModel = new CacheModel();
    this.properties = new Properties();
    ExtendedSqlMapClient client = config.getClient();
    TypeHandlerFactory typeHandlerFactory = config.getTypeHandlerFactory();
    errorContext.setActivity("building a cache model");
    type = typeHandlerFactory.resolveAlias(type);
    if (readOnly != null) {
      cacheModel.setReadOnly(readOnly.booleanValue());
    } else {
      cacheModel.setReadOnly(true);
    }
    if (serialize != null) {
      cacheModel.setSerialize(serialize.booleanValue());
    } else {
      cacheModel.setSerialize(false);
    }
    errorContext.setObjectId(id + " cache model");
    errorContext.setMoreInfo("Check the cache model type.");
    cacheModel.setId(id);
    cacheModel.setResource(errorContext.getResource());
    try {
      cacheModel.setControllerClassName(type);
    } catch (Exception e) {
      throw new RuntimeException("Error setting Cache Controller Class.  Cause: " + e, e);
    }
    errorContext.setMoreInfo("Check the cache model configuration.");
    if (client.getDelegate().isCacheModelsEnabled()) {
      client.getDelegate().addCacheModel(cacheModel);
    }
    errorContext.setMoreInfo(null);
    errorContext.setObjectId(null);
  }

  public void setProperty(String name, String value) {
    properties.setProperty(name, value);
  }

  public void setFlushInterval(int hours, int minutes, int seconds, int milliseconds) {
    errorContext.setMoreInfo("Check the cache model flush interval.");
    long t = 0;
    t += milliseconds;
    t += seconds * 1000;
    t += minutes * 60 * 1000;
    t += hours * 60 * 60 * 1000;
    if (t < 1)
      throw new RuntimeException("A flush interval must specify one or more of milliseconds, seconds, minutes or hours.");
    cacheModel.setFlushInterval(t);
  }

  public void addFlushTriggerStatement(String statement) {
    errorContext.setMoreInfo("Check the cache model flush on statement elements.");
    cacheModel.addFlushTriggerStatement(statement);
  }

  public void saveCacheModel() {
    cacheModel.configure(properties);
  }

}
