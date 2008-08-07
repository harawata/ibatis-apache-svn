package org.apache.ibatis.cache;

import org.apache.ibatis.cache.decorators.*;
import org.apache.ibatis.reflection.MetaObject;

public interface Cache {

  String getId();

  int getSize();

  void putObject(Object key, Object object);

  Object getObject(Object key);

  boolean hasKey(Object key);

  Object removeObject(Object key);

  void clear();

  public class Builder {
    private String id;
    private Class implementation;
    private Integer size;
    private Long clearInterval;
    private boolean readWrite;

    public Builder(String id, Class implementation) {
      this.id = id;
      this.implementation = implementation;
    }

    public Builder size(Integer size) {
      this.size = size;
      return this;
    }

    public Builder clearInterval(Long clearInterval) {
      this.clearInterval = clearInterval;
      return this;
    }

    public Builder readWrite(boolean readWrite) {
      this.readWrite = readWrite;
      return this;
    }

    public Cache build() {
      try {
        // implementation...
        Cache cache = (Cache) implementation.newInstance();
        MetaObject metaCache = MetaObject.forObject(cache);
        metaCache.setValue("id", id);
        if (size != null && metaCache.hasSetter("size")) {
          metaCache.setValue("size", size);
        }
        // decorators...
        if (clearInterval != null) {
          cache = new ScheduledCache(cache);
          ((ScheduledCache) cache).setClearInterval(clearInterval);
        }
        if (readWrite) {
          cache = new SerializedCache(cache);
        }
        cache = new LoggingCache(cache);
        cache = new SynchronizedCache(cache);
        return cache;
      } catch (Exception e) {
        throw new RuntimeException("Error building Cache class.  Cause: " + e, e);
      }
    }
  }

}