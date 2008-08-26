package org.apache.ibatis.migration;

import java.util.Properties;

public class PropertyParser {

  public static String parse(String string, Properties variables) {
    final String OPEN = "${";
    final String CLOSE = "}";
    String newString = string;
    if (newString != null && variables != null) {
      int start = newString.indexOf(OPEN);
      int end = newString.indexOf(CLOSE);

      while (start > -1 && end > start) {
        String prepend = newString.substring(0, start);
        String append = newString.substring(end + CLOSE.length());
        String propName = newString.substring(start + OPEN.length(), end);
        String propValue = variables.getProperty(propName);
        if (propValue == null) {
          newString = prepend + append;
        } else {
          newString = prepend + propValue + append;
        }
        start = newString.indexOf(OPEN);
        end = newString.indexOf(CLOSE);
      }
    }
    return newString;
  }


}
