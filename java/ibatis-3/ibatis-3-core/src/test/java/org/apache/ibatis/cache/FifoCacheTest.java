package org.apache.ibatis.cache;

import org.apache.ibatis.cache.decorators.FifoCache;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.junit.*;

public class FifoCacheTest {

  @Test
  public void shouldRemoveFirstItemInBeyondFiveEntries() {
    FifoCache cache = new FifoCache(new PerpetualCache(),5);
    for (int i = 0; i < 5; i++) {
      cache.putObject(i, i);
    }
    Assert.assertEquals(0, cache.getObject(0));
    cache.putObject(5, 5);
    Assert.assertNull(cache.getObject(0));
    Assert.assertEquals(5, cache.getSize());
  }

  @Test
  public void shouldRemoveItemOnDemand() {
    FifoCache cache = new FifoCache(new PerpetualCache());
    cache.putObject(0, 0);
    Assert.assertNotNull(cache.getObject(0));
    cache.removeObject(0);
    Assert.assertNull(cache.getObject(0));
  }

  @Test
  public void shouldFlushAllItemsOnDemand() {
    FifoCache cache = new FifoCache(new PerpetualCache());
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
