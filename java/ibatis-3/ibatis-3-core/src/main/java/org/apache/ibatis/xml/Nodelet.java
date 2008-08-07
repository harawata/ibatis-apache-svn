package org.apache.ibatis.xml;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Nodelet {
  String value();
}
