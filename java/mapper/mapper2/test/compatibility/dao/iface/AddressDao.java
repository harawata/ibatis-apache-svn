/**
 * User: Clinton Begin
 * Date: Mar 10, 2003
 * Time: 8:21:22 PM
 */
package compatibility.dao.iface;

import com.ibatis.db.dao.Dao;
import com.ibatis.db.dao.DaoException;
import compatibility.domain.Address;

public interface AddressDao extends Dao {

  public Address getAddress(int addressId) throws DaoException;

}
