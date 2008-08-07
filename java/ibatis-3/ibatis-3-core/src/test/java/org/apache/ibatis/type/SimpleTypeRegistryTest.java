package org.apache.ibatis.type;

import domain.misc.RichType;
import org.junit.*;

public class SimpleTypeRegistryTest {

  @Test
  public void shouldTestIfClassIsSimpleTypeAndReturnTrue() {
    Assert.assertTrue(SimpleTypeRegistry.isSimpleType(String.class));
  }

  @Test
  public void shouldTestIfClassIsSimpleTypeAndReturnFalse() {
    Assert.assertFalse(SimpleTypeRegistry.isSimpleType(RichType.class));
  }

}
