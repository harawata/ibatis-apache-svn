package org.apache.ibatis.cache;

import org.apache.ibatis.cache.decorators.SerializedCache;
import org.apache.ibatis.cache.impl.SoftCache;
import org.junit.*;

public class SoftCacheTest {

  @Test
  public void shouldDemonstrateObjectsBeingCollectedAsNeeded() throws Exception {
    SoftCache cache = new SoftCache();
    int n = 100000;
    for (int i = 0; i < n; i++) {
      byte[] array = new byte[5001]; //waste a bunch of memory
      array[5000] = 1;
      cache.putObject(i, array);

      Object value = cache.getObject(i);
      //Assert.assertTrue(value == null || value.equals(String.valueOf(i)));
    }
    Assert.assertTrue(cache.getSize() < n);
  }

  @Test
  public void shouldDemonstrateCopiesAreEqual() {
    Cache cache = new SoftCache();
    cache = new SerializedCache(cache);
    for (int i = 0; i < 1000; i++) {
      cache.putObject(i, i);
      Object value = cache.getObject(i);
      Assert.assertTrue(value == null || value.equals(i));
    }
  }

  @Test
  public void shouldRemoveItemOnDemand() {
    Cache cache = new SoftCache();
    cache.putObject(0, 0);
    Assert.assertNotNull(cache.getObject(0));
    cache.removeObject(0);
    Assert.assertNull(cache.getObject(0));
  }

  @Test
  public void shouldFlushAllItemsOnDemand() {
    Cache cache = new SoftCache();
    for (int i = 0; i < 5; i++) {
      cache.putObject(i, i);
    }
    Assert.assertNotNull(cache.getObject(0));
    Assert.assertNotNull(cache.getObject(4));
    cache.clear();
    Assert.assertNull(cache.getObject(0));
    Assert.assertNull(cache.getObject(4));
  }

}