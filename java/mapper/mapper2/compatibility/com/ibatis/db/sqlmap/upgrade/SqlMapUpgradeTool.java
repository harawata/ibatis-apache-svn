package com.ibatis.db.sqlmap.upgrade;

import com.ibatis.common.exception.NestedRuntimeException;

import java.io.*;

/**
 * User: Clinton Begin
 * Date: Dec 1, 2003
 * Time: 11:16:36 PM
 */
public class SqlMapUpgradeTool {

  private static final SqlMapXmlConverter CONVERTER = new SqlMapXmlConverter();

  private SqlMapUpgradeTool() {
  }

  public static void main(String[] args) {
    try {
      if (args.length < 2 || args.length > 3) {
        System.out.println("Usage:\n\njava " + SqlMapUpgradeTool.class.getName() + " [InputXMLFile] [OutputXMLFile]\n\n");
        return;
      } else if (args.length == 2) {
        CONVERTER.convertFile(args[0], args[1]);
      } else if (args.length == 3) {
        // Backward compatibility before autodetect
        CONVERTER.convertFile(args[1], args[2]);
      }
    } catch (IOException e) {
      throw new NestedRuntimeException("Error running SQL Map Upgrade Tool.  Cause: " + e, e);
    }
  }


}
