/**
 * User: Clinton Begin
 * Date: Jan 21, 2003
 * Time: 8:13:48 PM
 */
package com.ibatis.sqlmap.engine.datasource;

import com.ibatis.sqlmap.client.*;

import javax.sql.*;
import javax.naming.*;

import java.util.*;

public class JndiDataSourceFactory implements DataSourceFactory {

  public final static String CONTEXT_SETTINGS = "context_settings";

  private DataSource dataSource;

  public void initialize(Map properties) {
    try {
      InitialContext initCtx = null;
      Properties context = getContextProperties(properties);

      if (context == null) {
        initCtx = new InitialContext();
      } else {
        initCtx = new InitialContext(context);
      }

      if (properties.containsKey("DataSource")) {
        dataSource = (DataSource) initCtx.lookup((String) properties.get("DataSource"));
      } else if (properties.containsKey("DBJndiContext")) { // LEGACY --Backward compatibility        
        dataSource = (DataSource) initCtx.lookup((String) properties.get("DBJndiContext"));
      } else if (properties.containsKey("DBFullJndiContext")) { // LEGACY --Backward compatibility
        dataSource = (DataSource) initCtx.lookup((String) properties.get("DBFullJndiContext"));
      } else if (properties.containsKey("DBInitialContext")
          && properties.containsKey("DBLookup")) { // LEGACY --Backward compatibility
        Context ctx = (Context) initCtx.lookup((String) properties.get("DBInitialContext"));
        dataSource = (DataSource) ctx.lookup((String) properties.get("DBLookup"));
      }

    } catch (NamingException e) {
      throw new SqlMapException("There was an error configuring JndiDataSourceDaoTransactionPool. Cause: " + e, e);
    }
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public String[] getExpectedProperties() {
    return new String[]{"DBFullJndiContext"};
  }

  private static Properties getContextProperties(Map allProps) {
    final String PREFIX = "context.";
    Properties contextProperties = null;
    Iterator keys = allProps.keySet().iterator();
    while (keys.hasNext()) {
      String key = (String)keys.next();
      String value = (String)allProps.get(key);
      if (key.startsWith(PREFIX)) {
        if(contextProperties == null) {
          contextProperties = new Properties();
        }
        contextProperties.put(key.substring(PREFIX.length()),value);
      }
    }
    return contextProperties;
  }

}

