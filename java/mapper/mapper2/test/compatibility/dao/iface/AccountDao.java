/**
 * User: Clinton Begin
 * Date: Mar 10, 2003
 * Time: 8:21:15 PM
 */
package compatibility.dao.iface;

import com.ibatis.db.dao.Dao;
import com.ibatis.db.dao.DaoException;

import java.util.*;

import compatibility.domain.Account;


public interface AccountDao extends Dao {

  public Account getAccount(int accountId) throws DaoException;

  public List getAccountList() throws DaoException;

  public void insertAccount(Account account) throws DaoException;

  public void updateAccount(Account account) throws DaoException;

  public void deleteAccount(Account account) throws DaoException;


}
