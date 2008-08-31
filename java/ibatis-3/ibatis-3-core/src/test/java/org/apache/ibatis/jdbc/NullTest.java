package org.apache.ibatis.jdbc;

import org.apache.ibatis.type.*;
import org.apache.ibatis.jdbc.Null;
import org.junit.*;

public class NullTest {

  @Test
  public void shouldGetTypeAndTypeHandlerForNullStringType() {
    Assert.assertEquals(JdbcType.VARCHAR, Null.STRING.getJdbcType());
    Assert.assertTrue(Null.STRING.getTypeHandler() instanceof StringTypeHandler);
  }

}
