package org.apache.ibatis.migration.commands;

import org.apache.ibatis.migration.Change;

import java.util.List;
import java.io.File;

public class StatusCommand extends BaseCommand {

  public StatusCommand(File repository, String environment, boolean force) {
    super(repository, environment, force);
  }

  public void execute(String... params) {
    if (changelogExists()) {
      List<Change> changelog = getChangelog();
      out.println("ID             Applied At          Description");
      out.println(horizontalLine("", 60));
      for (Change change : changelog) {
        out.println(change);
      }
    } else {
      out.println("Changelog does not exist.");
    }
  }

}
