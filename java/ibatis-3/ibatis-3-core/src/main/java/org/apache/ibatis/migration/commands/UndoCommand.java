package org.apache.ibatis.migration.commands;

import org.apache.ibatis.migration.Change;
import org.apache.ibatis.migration.MigrationReader;
import org.apache.ibatis.migration.ScriptRunner;
import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.adhoc.AdHocExecutor;

import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Comparator;
import java.sql.SQLException;

public class UndoCommand extends BaseCommand {

  public UndoCommand(File repository, String environment, boolean force) {
    super(repository, environment, force);
  }

  public void execute(String... params) {
    try {
      String[] filenames = scriptPath.list();
      reverse(filenames);
      Change lastChange = getLastChange();
      for (String filename : filenames) {
        if (filename.endsWith(".sql")) {
          Change change = parseChangeFromFilename(filename);
          if (change.getId().equals(lastChange.getId())) {
            out.println(horizontalLine("Undoing: " + filename, 80));
            ScriptRunner runner = getScriptRunner();
            runner.runScript(new MigrationReader(new FileReader(scriptFile(filename)), true));
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
