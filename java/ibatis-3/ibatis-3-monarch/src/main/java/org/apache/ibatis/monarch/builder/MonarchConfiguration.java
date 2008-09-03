package org.apache.ibatis.monarch.builder;

import org.apache.ibatis.mapping.Configuration;
import org.apache.ibatis.monarch.environment.Environment;

import java.util.*;

public class MonarchConfiguration extends Configuration {

  private String defaultEnvironment;
  private Map<String,Environment> environmentMap = new HashMap<String,Environment>();

  public String getDefaultEnvironment() {
    return defaultEnvironment;
  }

  public void setDefaultEnvironment(String defaultEnvironment) {
    this.defaultEnvironment = defaultEnvironment;
  }

  public void addEnvironment(Environment environment) {
    if(environmentMap.containsKey(environment.getId())) {
      throw new BuilderException("Duplicate environment ID detected: " + environment.getId() + ".  Environment ID must be unique.");
    }
    environmentMap.put(environment.getId(),environment);
  }
}

