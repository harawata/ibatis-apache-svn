package com.ibatis.dao.client.template;

import com.ibatis.dao.client.DaoManager;

/**
 * <b>DEPRECATED</b>
 * This is now exactly the same as the JdbcDaoTemplate, and therefore
 * has beend deprecated.  There is no behavioural difference between
 * the two.
 * 
 * @deprecated Use JdbcDaoTemplate instead.  Both have the same
 * interface, so simply changing your code to extend JdbcDaoTemplate
 * should work without any change in behaviour.
 */
public abstract class JtaDaoTemplate extends JdbcDaoTemplate {

  public JtaDaoTemplate(DaoManager daoManager) {
    super(daoManager);
  }

}
