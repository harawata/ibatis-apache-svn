package org.apache.ibatis.adhoc;

import org.apache.ibatis.type.*;
import org.junit.*;

public class NullTest {

  @Test
  public void shouldGetTypeAndTypeHandlerForNullStringType() {
    Assert.assertEquals(JdbcType.VARCHAR, Null.STRING.getJdbcType());
    Assert.assertTrue(Null.STRING.getTypeHandler() instanceof StringTypeHandler);
  }

}
