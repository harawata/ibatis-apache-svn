package com.ibatis.db.jdbc;

import java.util.*;

/**
 * SimpleDataSource alias for 1.x compatibility
 *
 * <p>
 * Date: Jan 16, 2004 7:52:04 PM
 * @author Clinton Begin
 */
public class SimpleDataSource extends com.ibatis.common.jdbc.SimpleDataSource {

  public SimpleDataSource(Map props) {
    super(props);
  }

}
