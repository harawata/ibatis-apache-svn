package org.apache.ibatis.migration.commands;

import org.apache.ibatis.migration.MigrationException;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class NewCommand extends BaseCommand {

  public NewCommand(File repository, String environment, boolean force) {
    super(repository, environment, force);
  }

  public void execute (String... params) {
    if (paramsEmpty(params)) {
      throw new MigrationException("No description specified for new migration.");
    }
    String description = params[0];
    Map<String, String> variables = new HashMap<String, String>();
    variables.put("description", description);
    existingEnvironmentFile();
    String filename = getTimestampAsString() + "_" + description.replace(' ', '_') + ".sql";
    copyResourceTo("org/apache/ibatis/migration/template_migration.sql", repositoryFile(filename), variables);
    out.println("Done!");
  }

  protected boolean paramsEmpty(String... params) {
    return params == null || params.length < 1 || params[0] == null || params[0].length() < 1;
  }
  

}
