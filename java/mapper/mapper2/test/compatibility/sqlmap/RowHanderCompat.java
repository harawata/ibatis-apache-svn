/**
 * User: Clinton Begin
 * Date: Feb 9, 2003
 * Time: 6:51:55 PM
 */
package compatibility.sqlmap;

import com.ibatis.db.sqlmap.RowHandler;

import compatibility.BaseCompat;

public class RowHanderCompat implements RowHandler {

  public void handleRow(Object object) {
    BaseCompat.println("UsingRowHandler: " + object);
  }

}
