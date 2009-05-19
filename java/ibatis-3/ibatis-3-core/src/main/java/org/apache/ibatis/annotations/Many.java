package org.apache.ibatis.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

@Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface Many {
  public abstract Class javaType();

  public abstract String select() default "";

  public abstract ConstructorArgs constructor() default @ConstructorArgs;

//  public abstract Results results() default @Results;
}
