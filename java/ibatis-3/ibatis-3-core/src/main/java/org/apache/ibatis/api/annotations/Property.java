package org.apache.ibatis.api.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
public @interface Property {
  String name();
  String value();
}
