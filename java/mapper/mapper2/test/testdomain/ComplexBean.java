package testdomain;

import java.io.Serializable;
import java.util.Map;

/**
 * User: Clinton Begin
 * Date: Jan 7, 2004
 * Time: 10:39:07 PM
 */
public class ComplexBean implements Serializable {

  private Map map;

  public Map getMap() {
    return map;
  }

  public void setMap(Map map) {
    this.map = map;
  }

}
