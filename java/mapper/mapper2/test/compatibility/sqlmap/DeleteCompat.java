/**
 * User: Clinton Begin
 * Date: Mar 8, 2003
 * Time: 7:43:17 PM
 */
package compatibility.sqlmap;

import com.ibatis.db.sqlmap.SqlMap;
import compatibility.domain.Account;
import compatibility.BaseCompat;

import java.sql.*;

public class DeleteCompat extends BaseCompat {

  public static void deleteCompatibility(int id)
      throws SQLException {
    // Get our configured SqlMap instance
    SqlMap sqlMap = SqlMapConfigCompat.getSqlMap();

    try {
      // -- Start the transaction
      sqlMap.startTransaction();

      // -- Get the existing account from the database
      Account newAccount = (Account) sqlMap.executeQueryForObject("getAccount", new Integer(id));

      // -- DELETE the Account
      sqlMap.executeUpdate("deleteAccount", newAccount);

      // -- Commit the transaction
      sqlMap.commitTransaction();

      // -- Check to see if the record was deleted correctly (just for testing)
      checkDelete(sqlMap, id);
    } catch (Exception e) {
      sqlMap.rollbackTransaction();
      e.printStackTrace();
      throw new SQLException(e.toString());
    }
  }

  private static void checkDelete(SqlMap sqlMap, int id) throws SQLException {
    // -- Check to see if the record was deleted correctly (just for testing)
    sqlMap.startTransaction();
    Account account = (Account) sqlMap.executeQueryForObject("getAccount", new Integer(id));
    if (account != null) throw new SQLException("Account was not deleted from the database correctly.");
    println("Deleted (should be null): " + account);
    sqlMap.commitTransaction();
  }


}
