package org.apache.ibatis.migration.commands;

import org.apache.ibatis.migration.Change;
import org.apache.ibatis.migration.ScriptRunner;
import org.apache.ibatis.migration.MigrationReader;

import java.math.BigInteger;
import java.io.FileReader;
import java.io.File;
import java.util.List;

public class VersionCommand extends BaseCommand {

  public VersionCommand(File repository, String environment, boolean force) {
    super(repository, environment, force);
  }

  public void execute(String... params) {
    out.println("not implemented");
  }

}
