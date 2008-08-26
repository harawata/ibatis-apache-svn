package org.apache.ibatis.migration.commands;

import org.apache.ibatis.migration.*;

import java.io.*;
import java.util.Arrays;

public class BootstrapCommand extends BaseCommand {

  public BootstrapCommand(File repository, String environment, boolean force) {
    super(repository, environment, force);
  }

  public void execute(String... params) {
    try {
      if (changelogExists()) {
        out.println("For your safety, the bootstrap SQL script will only run before migrations are applied (i.e. before the changelog exists).");
      } else {
        File bootstrap = scriptFile("bootstrap.sql");
        if (bootstrap.exists()) {
          out.println(horizontalLine("Applying: bootstrap.sql", 80));
          ScriptRunner runner = getScriptRunner();
          runner.runScript(new MigrationReader(new FileReader(bootstrap), false, getEnvironmentProperties()));
        } else {
          out.println("Error, could not run bootstrap.sql.  The file does not exist.");
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("<Description>.  Cause: " + e, e);
    }
  }

}
