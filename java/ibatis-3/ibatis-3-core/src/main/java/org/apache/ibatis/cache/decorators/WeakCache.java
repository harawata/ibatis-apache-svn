package org.apache.ibatis.cache.decorators;

import org.apache.ibatis.cache.impl.BaseCache;
import org.apache.ibatis.cache.Cache;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.lang.ref.*;

/**
 * Weak Reference cache decorator
 * Thanks to Dr. Heinz Kabutz for his guidance here.
 */
public class WeakCache extends BaseCache {
  private final int numberOfHardLinks;
  private final LinkedList hardLinksToAvoidGarbageCollection = new LinkedList();
  private final ReferenceQueue queueOfGarbageCollectedEntries = new ReferenceQueue();
  private final Cache delegate;

  public WeakCache(Cache delegate) {
    this(delegate,256);
  }

  public WeakCache(Cache delegate, int numberOfHardLinks) {
    this.delegate = delegate;
    this.numberOfHardLinks = numberOfHardLinks;
  }

  public String getId() {
    return delegate.getId();
  }

  public int getSize() {
    removeGarbageCollectedItems();
    return delegate.getSize();
  }

  public void putObject(Object key, Object value) {
    removeGarbageCollectedItems();
    delegate.putObject(key, new WeakEntry(key, value, queueOfGarbageCollectedEntries));
  }

  public Object getObject(Object key) {
    Object result = null;
    WeakReference weakReference = (WeakReference) delegate.getObject(key);
    if (weakReference != null) {
      result = weakReference.get();
      if (result == null) {
        delegate.removeObject(key);
      } else {
        hardLinksToAvoidGarbageCollection.addFirst(result);
        if (hardLinksToAvoidGarbageCollection.size() > numberOfHardLinks) {
          hardLinksToAvoidGarbageCollection.removeLast();
        }
      }
    }
    return result;
  }

  public boolean hasKey(Object key) {
    return delegate.hasKey(key);
  }

  public Object removeObject(Object key) {
    removeGarbageCollectedItems();
    return delegate.removeObject(key);
  }

  public void clear() {
    hardLinksToAvoidGarbageCollection.clear();
    removeGarbageCollectedItems();
    delegate.clear();
  }

  public ReadWriteLock getReadWriteLock() {
    return delegate.getReadWriteLock();
  }

  private void removeGarbageCollectedItems() {
    WeakEntry sv;
    while ((sv = (WeakEntry) queueOfGarbageCollectedEntries.poll()) != null) {
      delegate.removeObject(sv.key);
    }
  }

  private static class WeakEntry extends WeakReference {
    private final Object key;
    private WeakEntry(Object key, Object value, ReferenceQueue garbageCollectionQueue) {
      super(value, garbageCollectionQueue);
      this.key = key;
    }
  }

}
