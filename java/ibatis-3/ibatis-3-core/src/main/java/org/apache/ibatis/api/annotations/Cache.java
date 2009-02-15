package org.apache.ibatis.api.annotations;

import org.apache.ibatis.cache.impl.PerpetualCache;
import org.apache.ibatis.cache.decorators.LruCache;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Cache {
  Class<? extends org.apache.ibatis.cache.Cache> type() default PerpetualCache.class;
  Class<? extends org.apache.ibatis.cache.Cache> eviction() default LruCache.class;
  int flushInterval() default 3600000;
  int size() default 1000;
  boolean readOnly() default false;
}
