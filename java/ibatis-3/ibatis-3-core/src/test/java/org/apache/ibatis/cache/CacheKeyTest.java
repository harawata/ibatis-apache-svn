package org.apache.ibatis.cache;

import org.junit.*;

import java.util.Date;

public class CacheKeyTest {

  @Test
  public void shouldTestCacheKeysEqual() {
    Date date = new Date();
    CacheKey key1 = new CacheKey(new Object[]{1, "hello", null, new Date(date.getTime())});
    CacheKey key2 = new CacheKey(new Object[]{1, "hello", null, new Date(date.getTime())});
    Assert.assertTrue(key1.equals(key2));
    Assert.assertTrue(key2.equals(key1));
    Assert.assertTrue(key1.hashCode() == key2.hashCode());
    Assert.assertTrue(key1.toString().equals(key2.toString()));
  }

  @Test
  public void shouldTestCacheKeysNotEqualDueToDateDifference() throws Exception {
    CacheKey key1 = new CacheKey(new Object[]{1, "hello", null, new Date()});
    Thread.sleep(1000);
    CacheKey key2 = new CacheKey(new Object[]{1, "hello", null, new Date()});
    Assert.assertFalse(key1.equals(key2));
    Assert.assertFalse(key2.equals(key1));
    Assert.assertFalse(key1.hashCode() == key2.hashCode());
    Assert.assertFalse(key1.toString().equals(key2.toString()));
  }

  @Test
  public void shouldTestCacheKeysNotEqualDueToOrder() throws Exception {
    CacheKey key1 = new CacheKey(new Object[]{1, "hello", null});
    Thread.sleep(1000);
    CacheKey key2 = new CacheKey(new Object[]{1, null, "hello"});
    Assert.assertFalse(key1.equals(key2));
    Assert.assertFalse(key2.equals(key1));
    Assert.assertFalse(key1.hashCode() == key2.hashCode());
    Assert.assertFalse(key1.toString().equals(key2.toString()));
  }

  @Test
  public void shouldDemonstrateEmptyAndNullKeysAreEqual() {
    CacheKey key1 = new CacheKey();
    CacheKey key2 = new CacheKey();
    Assert.assertEquals(key1, key2);
    Assert.assertEquals(key2, key1);
    key1.update(null);
    key2.update(null);
    Assert.assertEquals(key1, key2);
    Assert.assertEquals(key2, key1);
    key1.update(null);
    key2.update(null);
    Assert.assertEquals(key1, key2);
    Assert.assertEquals(key2, key1);
  }

}
