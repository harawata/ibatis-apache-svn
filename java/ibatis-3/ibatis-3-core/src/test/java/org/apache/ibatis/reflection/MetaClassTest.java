package org.apache.ibatis.reflection;

import domain.misc.RichType;
import domain.misc.generics.GenericConcrete;
import org.junit.*;

import java.util.*;

public class MetaClassTest {

  private RichType rich = new RichType();
  Map map = new HashMap() {
    {
      put("richType", rich);
    }
  };

  public MetaClassTest() {
    rich.setRichType(new RichType());
  }

  @Test
  public void shouldTestDataTypeOfGenericMethod() {
    MetaClass meta = MetaClass.forClass(GenericConcrete.class);
    Assert.assertEquals(Long.class, meta.getGetterType("id"));
    Assert.assertEquals(Long.class, meta.getSetterType("id"));
  }

  @Test
  public void shouldCheckGetterExistance() {
    MetaClass meta = MetaClass.forClass(RichType.class);
    Assert.assertTrue(meta.hasGetter("richField"));
    Assert.assertTrue(meta.hasGetter("richProperty"));
    Assert.assertTrue(meta.hasGetter("richList"));
    Assert.assertTrue(meta.hasGetter("richMap"));
    Assert.assertTrue(meta.hasGetter("richList[0]"));

    Assert.assertTrue(meta.hasGetter("richType"));
    Assert.assertTrue(meta.hasGetter("richType.richField"));
    Assert.assertTrue(meta.hasGetter("richType.richProperty"));
    Assert.assertTrue(meta.hasGetter("richType.richList"));
    Assert.assertTrue(meta.hasGetter("richType.richMap"));
    Assert.assertTrue(meta.hasGetter("richType.richList[0]"));

    Assert.assertFalse(meta.hasGetter("[0]"));
  }

  @Test
  public void shouldCheckSetterExistance() {
    MetaClass meta = MetaClass.forClass(RichType.class);
    Assert.assertTrue(meta.hasSetter("richField"));
    Assert.assertTrue(meta.hasSetter("richProperty"));
    Assert.assertTrue(meta.hasSetter("richList"));
    Assert.assertTrue(meta.hasSetter("richMap"));
    Assert.assertTrue(meta.hasSetter("richList[0]"));

    Assert.assertTrue(meta.hasSetter("richType"));
    Assert.assertTrue(meta.hasSetter("richType.richField"));
    Assert.assertTrue(meta.hasSetter("richType.richProperty"));
    Assert.assertTrue(meta.hasSetter("richType.richList"));
    Assert.assertTrue(meta.hasSetter("richType.richMap"));
    Assert.assertTrue(meta.hasSetter("richType.richList[0]"));

    Assert.assertFalse(meta.hasSetter("[0]"));
  }

  @Test
  public void shouldCheckTypeForEachGetter() {
    MetaClass meta = MetaClass.forClass(RichType.class);
    Assert.assertEquals(String.class, meta.getGetterType("richField"));
    Assert.assertEquals(String.class, meta.getGetterType("richProperty"));
    Assert.assertEquals(List.class, meta.getGetterType("richList"));
    Assert.assertEquals(Map.class, meta.getGetterType("richMap"));
    Assert.assertEquals(List.class, meta.getGetterType("richList[0]"));

    Assert.assertEquals(RichType.class, meta.getGetterType("richType"));
    Assert.assertEquals(String.class, meta.getGetterType("richType.richField"));
    Assert.assertEquals(String.class, meta.getGetterType("richType.richProperty"));
    Assert.assertEquals(List.class, meta.getGetterType("richType.richList"));
    Assert.assertEquals(Map.class, meta.getGetterType("richType.richMap"));
    Assert.assertEquals(List.class, meta.getGetterType("richType.richList[0]"));
  }

  @Test
  public void shouldCheckTypeForEachSetter() {
    MetaClass meta = MetaClass.forClass(RichType.class);
    Assert.assertEquals(String.class, meta.getSetterType("richField"));
    Assert.assertEquals(String.class, meta.getSetterType("richProperty"));
    Assert.assertEquals(List.class, meta.getSetterType("richList"));
    Assert.assertEquals(Map.class, meta.getSetterType("richMap"));
    Assert.assertEquals(List.class, meta.getSetterType("richList[0]"));

    Assert.assertEquals(RichType.class, meta.getSetterType("richType"));
    Assert.assertEquals(String.class, meta.getSetterType("richType.richField"));
    Assert.assertEquals(String.class, meta.getSetterType("richType.richProperty"));
    Assert.assertEquals(List.class, meta.getSetterType("richType.richList"));
    Assert.assertEquals(Map.class, meta.getSetterType("richType.richMap"));
    Assert.assertEquals(List.class, meta.getSetterType("richType.richList[0]"));
  }

  @Test
  public void shouldCheckGetterAndSetterNames() {
    MetaClass meta = MetaClass.forClass(RichType.class);
    Assert.assertEquals(5, meta.getGetterNames().length);
    Assert.assertEquals(5, meta.getSetterNames().length);
  }

  @Test
  public void shouldFindPropertyName() {
    MetaClass meta = MetaClass.forClass(RichType.class);
    Assert.assertEquals("richField", meta.findProperty("RICHfield"));
  }

}
