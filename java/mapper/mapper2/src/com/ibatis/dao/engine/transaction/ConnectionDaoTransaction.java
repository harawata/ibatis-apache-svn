package com.ibatis.dao.engine.transaction;

import com.ibatis.dao.client.DaoTransaction;

import java.sql.Connection;

/**
 * User: Clinton
 * Date: 31-Jul-2004
 * Time: 2:55:55 PM
 */
public interface ConnectionDaoTransaction extends DaoTransaction{

  public Connection getConnection();

}
