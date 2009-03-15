package org.apache.ibatis.annotations;

import org.apache.ibatis.cache.decorators.LruCache;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.mapping.ResultSetType;
import org.apache.ibatis.mapping.StatementType;

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
    Class javaType() default void.class;
    JdbcType jdbcType() default JdbcType.UNDEFINED;
    Class typeHandler() default void.class;
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public static @interface Results {
    Result[] value() default {};
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public static @interface Result {
    boolean id() default false;
    String column() default "";
    String property() default "";
    Class javaType() default void.class;
    JdbcType jdbcType() default JdbcType.UNDEFINED;
    Class typeHandler() default void.class;
    One one() default @One;
    Many many() default @Many(javaType = void.class);
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public static @interface One {
    String select() default "";
    ConstructorArgs constructor() default @ConstructorArgs;
    Results results() default @Results;
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public static @interface Many {
    Class javaType();
    String select() default "";
    ConstructorArgs constructor() default @ConstructorArgs;
    Results results() default @Results;
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  public static @interface CacheDomain {
    Class<? extends org.apache.ibatis.cache.Cache> implementation() default PerpetualCache.class;
    Class<? extends org.apache.ibatis.cache.Cache> eviction() default LruCache.class;
    long flushInterval() default 3600000;
    int size() default 1000;
    boolean readWrite() default true;
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  public @interface CacheDomainRef {
    Class value();
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface Insert {
    String[] value() default {};
    SqlProvider sqlProvider() default @SqlProvider(type = void.class, method = "");
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface Update {
    String[] value() default {};
    SqlProvider sqlProvider() default @SqlProvider(type = void.class, method = "");
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface Delete {
    String[] value() default {};
    SqlProvider sqlProvider() default @SqlProvider(type = void.class, method = "");
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface Select {
    String[] value() default {};
    SqlProvider sqlProvider() default @SqlProvider(type = void.class, method = "");
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface SqlProvider {
    Class type();
    String method();
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface Options {
    boolean useCache() default true;
    boolean flushCache() default false;
    ResultSetType resultSetType() default ResultSetType.FORWARD_ONLY;
    StatementType statementType() default StatementType.PREPARED;
    int fetchSize() default -1;
    int timeout() default -1;
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface TypeDiscriminator {
    String column();
    Class javaType() default void.class;
    JdbcType jdbcType() default JdbcType.UNDEFINED;
    Class typeHandler() default void.class;
    Case[] cases();
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface Case {
    String value();
    Class type();
    Result[] results() default {};
  }

}
