package com.ibatis.sqlmap.engine.impl;

import com.ibatis.sqlmap.client.*;
import com.ibatis.sqlmap.engine.mapping.statement.*;
import com.ibatis.sqlmap.engine.execution.*;

/**
 * User: Clinton Begin
 * Date: Dec 28, 2003
 * Time: 4:31:14 PM
 */
public interface ExtendedSqlMapClient extends SqlMapClient {

  public SqlMapExecutorDelegate getDelegate();

  public MappedStatement getMappedStatement(String id);

  public SqlExecutor getSqlExecutor();

  public boolean isLazyLoadingEnabled();

  public boolean isEnhancementEnabled();

}
