package org.apache.ibatis.type;

import domain.misc.RichType;
import org.junit.*;

public class TypeHandlerRegistryTest {

  private TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();

  @Test
  public void shouldRegisterAndRetrieveTypeHandler() {
    TypeHandler stringTypeHandler = typeHandlerRegistry.getTypeHandler(String.class);
    typeHandlerRegistry.register(String.class, JdbcType.LONGVARCHAR, stringTypeHandler);
    Assert.assertEquals(stringTypeHandler, typeHandlerRegistry.getTypeHandler(String.class, JdbcType.LONGVARCHAR));

    Assert.assertTrue(typeHandlerRegistry.hasTypeHandler(String.class));
    Assert.assertFalse(typeHandlerRegistry.hasTypeHandler(RichType.class));
    Assert.assertTrue(typeHandlerRegistry.hasTypeHandler(String.class, JdbcType.LONGVARCHAR));
    Assert.assertTrue(typeHandlerRegistry.hasTypeHandler(String.class, JdbcType.INTEGER));
    Assert.assertTrue(typeHandlerRegistry.getUnkownTypeHandler() instanceof UnknownTypeHandler);
  }

}
