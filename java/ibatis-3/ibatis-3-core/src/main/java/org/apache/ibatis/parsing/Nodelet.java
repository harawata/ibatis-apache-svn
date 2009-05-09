package org.apache.ibatis.parsing;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Nodelet {
  String value();
}
