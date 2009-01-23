package org.apache.ibatis.reflection;

import domain.jpetstore.Product;
import domain.misc.RichType;
import org.junit.*;
import static org.junit.Assert.*;

public class MetaObjectTest {

  @Test
  public void shouldGetAndSetField() {
    RichType rich = new RichType();
    MetaObject meta = MetaObject.forObject(rich);
    meta.setValue("richField", "foo");
    assertEquals("foo", meta.getValue("richField"));
  }

  @Test
  public void shouldGetAndSetNestedField() {
    RichType rich = new RichType();
    MetaObject meta = MetaObject.forObject(rich);
    meta.setValue("richType.richField", "foo");
    assertEquals("foo", meta.getValue("richType.richField"));
  }

  @Test
  public void shouldGetAndSetProperty() {
    RichType rich = new RichType();
    MetaObject meta = MetaObject.forObject(rich);
    meta.setValue("richProperty", "foo");
    assertEquals("foo", meta.getValue("richProperty"));
  }

  @Test
  public void shouldGetAndSetNestedProperty() {
    RichType rich = new RichType();
    MetaObject meta = MetaObject.forObject(rich);
    meta.setValue("richType.richProperty", "foo");
    assertEquals("foo", meta.getValue("richType.richProperty"));
  }

  @Test
  public void shouldGetAndSetMapPair() {
    RichType rich = new RichType();
    MetaObject meta = MetaObject.forObject(rich);
    meta.setValue("richMap.key", "foo");
    assertEquals("foo", meta.getValue("richMap.key"));
  }

  @Test
  public void shouldGetAndSetMapPairUsingArraySyntax() {
    RichType rich = new RichType();
    MetaObject meta = MetaObject.forObject(rich);
    meta.setValue("richMap[key]", "foo");
    assertEquals("foo", meta.getValue("richMap[key]"));
  }

  @Test
  public void shouldGetAndSetNestedMapPair() {
    RichType rich = new RichType();
    MetaObject meta = MetaObject.forObject(rich);
    meta.setValue("richType.richMap.key", "foo");
    assertEquals("foo", meta.getValue("richType.richMap.key"));
  }

  @Test
  public void shouldGetAndSetNestedMapPairUsingArraySyntax() {
    RichType rich = new RichType();
    MetaObject meta = MetaObject.forObject(rich);
    meta.setValue("richType.richMap[key]", "foo");
    assertEquals("foo", meta.getValue("richType.richMap[key]"));
  }

  @Test
  public void shouldGetAndSetListItem() {
    RichType rich = new RichType();
    MetaObject meta = MetaObject.forObject(rich);
    meta.setValue("richList[0]", "foo");
    assertEquals("foo", meta.getValue("richList[0]"));
  }

  @Test
  public void shouldSetAndGetSelfListItem() {
    RichType rich = new RichType();
    MetaObject meta = MetaObject.forObject(rich);
    meta.setValue("richList[0]", "foo");
    assertEquals("foo", meta.getValue("richList[0]"));
  }

  @Test
  public void shouldGetAndSetNestedListItem() {
    RichType rich = new RichType();
    MetaObject meta = MetaObject.forObject(rich);
    meta.setValue("richType.richList[0]", "foo");
    assertEquals("foo", meta.getValue("richType.richList[0]"));
  }

  @Test
  public void shouldGetReadablePropertyNames() {
    RichType rich = new RichType();
    MetaObject meta = MetaObject.forObject(rich);
    String[] readables = meta.getGetterNames();
    assertEquals(5, readables.length);
    for (String readable : readables) {
      assertTrue(meta.hasGetter(readable));
      assertTrue(meta.hasGetter("richType." + readable));
    }
    assertTrue(meta.hasGetter("richType"));
  }

  @Test
  public void shouldGetWriteablePropertyNames() {
    RichType rich = new RichType();
    MetaObject meta = MetaObject.forObject(rich);
    String[] writeables = meta.getSetterNames();
    assertEquals(5, writeables.length);
    for (String writeable : writeables) {
      assertTrue(meta.hasSetter(writeable));
      assertTrue(meta.hasSetter("richType." + writeable));
    }
    assertTrue(meta.hasSetter("richType"));
  }

  @Test
  public void shouldSetPropertyOfNullNestedProperty() {
    MetaObject richWithNull = MetaObject.forObject(new RichType());
    richWithNull.setValue("richType.richProperty", "foo");
    assertEquals("foo", richWithNull.getValue("richType.richProperty"));
  }

  @Test
  public void shouldSetPropertyOfNullNestedPropertyWithNull() {
    MetaObject richWithNull = MetaObject.forObject(new RichType());
    richWithNull.setValue("richType.richProperty", null);
    assertEquals(null, richWithNull.getValue("richType.richProperty"));
  }

  @Test
  public void shouldGetPropertyOfNullNestedProperty() {
    MetaObject richWithNull = MetaObject.forObject(new RichType());
    assertNull(richWithNull.getValue("richType.richProperty"));
  }

  @Test
  public void shouldVerifyHasReadablePropertiesReturnedByGetReadablePropertyNames() {
    MetaObject object = MetaObject.forObject(new Product());
    for (String readable : object.getGetterNames()) {
      assertTrue(object.hasGetter(readable));
    }
  }

  @Test
  public void shouldVerifyHasWriteablePropertiesReturnedByGetWriteablePropertyNames() {
    MetaObject object = MetaObject.forObject(new Product());
    for (String writeable : object.getSetterNames()) {
      assertTrue(object.hasSetter(writeable));
    }
  }

  @Test
  public void shouldSetAndGetProperties() {
    MetaObject object = MetaObject.forObject(new Product());
    for (String writeable : object.getSetterNames()) {
      if (!writeable.contains("$")) {
        object.setValue(writeable, "test");
        assertEquals("test", object.getValue(writeable));
      }
    }
  }

  @Test
  public void shouldVerifyPropertyTypes() {
    MetaObject object = MetaObject.forObject(new Product());
    for (String writeable : object.getSetterNames()) {
      if (!writeable.contains("$")) {
        assertEquals(String.class, object.getGetterType(writeable));
        assertEquals(String.class, object.getSetterType(writeable));
      }
    }
  }


}
