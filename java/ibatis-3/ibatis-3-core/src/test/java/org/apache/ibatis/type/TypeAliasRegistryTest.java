package org.apache.ibatis.type;

import org.junit.*;
import static org.junit.Assert.*;

public class TypeAliasRegistryTest {

  @Test
  public void shouldRegisterAndResolveTypeAlias() {
    TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();

    typeAliasRegistry.registerAlias("rich", "domain.misc.RichType");

    assertEquals("domain.misc.RichType", typeAliasRegistry.resolveAlias("rich"));
    assertEquals("unknown", typeAliasRegistry.resolveAlias("unknown"));
  }

}
