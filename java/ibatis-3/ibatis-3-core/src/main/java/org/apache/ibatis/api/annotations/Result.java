package org.apache.ibatis.api.annotations;

import org.apache.ibatis.type.JdbcType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Result {

  String column() default "";
  String property() default "";
  String field() default "";
  JdbcType jdbcType() default JdbcType.UNDEFINED;
  Class typeHandler() default Undefined.class;
  QueryMethod nestedQuery() default @QueryMethod(type= Undefined.class, methodName="", parameters="");

}
