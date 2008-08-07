package org.apache.ibatis.type;

import org.junit.*;

public class TypeAliasRegistryTest {

  @Test
  public void shouldRegisterAndResolveTypeAlias() {
    TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();

    typeAliasRegistry.registerAlias("rich", "domain.misc.RichType");

    Assert.assertEquals("domain.misc.RichType", typeAliasRegistry.resolveAlias("rich"));
    Assert.assertEquals("unknown", typeAliasRegistry.resolveAlias("unknown"));
  }

}
