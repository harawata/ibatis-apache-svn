/**
 * User: Clinton Begin
 * Date: Jun 3, 2003
 * Time: 9:50:16 PM
 */
package compatibility.dao.impl.map;

import com.ibatis.db.sqlmap.*;
import com.ibatis.db.dao.*;
import com.ibatis.db.dao.jdbc.*;

public class BaseMapDao implements Dao {

  /**
   * This method gets the SQL Map from the local transaction
   * in 3 easy steps.
   */
  protected SqlMap getSqlMapFromLocalTransaction() throws DaoException {
    // Step 1
    // DaoManager has the ability to look up a DaoManager instance
    // based on the Dao that it created (i.e. "who's your daddy?").
    DaoManager daoManager = DaoManager.getInstance(this);

    // Step 2
    // We're using the DaoManager's transaction management, so
    // here we grab the local transaction.  Because we know these
    // are "Map" DAOs, we know that the transaction has to be an
    // SqlMapDaoTransaction instance, so we cast it.
    SqlMapDaoTransaction trans = (SqlMapDaoTransaction) daoManager.getLocalTransaction();

    // Step 3
    // The SqlMapDaoTransaction can give us the sqlMap instance.
    SqlMap sqlMap = trans.getSqlMap();
    return sqlMap;
  }

}
