package org.apache.ibatis.migration;

import org.apache.ibatis.io.Resources;

import java.math.BigInteger;
import java.io.*;
import java.text.SimpleDateFormat;
import java.sql.Date;
import java.util.*;

public class Migrator {

  private File repository;
  private String environment;
  private boolean force;

  public Migrator(String repository, String environment, boolean force) {
    this.repository = new File(repository);
    this.environment = environment;
    this.force = force;
  }

  public static void main(String[] args) throws Exception {
    new CommandLine(args).execute();
  }

  public void initialize() {
    createIfNecessary(repository);
    ensureEmpty(repository);
    System.out.println("Initializing: " + repository);
    copyResourceTo("org/apache/ibatis/migration/template_environment.properties", environmentFile(environment));
    copyResourceTo("org/apache/ibatis/migration/template_changelog.sql", repositoryFile(getTimestamp() + "_create_changelog.sql"));
    copyResourceTo("org/apache/ibatis/migration/template_migration.sql", repositoryFile(getTimestamp() + "_first_migration.sql"));
    System.out.println("Done!");
  }

  public void newMigration(String description) {
    if (description == null) {
      throw new MigrationException("No description specified for new migration.");
    }
    Map<String,String> variables = new HashMap<String,String>();
    variables.put("description",description);
    ensureEnvironment(environment);
    String filename = getTimestamp() + "_" + description.replace(' ', '_') + ".sql";
    copyResourceTo("org/apache/ibatis/migration/template_migration.sql", repositoryFile(filename), variables);
    System.out.println("Done!");
  }

  public void runPendingMigrations() {
    try {
      String[] filenames = repository.list();
      Arrays.sort(filenames);
      for(String filename : filenames) {
        if (filename.endsWith(".sql")) {
          System.out.println(horizontalLine("Applying: " + filename,80));
          ScriptRunner runner = getScriptRunner();
          runner.runScript(new MigrationReader(new FileReader(repositoryFile(filename)),false));
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("<Description>.  Cause: " + e, e);
    }
  }

  public void migrateToVersion(BigInteger version) {
    System.out.println("Migrating to: " + version);
  }

  public void undoLastMigration() {
    try {
      String[] filenames = repository.list();
      Arrays.sort(filenames,new Comparator(){
        public int compare(Object o1, Object o2) {
          return ((Comparable)o2).compareTo(o1);
        }
      });
      for(String filename : filenames) {
        if (filename.endsWith(".sql")) {
          System.out.println(horizontalLine("Undoing: " + filename,80));
          ScriptRunner runner = getScriptRunner();
          runner.runScript(new MigrationReader(new FileReader(repositoryFile(filename)),true));
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("<Description>.  Cause: " + e, e);
    }
  }

  private String horizontalLine(String caption, int length) {
    StringBuilder builder = new StringBuilder();
    builder.append("========== ");
    builder.append(caption);
    builder.append(" ");
    for (int i=0; i < length - caption.length() - 2; i++) {
      builder.append("=");
    }
    return builder.toString();
  }

  private String getTimestamp() {
    try {
      // Ensure that two subsequent calls are less likely to return the same value.
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      //ignore
    }
    return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(System.currentTimeMillis()));
  }

  private File repositoryFile(String fileName) {
    return new File(repository.getAbsolutePath()+File.separator+fileName);
  }

  private void copyResourceTo(String resource, File toFile) {
    copyResourceTo(resource,toFile,null);
  }

  private void copyResourceTo(String resource, File toFile, Map<String,String> variables) {
    System.out.println("Creating: " + toFile.getName());
    try {
      LineNumberReader reader = new LineNumberReader(Resources.getResourceAsReader(this.getClass().getClassLoader(), resource));
      try {
        PrintWriter writer = new PrintWriter(new FileWriter(toFile));
        try {
          String line;
          while ((line = reader.readLine()) != null) {
            line = parsePlaceholders(line,variables);
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

  private void ensureEmpty(File path) {
    String[] list = path.list();
    if (list.length != 0) {
      for (String entry : list) {
        if (!entry.startsWith(".")) {
          throw new MigrationException("Directory must be empty (.svn etc allowed): " + path.getAbsolutePath());
        }
      }
    }
  }

  private void ensureEnvironment(String environment) {
    File envFile = environmentFile(environment);
    if (!envFile.exists()) {
      throw new MigrationException("Environment file missing: " + envFile.getAbsolutePath());
    }
  }

  private File environmentFile(String environment) {
    return repositoryFile(environment+".properties");
  }

  private void createIfNecessary(File path) {
    if (!path.exists()) {
      File parent = new File(path.getParent());
      createIfNecessary(parent);
      if (!path.mkdir()) {
        throw new MigrationException("Could not create directory path for an unknown reason. Make sure you have access to the directory.");
      }
    }
  }

  private String parsePlaceholders(String string, Map<String,String> variables) {
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

  private ScriptRunner getScriptRunner() {
    try {
      Properties props = new Properties();
      String filename = repository + File.separator + environment + ".properties";
      File file = new File(filename);
      if (!file.exists()) {
        throw new MigrationException("Could not find environment properties file: " + filename);
      }
      props.load(new FileInputStream(file));
      String driver = props.getProperty("driver");
      String url = props.getProperty("url");
      String username = props.getProperty("username");
      String password = props.getProperty("password");
      return new ScriptRunner(driver,url,username,password,false,!force);
    } catch (Exception e) {
      throw new MigrationException("Error creating ScriptRunner.  Cause: " + e, e);
    }
  }

}
