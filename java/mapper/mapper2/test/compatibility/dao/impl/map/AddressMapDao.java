/**
 * User: Clinton Begin
 * Date: Mar 10, 2003
 * Time: 8:22:25 PM
 */
package compatibility.dao.impl.map;

import compatibility.dao.iface.AddressDao;
import compatibility.domain.Address;
import com.ibatis.db.dao.DaoException;
import com.ibatis.db.sqlmap.SqlMap;

public class AddressMapDao extends BaseMapDao implements AddressDao {

  public Address getAddress(int addressId) throws DaoException {
    try {
      // See BaseMapDao for how the sqlMap is retreived.
      SqlMap sqlMap = getSqlMapFromLocalTransaction();
      return (Address) sqlMap.executeQueryForObject("getAddress", new Integer(addressId));
    } catch (Exception e) {
      throw new DaoException(e.toString());
    }
  }

}
