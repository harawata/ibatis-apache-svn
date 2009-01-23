package org.apache.ibatis.jdbc;

import org.apache.ibatis.type.*;
import org.junit.*;
import static org.junit.Assert.*;

public class NullTest {

  @Test
  public void shouldGetTypeAndTypeHandlerForNullStringType() {
    assertEquals(JdbcType.VARCHAR, Null.STRING.getJdbcType());
    assertTrue(Null.STRING.getTypeHandler() instanceof StringTypeHandler);
  }

}
