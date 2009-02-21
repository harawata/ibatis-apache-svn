package org.apache.ibatis.annotations;

import org.apache.ibatis.cache.decorators.LruCache;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.apache.ibatis.type.JdbcType;

import java.lang.annotation.*;

public class Annotations {

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public static @interface ConstructorArgs {
    Arg[] value() default {};
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public static @interface Arg {
    boolean id() default false;
    String column() default "";
    Class javaType() default Class.class;
    JdbcType jdbcType() default JdbcType.UNDEFINED;
    Class typeHandler() default Class.class;
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public static @interface Results {
    Result[] value() default {};
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public static @interface Result {
    boolean key() default false;
    String column() default "";
    String property() default "";
    Class javaType() default Class.class;
    JdbcType jdbcType() default JdbcType.UNDEFINED;
    Class typeHandler() default Class.class;
    String collectionSelect() default "";
    Results collectionResults() default @Results;
    String associationSelect() default "";
    Results associationResults() default @Results;
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  public static @interface Cache {
    Class<? extends org.apache.ibatis.cache.Cache> type() default PerpetualCache.class;
    Class<? extends org.apache.ibatis.cache.Cache> eviction() default LruCache.class;
    int flushInterval() default 3600000;
    int size() default 1000;
    boolean readOnly() default false;
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  public @interface CacheRefNamespace {
    String[] value();
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface Insert {
    String[] value();
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface Update {
    String[] value();
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface Delete {
    String[] value();
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface Select {
    String[] value();
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface Procedure {
    String[] value();
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface Statement {
    String[] value();
  }

}
