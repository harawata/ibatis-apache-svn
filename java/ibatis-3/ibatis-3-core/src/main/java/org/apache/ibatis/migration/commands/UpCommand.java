package org.apache.ibatis.migration.commands;

import org.apache.ibatis.migration.Change;
import org.apache.ibatis.migration.MigrationReader;
import org.apache.ibatis.migration.ScriptRunner;
import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.adhoc.AdHocExecutor;

import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

public class UpCommand extends BaseCommand {

  private boolean runOneStepOnly = false;

  public UpCommand(File repository, String environment, boolean force) {
    super(repository,environment,force);
  }

  public UpCommand(File repository, String environment, boolean force, boolean runOneStepOnly) {
    super(repository, environment, force);
    this.runOneStepOnly = runOneStepOnly;
  }

  public void execute(String... params) {
    try {
      Change lastChange = null; 
      if (changelogExists()) {
        lastChange = getLastAppliedChange();
      }
      List<Change> migrations = getMigrations();
      for (Change change : migrations) {
        if (lastChange == null || change.getId().compareTo(lastChange.getId()) > 0) {
          out.println(horizontalLine("Applying: " + change.getFilename(), 80));
          ScriptRunner runner = getScriptRunner();
          runner.runScript(new MigrationReader(new FileReader(scriptFile(change.getFilename())), false, getEnvironmentProperties()));
          insertChangelog(change);
          if (runOneStepOnly) {
            break;
          }
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("<Description>.  Cause: " + e, e);
    }
  }

  protected void insertChangelog(Change change) {
    AdHocExecutor executor = getAdHocExecutor();
    change.setAppliedTimestamp(getAppliedTimestampAsString());
    try {
      executor.insert("insert into CHANGELOG (ID, APPLIED_AT, DESCRIPTION) values (?,?,?)", change.getId(), change.getAppliedTimestamp(), change.getDescription());
    } catch (SQLException e) {
      throw new MigrationException("Error querying last applied migration.  Cause: " + e, e);
    } finally {
      executor.closeConnection();
    }
  }

  protected String getAppliedTimestampAsString() {
    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.sql.Date(System.currentTimeMillis()));
  }

}
