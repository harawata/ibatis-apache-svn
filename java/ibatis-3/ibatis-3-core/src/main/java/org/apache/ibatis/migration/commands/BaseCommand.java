package org.apache.ibatis.migration.commands;

import org.apache.ibatis.migration.Change;
import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.ScriptRunner;
import org.apache.ibatis.adhoc.AdHocExecutor;
import org.apache.ibatis.io.Resources;

import java.util.*;
import java.sql.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.io.*;
import java.net.*;

public abstract class BaseCommand implements Command {

  protected static final PrintStream out = System.out;

  protected File basePath;
  protected File envPath;
  protected File scriptPath;
  protected File driverPath;
  protected String environment;
  protected boolean force;
  private ClassLoader driverClassLoader;

  protected BaseCommand(File repository, String environment, boolean force) {
    this.basePath = repository;
    this.envPath = subdirectory(repository, "environments");
    this.scriptPath = subdirectory(repository, "scripts");
    this.driverPath = subdirectory(repository, "drivers");
    this.environment = environment;
    this.force = force;
  }

  protected void reverse(Comparable[] comparable) {
    Arrays.sort(comparable, new Comparator() {
      public int compare(Object o1, Object o2) {
        return ((Comparable) o2).compareTo(o1);
      }
    });
  }
  
  protected boolean paramsEmpty(String... params) {
    return params == null || params.length < 1 || params[0] == null || params[0].length() < 1;
  }

  protected Change parseChangeFromFilename(String filename) {
    try {
      Change change = new Change();
      String[] parts = filename.split("\\.")[0].split("_");
      change.setId(new BigDecimal(parts[0]));
      StringBuilder builder = new StringBuilder();
      for (int i = 1; i < parts.length; i++) {
        if (i > 1) builder.append(" ");
        builder.append(parts[i]);
      }
      change.setDescription(builder.toString());
      return change;
    } catch (Exception e) {
      throw new MigrationException("Error parsing change from file.  Cause: " + e, e);
    }
  }

  protected List<Change> getChangelog() {
    AdHocExecutor executor = getAdHocExecutor();
    try {
      List<Map<String, Object>> changelog = executor.selectAll("select ID, DESCRIPTION from CHANGELOG order by id");
      List<Change> changes = new ArrayList<Change>();
      for (Map<String, Object> change : changelog) {
        changes.add(new Change(new BigDecimal(change.get("ID").toString()), change.get("DESCRIPTION").toString()));
      }
      return changes;
    } catch (SQLException e) {
      throw new MigrationException("Error querying last applied migration.  Cause: " + e, e);
    } finally {
      executor.closeConnection();
    }
  }

  protected Change getLastAppliedChange() {
    List<Change> changelog = getChangelog();
    return changelog.get(changelog.size() - 1);
  }

  protected boolean changelogExists() {
    AdHocExecutor executor = getAdHocExecutor();
    try {
      executor.selectAll("select ID, DESCRIPTION from CHANGELOG");
      return true;
    } catch (SQLException e) {
      return false;
    } finally {
      executor.closeConnection();
    }
  }

  protected String horizontalLine(String caption, int length) {
    StringBuilder builder = new StringBuilder();
    builder.append("==========");
    if (caption.length() > 0) {
      caption = " " + caption + " ";
      builder.append(caption);
    }
    for (int i = 0; i < length - caption.length(); i++) {
      builder.append("=");
    }
    return builder.toString();
  }

  protected String getTimestampAsString() {
    try {
      // Ensure that two subsequent calls are less likely to return the same value.
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      //ignore
    }
    return new SimpleDateFormat("yyyyMMddHHmmss").format(new java.sql.Date(System.currentTimeMillis()));
  }

  protected void copyResourceTo(String resource, File toFile) {
    copyResourceTo(resource, toFile, null);
  }

  protected void copyResourceTo(String resource, File toFile, Map<String, String> variables) {
    out.println("Creating: " + toFile.getName());
    try {
      LineNumberReader reader = new LineNumberReader(Resources.getResourceAsReader(this.getClass().getClassLoader(), resource));
      try {
        PrintWriter writer = new PrintWriter(new FileWriter(toFile));
        try {
          String line;
          while ((line = reader.readLine()) != null) {
            line = parsePlaceholders(line, variables);
            writer.println(line);
          }
        } finally {
          writer.close();
        }
      } finally {
        reader.close();
      }
    } catch (IOException e) {
      throw new MigrationException("Error copying " + resource + " to " + toFile.getAbsolutePath() + ".  Cause: " + e, e);
    }
  }

  protected AdHocExecutor getAdHocExecutor() {
    lazyInitializeDrivers();

    Properties props = getEnvironmentProperties();
    String driver = props.getProperty("driver");
    String url = props.getProperty("url");
    String username = props.getProperty("username");
    String password = props.getProperty("password");
    return new AdHocExecutor(driver, url, username, password, false, driverClassLoader);
  }

  protected ScriptRunner getScriptRunner() {
    try {
      lazyInitializeDrivers();

      Properties props = getEnvironmentProperties();
      String driver = props.getProperty("driver");
      String url = props.getProperty("url");
      String username = props.getProperty("username");
      String password = props.getProperty("password");
      PrintWriter outWriter = new PrintWriter(out);
      ScriptRunner scriptRunner = new ScriptRunner(driver, url, username, password, false, !force);
      scriptRunner.setDriverClassLoader(driverClassLoader);
      scriptRunner.setLogWriter(outWriter);
      scriptRunner.setErrorLogWriter(outWriter);
      return scriptRunner;
    } catch (Exception e) {
      throw new MigrationException("Error creating ScriptRunner.  Cause: " + e, e);
    }
  }

  protected File baseFile(String fileName) {
    return new File(basePath.getAbsolutePath() + File.separator + fileName);
  }

  protected File environmentFile(String fileName) {
    return new File(envPath.getAbsolutePath() + File.separator + fileName);
  }

  protected File scriptFile(String fileName) {
    return new File(scriptPath.getAbsolutePath() + File.separator + fileName);
  }

  protected File driverFile(String fileName) {
    return new File(driverPath.getAbsolutePath() + File.separator + fileName);
  }

  protected File environmentFile() {
    return environmentFile(environment + ".properties");
  }

  protected File existingEnvironmentFile() {
    File envFile = environmentFile();
    if (!envFile.exists()) {
      throw new MigrationException("Environment file missing: " + envFile.getAbsolutePath());
    }
    return envFile;
  }

  private void lazyInitializeDrivers() {
    try {
      if (driverClassLoader == null && driverPath.exists()) {
        List<URL> urlList = new ArrayList<URL>();
        for (File file : driverPath.listFiles()) {
          URL url = new URL("jar:file:/" + file.getAbsolutePath() + "!/");
          urlList.add(url);
        }
        URL[] urls = urlList.toArray(new URL[urlList.size()]);
        driverClassLoader = new URLClassLoader(urls);
      }
    } catch (IOException e) {
      throw new MigrationException("Error loading JDBC drivers. Cause: " + e, e);
    }
  }

  private File subdirectory(File base, String sub) {
    return new File(base.getAbsoluteFile() + File.separator + sub);
  }

  private Properties getEnvironmentProperties() {
    try {
      File file = existingEnvironmentFile();
      Properties props = new Properties();
      props.load(new FileInputStream(file));
      return props;
    } catch (IOException e) {
      throw new MigrationException("Error loading environment properties.  Cause: " + e, e);
    }
  }

  private String parsePlaceholders(String string, Map<String, String> variables) {
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
        String propValue = variables.get(propName);
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
