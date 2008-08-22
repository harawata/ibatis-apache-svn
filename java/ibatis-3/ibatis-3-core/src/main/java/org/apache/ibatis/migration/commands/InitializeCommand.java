package org.apache.ibatis.migration.commands;

import org.apache.ibatis.migration.MigrationException;

import java.io.File;

public class InitializeCommand extends BaseCommand {

  public InitializeCommand(File repository, String environment, boolean force) {
    super(repository, environment, force);
  }

  public void execute(String... args) {
    createDirectoryIfNecessary(repository);
    ensureDirectoryIsEmpty(repository);
    out.println("Initializing: " + repository);
    copyResourceTo("org/apache/ibatis/migration/template_environment.properties", environmentFile());
    copyResourceTo("org/apache/ibatis/migration/template_changelog.sql", repositoryFile(getTimestampAsString() + "_create_changelog.sql"));
    copyResourceTo("org/apache/ibatis/migration/template_migration.sql", repositoryFile(getTimestampAsString() + "_first_migration.sql"));
    out.println("Done!");
  }

  protected void ensureDirectoryIsEmpty(File path) {
    String[] list = path.list();
    if (list.length != 0) {
      for (String entry : list) {
        if (!entry.startsWith(".")) {
          throw new MigrationException("Directory must be empty (.svn etc allowed): " + path.getAbsolutePath());
        }
      }
    }
  }

  protected void createDirectoryIfNecessary(File path) {
    if (!path.exists()) {
      File parent = new File(path.getParent());
      createDirectoryIfNecessary(parent);
      if (!path.mkdir()) {
        throw new MigrationException("Could not create directory path for an unknown reason. Make sure you have access to the directory.");
      }
    }
  }

  

}
