/**
 * Created by IntelliJ IDEA.
 * User: Clinton Begin
 * Date: Nov 17, 2002
 * Time: 4:44:09 PM
 * To change this template use Options | File Templates.
 */
package com.ibatis.common.jdbc;

//import org.apache.commons.logging.*;

import com.ibatis.common.resources.*;
import com.ibatis.common.exception.NestedRuntimeException;

import java.sql.*;
import java.io.*;
import java.util.*;

public class ScriptRunner {

  //private static final Log log = LogFactory.getLog(ScriptRunner.class);


  public String driver;
  public String url;
  public String username;
  public String password;
  public boolean stopOnError;
  public boolean autoCommit;
  public PrintWriter logWriter = new PrintWriter(System.out);
  public PrintWriter errorLogWriter = new PrintWriter(System.err);

  public ScriptRunner() {
    stopOnError = false;
    autoCommit = false;
  }

  public ScriptRunner(Map props) {
    setDriver((String) props.get("driver"));
    setUrl((String) props.get("url"));
    setUsername((String) props.get("username"));
    setPassword((String) props.get("password"));
    setStopOnError("true".equals(props.get("stopOnError")));
    setAutoCommit("true".equals(props.get("autoCommit")));
  }

  /**
   * @deprecated
   */
  public ScriptRunner(String driver, String url, String username, String password) {
    this.driver = driver;
    this.url = url;
    this.username = username;
    this.password = password;
    this.autoCommit = false;
  }

  public boolean isStopOnError() {
    return stopOnError;
  }

  public void setStopOnError(boolean stopOnError) {
    this.stopOnError = stopOnError;
  }

  public boolean isAutoCommit() {
    return autoCommit;
  }

  public void setAutoCommit(boolean autoCommit) {
    this.autoCommit = autoCommit;
  }

  public PrintWriter getLogWriter() {
    return logWriter;
  }

  public void setLogWriter(PrintWriter logWriter) {
    this.logWriter = logWriter;
  }

  public PrintWriter getErrorLogWriter() {
    return errorLogWriter;
  }

  public void setErrorLogWriter(PrintWriter errorLogWriter) {
    this.errorLogWriter = errorLogWriter;
  }

  public String getDriver() {
    return driver;
  }

  public void setDriver(String driver) {
    this.driver = driver;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void runScript(Reader reader)
      throws ClassNotFoundException, SQLException, IOException,
             IllegalAccessException, InstantiationException {
    DriverManager.registerDriver((Driver) Resources.classForName(driver).newInstance());
    Connection conn = DriverManager.getConnection(url, username, password);
    if (conn.getAutoCommit() != autoCommit) {
      conn.setAutoCommit(autoCommit);
    }
    runScript(conn, reader);
    conn.close();
  }

  public void runScript(Connection conn, Reader reader)
      throws IOException, SQLException {
    StringBuffer command = null;
    try {
      LineNumberReader lineReader = new LineNumberReader(reader);
      String line = null;
      while ((line = lineReader.readLine()) != null) {
        if (command == null) {
          command = new StringBuffer();
        }
        String trimmedLine = line.trim();
        if (trimmedLine.startsWith("--")) {
          println(trimmedLine);
//          if (log.isDebugEnabled()) {
//            log.debug(trimmedLine);
//          }
        } else if (trimmedLine.length() < 1 || trimmedLine.startsWith("//")) {
          //Do nothing
        } else if (trimmedLine.endsWith(";")) {
          command.append(line.substring(0, line.lastIndexOf(";")));
          command.append(" ");
          Statement statement = conn.createStatement();

          println(command);
//          if (log.isDebugEnabled()) {
//            log.debug(command);
//          }

          boolean hasResults = false;
          if (stopOnError) {
            hasResults = statement.execute(command.toString());
          } else {
            try {
              statement.execute(command.toString());
            } catch (SQLException e) {
              e.fillInStackTrace();
              printlnError("Error executing: " + command);
              printlnError(e);
            }
          }

          if (autoCommit && !conn.getAutoCommit()) {
            conn.commit();
          }

          ResultSet rs = statement.getResultSet();
          if (hasResults && rs != null) {
            ResultSetMetaData md = rs.getMetaData();
            int cols = md.getColumnCount();
            for (int i = 0; i < cols; i++) {
              String name = md.getColumnName(i);
              print(name + "\t");
            }
            println("");
            while (rs.next()) {
              for (int i = 0; i < cols; i++) {
                String value = rs.getString(i);
                print(value + "\t");
              }
              println("");
            }
          }

          command = null;
          try {
            statement.close();
          } catch (Exception e) {
            // Ignore to workaround a bug in Jakarta DBCP
          }
          Thread.yield();
        } else {
          command.append(line);
          command.append(" ");
        }
      }
      if (!autoCommit) {
        conn.commit();
      }
    } catch (SQLException e) {
      e.fillInStackTrace();
      printlnError("Error executing: " + command);
      printlnError(e);
//      log.error("Error executing: " + command, e);
      throw e;
    } catch (IOException e) {
      e.fillInStackTrace();
      printlnError("Error executing: " + command);
      printlnError(e);
//      log.error("Error executing: " + command, e);
      throw e;
    } finally {
      conn.rollback();
      flush();
    }
  }


  /**
   * Deprecated
   * @param args
   * @deprecated Main method will not be supported in future versions
   */
  public static void main(String[] args) {
    try {
      if (args.length < 1) {
        System.out.println("Usage: " + ScriptRunner.class.getName() + " <scriptrunner.properties>");
      } else {
        File file = new File(args[0]);
        if (!file.exists()) {
          System.out.println(args[0] + " not found.");
          System.out.println("Usage: " + ScriptRunner.class.getName() + " <scriptrunner.properties>");
        } else {
          FileInputStream in = new FileInputStream(file);
          Properties props = new Properties();
          props.load(in);
          in.close();
          ScriptRunner runner = new ScriptRunner(props);

          Map map = new TreeMap();

          Enumeration propEnum = props.propertyNames();
          while (propEnum.hasMoreElements()) {
            String name = (String) propEnum.nextElement();
            if (name.startsWith("file-")) {
              runner.println("");
              runner.printlnError("");
              String filename = props.getProperty(name);
              map.put(Integer.valueOf(name.substring(5, name.length())), filename);
            }
          }

          Iterator i = map.keySet().iterator();
          while (i.hasNext()) {
            String filename = (String) map.get(i.next());
            runner.println("Running Script: " + filename);
            runner.printlnError("Running Script: " + filename);
            runner.runScript(new FileReader(filename));
            runner.printlnError("");
          }
        }
      }
    } catch (Exception e) {
      throw new NestedRuntimeException("Error running ScriptRunner.  Cause: " + e, e);
    }
  }

  private void print(Object o) {
    if (logWriter != null) {
      System.out.print(o);
    }
  }

  private void println(Object o) {
    if (logWriter != null) {
      logWriter.println(o);
    }
  }

  private void printlnError(Object o) {
    if (errorLogWriter != null) {
      errorLogWriter.println(o);
    }
  }

  private void flush() {
    if (logWriter != null) {
      logWriter.flush();
    }
    if (errorLogWriter != null) {
      errorLogWriter.flush();
    }
  }


}
