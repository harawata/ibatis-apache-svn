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

public class DownCommand extends BaseCommand {

  public DownCommand(File repository, String environment, boolean force) {
    super(repository, environment, force);
  }

  public void execute(String... params) {
    try {     
      Change lastChange = getLastAppliedChange();
      List<Change> migrations = getMigrations();
      Collections.reverse(migrations);
      for (Change change : migrations) {
        if (change.getId().equals(lastChange.getId())) {
          out.println(horizontalLine("Undoing: " + change.getFilename(), 80));
          ScriptRunner runner = getScriptRunner();
          runner.runScript(new MigrationReader(new FileReader(scriptFile(change.getFilename())), true, getEnvironmentProperties()));
          if (changelogExists()) {
            deleteChange(change);
          } else {
            out.println("Changelog doesn't exist. No further migrations will be undone (normal for the last migration).");
          }
          break;
        }
      }
    } catch (Exception e) {
      throw new MigrationException("Error undoing last migration.  Cause: " + e, e);
    }
  }

  protected void deleteChange(Change change) {
    AdHocExecutor executor = getAdHocExecutor();
    try {
      executor.delete("delete from CHANGELOG where id = ?", change.getId());
    } catch (SQLException e) {
      throw new MigrationException("Error querying last applied migration.  Cause: " + e, e);
    } finally {
      executor.closeConnection();
    }
  }

  

  protected void reverse(Comparable[] comparable) {
    Arrays.sort(comparable, new Comparator() {
      public int compare(Object o1, Object o2) {
        return ((Comparable) o2).compareTo(o1);
      }
    });
  }

}
