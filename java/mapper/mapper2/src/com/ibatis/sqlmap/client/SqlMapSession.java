package com.ibatis.sqlmap.client;

import javax.sql.*;
import java.sql.*;

/**
 * A single threaded session for working with your SQL Maps.  This interface inherits transaction control
 * and execution methods from the SqlMapTransactionManager and SqlMapExecutor interfaces.
 * <P>
 * Date: Sep 5, 2003 8:36:11 PM
 * @author Clinton Begin
 * @see SqlMapClient, SqlMapSession, SqlMapExecutor 
 */
public interface SqlMapSession extends SqlMapExecutor, SqlMapTransactionManager {

  public void close();

}
