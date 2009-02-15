package org.apache.ibatis.binding;

import org.apache.ibatis.api.SqlSession;
import org.apache.ibatis.mapping.Configuration;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.List;

public class MapperMethod {

  private SqlSession sqlSession;
  private Configuration config;

  private MapperMethodType type;
  private String commandName;

  private Method method;
  private int argCount;

  private boolean returnsList;
  private boolean hasListBounds;

  public MapperMethod(Method method, SqlSession sqlSession) {

    this.sqlSession = sqlSession;
    this.method = method;
    this.config = sqlSession.getConfiguration();

    setupFields();
    determineSelectMethod();
    determineCommandType();
    validateStatement();
  }

  public Object execute(Object[] args) throws SQLException {
    Object result;
    if (MapperMethodType.INSERT == type) {
      Object param = getParam(args);
      result = sqlSession.insert(commandName, param);
    } else if (MapperMethodType.UPDATE == type) {
      Object param = getParam(args);
      result = sqlSession.update(commandName, param);
    } else if (MapperMethodType.DELETE == type) {
      Object param = getParam(args);
      result = sqlSession.delete(commandName, param);
    } else if (MapperMethodType.SELECT == type) {
      if (returnsList) {
        result = executeForList(args);
      } else {
        Object param = getParam(args);
        result = sqlSession.selectOne(commandName, param);
      }
    } else {
      throw new BindingException("Unkown execution method for: " + commandName);
    }

    return result;
  }

  private void setupFields() {
    this.commandName = method.getDeclaringClass().getName() + "." + method.getName();
    this.argCount = method.getParameterTypes().length;
  }

  private void determineSelectMethod() {
    if (List.class.isAssignableFrom(method.getReturnType())) {
      returnsList = true;
      if (argCount == 2) {
      } else if (argCount == 3) {
        hasListBounds = true;
      }
    }
  }

  private void determineCommandType() {
    String methodName = method.getName();
    if (methodName.startsWith("insert") || methodName.startsWith("create")) {
      type = MapperMethodType.INSERT;
    } else if (methodName.startsWith("update") || methodName.startsWith("save")) {
      type = MapperMethodType.UPDATE;
    } else if (methodName.startsWith("delete") || methodName.startsWith("remove")) {
      type = MapperMethodType.DELETE;
    } else if (methodName.startsWith("select") || methodName.startsWith("find")) {
      type = MapperMethodType.SELECT;
    } else {
      throw new BindingException("Unkown execution method for: " + commandName);      
    }
  }

  private void validateStatement() {
    try {
      config.getMappedStatement(commandName);
    } catch (Exception e) {
      throw new BindingException("Invalid bound statement (not found): " + commandName);
    }
  }

  private Object executeForList(Object[] args) throws SQLException {
    Object result;
    if (hasListBounds) {
      Object param = getParam(args);
      int skip = ((Integer) args[1]);
      int max = ((Integer) args[2]);
      result = sqlSession.selectList(commandName, param, skip, max);
    } else {
      Object param = getParam(args);
      result = sqlSession.selectList(commandName, param);
    }
    return result;
  }

  private Object getParam(Object[] args) {
    if (args == null) {
      return null;
    }
    return args.length > 0 ? args[0] : null;
  }

}
