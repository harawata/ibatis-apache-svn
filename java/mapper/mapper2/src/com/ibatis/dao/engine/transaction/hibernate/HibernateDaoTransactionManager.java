package com.ibatis.dao.engine.transaction.hibernate;

import com.ibatis.common.resources.Resources;
import com.ibatis.dao.client.DaoException;
import com.ibatis.dao.client.DaoTransaction;
import com.ibatis.dao.engine.transaction.DaoTransactionManager;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.cfg.Configuration;

import java.util.Iterator;
import java.util.Properties;

/**
 * <p/>
 * Date: Jan 27, 2004 10:49:28 PM
 *
 * @author Clinton Begin
 */
public class HibernateDaoTransactionManager implements DaoTransactionManager {

  private SessionFactory factory;

  public void configure(Properties properties) {
    try {
      Configuration config = new Configuration();

      Iterator it = properties.keySet().iterator();
      while (it.hasNext()) {
        String key = (String) it.next();
        String value = (String) properties.get(key);
        if (key.startsWith("class.")) {
          config.addClass(Resources.classForName(value));
        }
      }

      Properties props = new Properties();
      props.putAll(properties);
      config.setProperties(props);

      factory = config.buildSessionFactory();

    } catch (Exception e) {
      throw new DaoException("Error configuring Hibernate.  Cause: " + e);
    }
  }

  public DaoTransaction startTransaction() {
    return new HibernateDaoTransaction(factory);
  }

  public void commitTransaction(DaoTransaction trans) {
    ((HibernateDaoTransaction) trans).commit();
  }

  public void rollbackTransaction(DaoTransaction trans) {
    ((HibernateDaoTransaction) trans).rollback();
  }
}
