/**
 * User: Clinton Begin
 * Date: Mar 8, 2003
 * Time: 11:59:49 AM
 */
package compatibility.sqlmap;

import com.ibatis.db.sqlmap.SqlMap;
import compatibility.domain.Account;
import compatibility.BaseCompat;

import java.sql.*;

public class UpdateCompat extends BaseCompat {

  public static void updateCompatibility(int id)
      throws SQLException {
    // Get our configured SqlMap instance
    SqlMap sqlMap = SqlMapConfigCompat.getSqlMap();

    try {
      // -- Start the transaction
      sqlMap.startTransaction();

      // -- Get the existing account from the database
      Account newAccount = (Account) sqlMap.executeQueryForObject("getAccount", new Integer(id));

      // -- UPDATE the Account
      newAccount.setEmailAddress("neo@matrix.net");
      sqlMap.executeUpdate("updateAccount", newAccount);

      // -- Commit the transaction
      sqlMap.commitTransaction();

      // -- Check to see if the record was updated correctly (just for testing)
      checkUpdate(sqlMap, id, newAccount);
    } catch (Exception e) {
      sqlMap.rollbackTransaction();
      e.printStackTrace();
      throw new SQLException(e.toString());
    }
  }

  private static void checkUpdate(SqlMap sqlMap, int id, Account newAccount) throws SQLException {
    // -- Check to see if the record was updated correctly (just for testing)
    sqlMap.startTransaction();
    Account account = (Account) sqlMap.executeQueryForObject("getAccount", new Integer(id));
    if (account == null) throw new SQLException("Account was not updated to the database correctly.");
    boolean isEqual = newAccount.getId() == account.getId();
    if (!isEqual) throw new SQLException("Account was not updated to the database correctly.");
    println("Updated: " + account);
    sqlMap.commitTransaction();
  }

}
