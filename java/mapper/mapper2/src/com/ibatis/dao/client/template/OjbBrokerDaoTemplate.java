package com.ibatis.dao.client.template;

import com.ibatis.dao.client.DaoManager;
import com.ibatis.dao.engine.transaction.ojb.OjbBrokerDaoTransaction;
import org.apache.ojb.broker.PersistenceBroker;
import org.apache.ojb.broker.Identity;
import org.apache.ojb.broker.query.Query;
import org.apache.ojb.broker.query.QueryByCriteria;

import java.util.Collection;
import java.util.ArrayList;

/**
 * A DaoTemplate for OJB broker implementations that provides a
 * convenient method to access the broker.

 * This is the base class for OJB DAOs and provides the CRUD
 * pattern necessary methods plus an additional
 * method to retrieve all instances of a given class.
 *
 */
public abstract class OjbBrokerDaoTemplate
    extends DaoTemplate {

    /**
     * The DaoManager that manages this Dao instance will be passed  in as the parameter to this constructor automatically upon  instantiation.
     *
     * @param daoManager
     */
    public OjbBrokerDaoTemplate(final DaoManager daoManager) {
        super(daoManager);
    }

  /**
   * Puts an instance/value object on the persistentence layer.
   *
   * @param vo the value object.
   */
  public final void create(final Object vo) {

      if (vo == null) {
          throw new IllegalArgumentException("The value object to be created is null.");
      }

      PersistenceBroker broker = getPersistenceBroker();

      broker.store(vo);
  }

  /**
   * Retrieves an instance/value object, according to its primary key,
   *
   * @param vo the value object criteria.
   * @return the value object.
   */
  public final Object retrieveByPk(final Object vo) {

      PersistenceBroker broker = getPersistenceBroker();

      Identity identity = new Identity(vo, broker);

      return broker.getObjectByIdentity(identity);
  }

  /**
   * Updates the instance/value object representation on the persistence layer.
   *
   * @param vo the value object.
   */
  public final void update(final Object vo) {

      if (vo == null) {
          throw new IllegalArgumentException("The value object to be updated is null.");
      }

      PersistenceBroker broker = getPersistenceBroker();

      broker.store(vo);
  }

  /**
   * Removes an instance/value object from the persistence layer.
   *
   * @param vo the value object.
   */
  public final void delete(final Object vo) {

      PersistenceBroker broker = getPersistenceBroker();

      broker.delete(vo);
  }

  /**
   * Retrieves all instances of a given class, from the persistence layer.
   *
   * @param clazz the class of the instances to be fetched.
   * @return all instances of the given class.
   */
  public final Collection retrieveExtent(final Class clazz) {

      PersistenceBroker broker = getPersistenceBroker();

      Query query = new QueryByCriteria(clazz, null);
      Collection collection = new ArrayList();

      collection = broker.getCollectionByQuery(query);

      return collection;
  }

  /**
   * Gets the OJB persistence broker associated with the current
   * DaoTransaction that this Dao is working under.
   *
   * @return A Hibernate Session instance.
   */
  protected final PersistenceBroker getPersistenceBroker() {
    OjbBrokerDaoTransaction trans = (OjbBrokerDaoTransaction) daoManager.getTransaction(this);
    return trans.getBroker();
  }

}
