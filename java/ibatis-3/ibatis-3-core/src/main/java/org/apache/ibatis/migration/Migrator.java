package org.apache.ibatis.migration;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.adhoc.AdHocExecutor;

import java.math.BigInteger;
import java.math.BigDecimal;
import java.io.*;
import java.text.SimpleDateFormat;
import java.sql.Date;
import java.sql.SQLException;
import java.util.*;

public class Migrator {

  private File repository;
  private String environment;
  private boolean force;
  private PrintStream out = System.out;

  public Migrator(String repository, String environment, boolean force) {
    this.repository = new File(repository);
    this.environment = environment;
    this.force = force;
  }

  public static void main(String[] args) throws Exception {
    new CommandLine(args).execute();
  }

  public void initialize() {
    createDirectoryIfNecessary(repository);
    ensureDirectoryIsEmpty(repository);
    out.println("Initializing: " + repository);
    copyResourceTo("org/apache/ibatis/migration/template_environment.properties", environmentFile());
    copyResourceTo("org/apache/ibatis/migration/template_changelog.sql", repositoryFile(getTimestampAsString() + "_create_changelog.sql"));
    copyResourceTo("org/apache/ibatis/migration/template_migration.sql", repositoryFile(getTimestampAsString() + "_first_migration.sql"));
    out.println("Done!");
  }

  public void newMigration(String description) {
    if (description == null) {
      throw new MigrationException("No description specified for new migration.");
    }
    Map<String, String> variables = new HashMap<String, String>();
    variables.put("description", description);
    existingEnvironmentFile();
    String filename = getTimestampAsString() + "_" + description.replace(' ', '_') + ".sql";
    copyResourceTo("org/apache/ibatis/migration/template_migration.sql", repositoryFile(filename), variables);
    out.println("Done!");
  }

  public void runPendingMigrations() {
    try {
      String[] filenames = repository.list();
      Arrays.sort(filenames);
      for (String filename : filenames) {
        if (filename.endsWith(".sql")) {
          out.println(horizontalLine("Applying: " + filename, 80));
          ScriptRunner runner = getScriptRunner();
          runner.runScript(new MigrationReader(new FileReader(repositoryFile(filename)), false));
          Change change = parseChangeFromFilename(filename);
          insertChangelog(change);
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("<Description>.  Cause: " + e, e);
    }
  }

  public void migrateToVersion(BigInteger version) {
    out.println("not implemented");
  }

  public void undoLastMigration() {
    try {
      String[] filenames = repository.list();
      reverse(filenames);
      Change lastChange = getLastChange();
      for (String filename : filenames) {
        if (filename.endsWith(".sql")) {
          Change change = parseChangeFromFilename(filename);
          if (change.getId().equals(lastChange.getId())) {
            out.println(horizontalLine("Undoing: " + filename, 80));
            ScriptRunner runner = getScriptRunner();
            runner.runScript(new MigrationReader(new FileReader(repositoryFile(filename)), true));
            if (changelogExists()) {
              deleteChange(change);
            } else {
              out.println("Changelog doesn't exist. No further migrations will be undone (normal for the last migration).");
            }
            break;            
          }
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("<Description>.  Cause: " + e, e);
    }
  }

  public void printStatus() {
    if (changelogExists()) {
      List<Change> changelog = getChangelog();
      out.println("ID             TIMESTAMP             DESCRIPTION");
      out.println(horizontalLine("", 60));
      for (Change change : changelog) {
        out.println(change);
      }
    } else {
      out.println("Changelog does not exist.");
    }
  }

  private void reverse(Comparable[] comparable) {
    Arrays.sort(comparable, new Comparator() {
      public int compare(Object o1, Object o2) {
        return ((Comparable) o2).compareTo(o1);
      }
    });
  }

  private void insertChangelog(Change change) {
    AdHocExecutor executor = getAdHocExecutor();
    try {
      executor.insert("insert into CHANGELOG (ID, DESCRIPTION) values (?,?)", change.getId(), change.getDescription());
    } catch (SQLException e) {
      throw new MigrationException("Error querying last applied migration.  Cause: " + e, e);
    } finally {
      executor.closeConnection();
    }
  }

  private void deleteChange(Change change) {
    AdHocExecutor executor = getAdHocExecutor();
    try {
      executor.delete("delete from CHANGELOG where id = ?", change.getId());
    } catch (SQLException e) {
      throw new MigrationException("Error querying last applied migration.  Cause: " + e, e);
    } finally {
      executor.closeConnection();
    }
  }

  private Change parseChangeFromFilename(String filename) {
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

  private List<Change> getChangelog() {
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

  private Change getLastChange() {
    List<Change> changelog = getChangelog();
    return changelog.get(changelog.size() - 1);
  }

  private boolean changelogExists() {
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

  private String horizontalLine(String caption, int length) {
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

  private String getTimestampAsString() {
    try {
      // Ensure that two subsequent calls are less likely to return the same value.
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      //ignore
    }
    return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(System.currentTimeMillis()));
  }

  private File repositoryFile(String fileName) {
    return new File(repository.getAbsolutePath() + File.separator + fileName);
  }

  private void copyResourceTo(String resource, File toFile) {
    copyResourceTo(resource, toFile, null);
  }

  private void copyResourceTo(String resource, File toFile, Map<String, String> variables) {
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

  private void ensureDirectoryIsEmpty(File path) {
    String[] list = path.list();
    if (list.length != 0) {
      for (String entry : list) {
        if (!entry.startsWith(".")) {
          throw new MigrationException("Directory must be empty (.svn etc allowed): " + path.getAbsolutePath());
        }
      }
    }
  }

  private void createDirectoryIfNecessary(File path) {
    if (!path.exists()) {
      File parent = new File(path.getParent());
      createDirectoryIfNecessary(parent);
      if (!path.mkdir()) {
        throw new MigrationException("Could not create directory path for an unknown reason. Make sure you have access to the directory.");
      }
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

  private AdHocExecutor getAdHocExecutor() {
    Properties props = getEnvironmentProperties();
    String driver = props.getProperty("driver");
    String url = props.getProperty("url");
    String username = props.getProperty("username");
    String password = props.getProperty("password");
    AdHocExecutor executor = new AdHocExecutor(driver, url, username, password, false);
    return executor;
  }

  private ScriptRunner getScriptRunner() {
    try {
      Properties props = getEnvironmentProperties();
      String driver = props.getProperty("driver");
      String url = props.getProperty("url");
      String username = props.getProperty("username");
      String password = props.getProperty("password");
      return new ScriptRunner(driver, url, username, password, false, !force);
    } catch (Exception e) {
      throw new MigrationException("Error creating ScriptRunner.  Cause: " + e, e);
    }
  }

  private File environmentFile() {
    return repositoryFile(environment + ".properties");
  }

  private File existingEnvironmentFile() {
    File envFile = environmentFile();
    if (!envFile.exists()) {
      throw new MigrationException("Environment file missing: " + envFile.getAbsolutePath());
    }
    return envFile;
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

}
