/**
 * User: Clinton Begin
 * Date: Mar 10, 2003
 * Time: 8:22:14 PM
 */
package compatibility.dao.impl.map;

import compatibility.dao.iface.AccountDao;
import compatibility.domain.Account;
import com.ibatis.db.dao.DaoException;
import com.ibatis.db.sqlmap.SqlMap;

import java.util.*;

public class AccountMapDao extends BaseMapDao implements AccountDao {


  public Account getAccount(int accountId) throws DaoException {
    try {
      // See BaseMapDao for how the sqlMap is retreived.
      SqlMap sqlMap = getSqlMapFromLocalTransaction();
      return (Account) sqlMap.executeQueryForObject("getAccount", new Integer(accountId));
    } catch (Exception e) {
      throw new DaoException(e.toString());
    }
  }

  public List getAccountList() throws DaoException {
    try {
      // See BaseMapDao for how the sqlMap is retreived.
      SqlMap sqlMap = getSqlMapFromLocalTransaction();
      return sqlMap.executeQueryForList("getAllAccounts", null);
    } catch (Exception e) {
      throw new DaoException(e.toString());
    }
  }

  public void insertAccount(Account account) throws DaoException {
    try {
      // See BaseMapDao for how the sqlMap is retreived.
      SqlMap sqlMap = getSqlMapFromLocalTransaction();
      sqlMap.executeUpdate("insertAccount", account);
    } catch (Exception e) {
      throw new DaoException(e.toString());
    }
  }

  public void updateAccount(Account account) throws DaoException {
    try {
      // See BaseMapDao for how the sqlMap is retreived.
      SqlMap sqlMap = getSqlMapFromLocalTransaction();
      sqlMap.executeUpdate("updateAccount", account);
    } catch (Exception e) {
      throw new DaoException(e.toString());
    }
  }

  public void deleteAccount(Account account) throws DaoException {
    try {
      // See BaseMapDao for how the sqlMap is retreived.
      SqlMap sqlMap = getSqlMapFromLocalTransaction();
      sqlMap.executeUpdate("deleteAccount", account);
    } catch (Exception e) {
      throw new DaoException(e.toString());
    }
  }

}
