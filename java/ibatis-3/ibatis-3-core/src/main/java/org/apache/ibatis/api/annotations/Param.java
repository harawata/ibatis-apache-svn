package org.apache.ibatis.api.annotations;

import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.type.JdbcType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Param {

  String name();
  ParameterMode mode() default ParameterMode.IN;
  JdbcType jdbcType() default JdbcType.UNDEFINED;
  Class typeHandler() default Undefined.class;
  
}
