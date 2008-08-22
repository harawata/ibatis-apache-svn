package org.apache.ibatis.migration;

import java.util.Arrays;
import java.util.Set;
import java.util.Collections;
import java.util.HashSet;
import java.io.File;
import java.io.PrintStream;
import java.math.BigInteger;

public class CommandLine {

  private static final String PATH_PREFIX = "--path=";
  private static final String ENV_PREFIX = "--env=";
  private static final String FORCE = "--force";
  private static final String INIT = "init";
  private static final String NEW = "new";
  private static final String RUN = "run";
  private static final String VERSION = "version";
  private static final String UNDO = "undo";
  private static final Set<String> KNOWN_COMMANDS = Collections.unmodifiableSet(
      new HashSet<String>(Arrays.asList(INIT, NEW, RUN, VERSION, UNDO)));

  private String repository;
  private String environment;
  private String command;
  private String params;
  private String parseError;
  private boolean help;
  private boolean force;

  public CommandLine(String[] args) {
    parse(args);
    validate();
  }

  public String getRepository() {
    return repository;
  }

  public String getEnvironment() {
    return environment;
  }

  public String getCommand() {
    return command;
  }

  public String getParams() {
    return params;
  }

  public boolean isHelp() {
    return help;
  }

  public boolean isValid() {
    return parseError == null;
  }

  public String toString() {
    return repository + " " + environment + " " + command + " " + (params == null ? "" : params);
  }

  public void execute() {
    if (isHelp()) {
      printUsage();
    } else if (!isValid()) {
      printError();
      printUsage();
    } else {
      try {
        runCommand();
      } catch (MigrationException e) {
        System.err.println(e.getMessage());
        printUsage();
      }
    }
  }

  private void runCommand() {
    Migrator migrator = new Migrator(repository, environment, force);
    if (INIT.equals(command)) {
      migrator.initialize();
    } else if (NEW.equals(command)) {
      migrator.newMigration(params);
    } else if (RUN.equals(command)) {
      migrator.runPendingMigrations();
    } else if (VERSION.equals(command)) {
      BigInteger version = null;
      try {
        version = new BigInteger(params);
      } catch(Exception e) {
        System.err.println("Invalid version number specified: " + params);
        printUsage();
      }
      migrator.migrateToVersion(version);
    } else if (UNDO.equals(command)) {
      migrator.undoLastMigration();
    } else {
      throw new RuntimeException("Attempt to execute unkown command.");
    }
  }

  public void printError() {
    PrintStream out = System.err;
    out.println(parseError);
    out.flush();
  }

  public void printUsage() {
    PrintStream out = System.out;
    out.println();
    out.println("Usage: migrate command [parameter] [--path=<directory>] [--env=<environment>]");
    out.println();
    out.println("--path=<directory>   Path to repository.  Default current working directory.");
    out.println("--env=<environment>  Environment to configure. Default environment is 'development'.");
    out.println("--env=<environment>  Environment to configure. Default environment is 'development'.");
    out.println("--force              Forces script to continue even if SQL errors are encountered.");
    out.println("--help               Displays this usage message.");
    out.println();
    out.println("Commands:");
    out.println("  init               Creates (if necessary) and initializes a migration path.");
    out.println("  new <description>  Creates a new migration with the provided description.");
    out.println("  run                Run all unapplied migrations.");
    out.println("  version <version>  Migrates the database up or down to the specified version.");
    out.println("  undo               Undoes the last migration applied to the database.");
    out.println();
    out.flush();
  }

  private void parse(String[] args) {
    for (String arg : args) {
      if (arg.startsWith(PATH_PREFIX) && arg.length() > PATH_PREFIX.length()) {
        repository = arg.split("=")[1];
      } else if (arg.startsWith(ENV_PREFIX) && arg.length() > ENV_PREFIX.length()) {
        environment = arg.split("=")[1];
      } else if (arg.startsWith("--force")) {
        force = true;
      } else if (arg.startsWith("--help")) {
        help = true;
      } else if (command == null) {
        command = arg;
      } else if (params == null){
        params = arg;
      } else {
        params += " ";
        params += arg;
      }
    }
  }

  private void validate() {
    if (repository == null) {
      repository = "./";
    }
    if (environment == null) {
      environment = "development";
    }
    File f = new File(repository);
    if (f.exists() && !f.isDirectory()) {
      parseError = ("Migrations path must be a directory: " + f.getAbsolutePath());
    } else {
      repository = f.getAbsolutePath();
      if (command == null) {
        parseError = "No command specified.";
      } else {
        if (!KNOWN_COMMANDS.contains(command)) {
          parseError = "Unknown command: " + command;
        }
      }
    }
  }

}