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

/**
 * Tool to run database scripts
 */
public class ScriptRunner {

  //private static final Log log = LogFactory.getLog(ScriptRunner.class);


  private String driver;
  private String url;
  private String username;
  private String password;
  private boolean stopOnError;
  private boolean autoCommit;
  private PrintWriter logWriter = new PrintWriter(System.out);
  private PrintWriter errorLogWriter = new PrintWriter(System.err);

  /**
   * Default constructor
   */
  public ScriptRunner() {
    stopOnError = false;
    autoCommit = false;
  }

  /**
   * Constructor to allow passing in a Map with configuration data
   * @param props - the configuration properties
   */
  public ScriptRunner(Map props) {
    setDriver((String) props.get("driver"));
    setUrl((String) props.get("url"));
    setUsername((String) props.get("username"));
    setPassword((String) props.get("password"));
    setStopOnError("true".equals(props.get("stopOnError")));
    setAutoCommit("true".equals(props.get("autoCommit")));
  }

  /**
   * Don't use this! ;-)
   * 
   * @param driver - the Driver class to use
   * @param url - the database URL
   * @param username - the username to use to connect to the database
   * @param password - the password to use to connect to the database
   * 
   * @deprecated
   */
  public ScriptRunner(String driver, String url, String username, String password) {
    this.driver = driver;
    this.url = url;
    this.username = username;
    this.password = password;
    this.autoCommit = false;
  }

  /**
   * Getter for stopOnError property
   * 
   * @return The value of the stopOnError property
   */
  public boolean isStopOnError() {
    return stopOnError;
  }

  /**
   * Setter for stopOnError property
   * 
   * @param stopOnError - the new value of the stopOnError property
   */
  public void setStopOnError(boolean stopOnError) {
    this.stopOnError = stopOnError;
  }

  /**
   * Getter for autoCommit property
   * 
   * @return The value of the autoCommit property
   */
  public boolean isAutoCommit() {
    return autoCommit;
  }

  /**
   * Setter for autoCommit property
   * 
   * @param autoCommit - the new value of the autoCommit property
   */
  public void setAutoCommit(boolean autoCommit) {
    this.autoCommit = autoCommit;
  }

  /**
   * Getter for logWriter property
   * 
   * @return The value of the logWriter property
   */
  public PrintWriter getLogWriter() {
    return logWriter;
  }

  /**
   * Setter for logWriter property
   * 
   * @param logWriter - the new value of the logWriter property
   */
  public void setLogWriter(PrintWriter logWriter) {
    this.logWriter = logWriter;
  }

  /**
   * Getter for errorLogWriter property
   * 
   * @return The value of the errorLogWriter property
   */
  public PrintWriter getErrorLogWriter() {
    return errorLogWriter;
  }

  /**
   * Setter for errorLogWriter property
   * 
   * @param errorLogWriter - the new value of the errorLogWriter property
   */
  public void setErrorLogWriter(PrintWriter errorLogWriter) {
    this.errorLogWriter = errorLogWriter;
  }

  /**
   * Getter for driver property
   * 
   * @return The value of the driver property
   */
  public String getDriver() {
    return driver;
  }

  /**
   * Setter for driver property
   * 
   * @param driver - the new value of the driver property
   */
  public void setDriver(String driver) {
    this.driver = driver;
  }

  /**
   * Getter for url property
   * 
   * @return The value of the url property
   */
  public String getUrl() {
    return url;
  }

  /**
   * Setter for url property
   * 
   * @param url - the new value of the url property
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * Getter for username property
   * 
   * @return The value of the username property
   */
  public String getUsername() {
    return username;
  }

  /**
   * Setter for username property
   * 
   * @param username - the new value of the username property
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * Getter for password property
   * 
   * @return The value of the password property
   */
  public String getPassword() {
    return password;
  }

  /**
   * Setter for password property
   * 
   * @param password - the new value of the password property
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * Runs an SQL script (read in using the Reader parameter)
   * 
   * @param reader - the source of the script
   * 
   * @throws ClassNotFoundException if the driver class cannot be found
   * @throws SQLException if any SQL errors occur
   * @throws IOException if there is an error reading from the Reader
   * @throws IllegalAccessException if there are problems creating the driver class
   * @throws InstantiationException if there are problems creating the driver class
   */
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

  /**
   * Runs an SQL script (read in using the Reader parameter) using the connection passed in
   * 
   * @param conn - the connection to use for the script
   * @param reader - the source of the script
   *  
   * @throws SQLException if any SQL errors occur
   * @throws IOException if there is an error reading from the Reader
   */
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
