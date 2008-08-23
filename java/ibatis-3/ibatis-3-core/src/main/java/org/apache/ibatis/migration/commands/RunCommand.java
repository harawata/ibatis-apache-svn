package org.apache.ibatis.migration.commands;

import org.apache.ibatis.migration.Change;
import org.apache.ibatis.migration.MigrationReader;
import org.apache.ibatis.migration.ScriptRunner;
import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.adhoc.AdHocExecutor;

import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.sql.SQLException;

public class RunCommand extends BaseCommand {

  private boolean runOneStepOnly = false;

  public RunCommand(File repository, String environment, boolean force) {
    super(repository,environment,force);
  }

  public RunCommand(File repository, String environment, boolean force, boolean runOneStepOnly) {
    super(repository, environment, force);
    this.runOneStepOnly = runOneStepOnly;
  }

  public void execute(String... params) {
    try {
      String[] filenames = scriptPath.list();
      Arrays.sort(filenames);
      Change lastChange = null; 
      if (changelogExists()) {
        lastChange = getLastAppliedChange();
      }
      for (String filename : filenames) {
        if (filename.endsWith(".sql")) {
          Change change = parseChangeFromFilename(filename);
          if (lastChange == null || change.getId().compareTo(lastChange.getId()) > 0) {
            out.println(horizontalLine("Applying: " + filename, 80));
            ScriptRunner runner = getScriptRunner();
            runner.runScript(new MigrationReader(new FileReader(scriptFile(filename)), false));
            insertChangelog(change);
            if (runOneStepOnly) {
              break;
            }
          }
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("<Description>.  Cause: " + e, e);
    }
  }

  protected void insertChangelog(Change change) {
    AdHocExecutor executor = getAdHocExecutor();
    try {
      executor.insert("insert into CHANGELOG (ID, DESCRIPTION) values (?,?)", change.getId(), change.getDescription());
    } catch (SQLException e) {
      throw new MigrationException("Error querying last applied migration.  Cause: " + e, e);
    } finally {
      executor.closeConnection();
    }
  }  

}
