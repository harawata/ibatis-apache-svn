package org.apache.ibatis.cache.decorators;

import org.apache.ibatis.cache.Cache;

import java.lang.ref.*;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * Soft Reference cache decorator
 * Thanks to Dr. Heinz Kabutz for his guidance here.
 */
public class SoftCache implements Cache {
  private final int numberOfHardLinks;
  private final LinkedList hardLinksToAvoidGarbageCollection = new LinkedList();
  private final ReferenceQueue queueOfGarbageCollectedEntries = new ReferenceQueue();
  private final Cache delegate;

  public SoftCache(Cache delegate) {
    this(delegate,256);
  }

  public SoftCache(Cache delegate, int numberOfHardLinks) {
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
    delegate.putObject(key, new SoftEntry(key, value, queueOfGarbageCollectedEntries));
  }

  public Object getObject(Object key) {
    Object result = null;
    SoftReference softReference = (SoftReference) delegate.getObject(key);
    if (softReference != null) {
      result = softReference.get();
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
    SoftEntry sv;
    while ((sv = (SoftEntry) queueOfGarbageCollectedEntries.poll()) != null) {
      delegate.removeObject(sv.key);
    }
  }

  private static class SoftEntry extends SoftReference {
    private final Object key;
    private SoftEntry(Object key, Object value, ReferenceQueue garbageCollectionQueue) {
      super(value, garbageCollectionQueue);
      this.key = key;
    }
  }

}