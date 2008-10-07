package org.apache.ibatis.datasource;

import org.apache.ibatis.monarch.environment.EnvironmentException;

import javax.naming.*;
import javax.sql.DataSource;
import java.util.*;

public class JndiDataSourceFactory implements DataSourceFactory {

  private DataSource dataSource;
  private static final String INITIAL_CONTEXT = "initialContext";
  private static final String DATA_SOURCE = "dataSource";
  private static final String CONTEXT_PREFIX = "context.";

  public void setProperties(Properties properties) {
    try {
      InitialContext initCtx = null;
      Hashtable context = getContextProperties(properties);

      if (context == null) {
        initCtx = new InitialContext();
      } else {
        initCtx = new InitialContext(context);
      }

      if (properties.containsKey(INITIAL_CONTEXT)
          && properties.containsKey(DATA_SOURCE)) {
        Context ctx = (Context) initCtx.lookup((String) properties.get(INITIAL_CONTEXT));
        dataSource = (DataSource) ctx.lookup((String) properties.get(DATA_SOURCE));
      } else if (properties.containsKey(DATA_SOURCE)) {
        dataSource = (DataSource) initCtx.lookup((String) properties.get(DATA_SOURCE));
      }

    } catch (NamingException e) {
      throw new EnvironmentException("There was an error configuring JndiDataSourceTransactionPool. Cause: " + e, e);
    }
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  private static Hashtable getContextProperties(Map allProps) {
    final String PREFIX = CONTEXT_PREFIX;
    Hashtable contextProperties = null;
    Iterator keys = allProps.keySet().iterator();
    while (keys.hasNext()) {
      String key = (String) keys.next();
      String value = (String) allProps.get(key);
      if (key.startsWith(PREFIX)) {
        if (contextProperties == null) {
          contextProperties = new Properties();
        }
        contextProperties.put(key.substring(PREFIX.length()), value);
      }
    }
    return contextProperties;
  }

}
