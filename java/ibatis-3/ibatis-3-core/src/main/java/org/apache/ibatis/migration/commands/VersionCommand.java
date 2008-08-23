package org.apache.ibatis.migration.commands;

import org.apache.ibatis.migration.*;

import java.io.File;
import java.math.BigDecimal;

public class VersionCommand extends BaseCommand {

  public VersionCommand(File repository, String environment, boolean force) {
    super(repository, environment, force);
  }

  public void execute(String... params) {
    ensureParamsPassed(params);
    ensureNumericParam(params);
    ensureVersionExists(params);

    BigDecimal version = new BigDecimal(params[0]);

    Change change = getLastAppliedChange();
    if (version.compareTo(change.getId()) > 0) {
      out.println("Upgrading to: " + version);  
      Command run = new RunCommand(basePath,environment,force,true);
      while (!version.equals(change.getId())) {
        run.execute();
        change = getLastAppliedChange();
      }
    } else if (version.compareTo(change.getId()) < 0) {
      out.println("Downgrading to: " + version);
      Command undo = new UndoCommand(basePath,environment,force);
      while (!version.equals(change.getId())) {
        undo.execute();
        change = getLastAppliedChange();
      }
    } else {
      out.println("Already at version: " + version);  
    }
  }

  private void ensureParamsPassed(String... params) {
    if (paramsEmpty(params)) {
      throw new MigrationException("No target version specified for migration.");
    }
  }

  private void ensureNumericParam(String... params) {
    BigDecimal id;
    try {
      id = new BigDecimal(params[0]);
    } catch (Exception e) {
      throw new MigrationException("The version number must be a numeric integer.  " + e, e);
    }
  }

  private void ensureVersionExists(String... params) {
    String[] filenames = scriptPath.list();
    reverse(filenames);
    boolean found = false;
    String prefix = params[0] + "_";
    for (String filename : filenames) {
      if (filename.startsWith(prefix)) {
        found = true;
        break;
      }
    }
    if (!found) {
      throw new MigrationException("A migration for the specified version number does not exist.");
    }
  }

}
