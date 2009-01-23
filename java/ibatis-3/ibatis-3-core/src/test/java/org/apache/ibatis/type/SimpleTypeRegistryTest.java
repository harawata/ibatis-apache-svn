package org.apache.ibatis.type;

import domain.misc.RichType;
import org.junit.*;
import static org.junit.Assert.*;

public class SimpleTypeRegistryTest {

  @Test
  public void shouldTestIfClassIsSimpleTypeAndReturnTrue() {
    assertTrue(SimpleTypeRegistry.isSimpleType(String.class));
  }

  @Test
  public void shouldTestIfClassIsSimpleTypeAndReturnFalse() {
    assertFalse(SimpleTypeRegistry.isSimpleType(RichType.class));
  }

}
