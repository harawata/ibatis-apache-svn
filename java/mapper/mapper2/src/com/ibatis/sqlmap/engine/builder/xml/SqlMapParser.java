package com.ibatis.sqlmap.engine.builder.xml;

import com.ibatis.common.resources.Resources;
import com.ibatis.common.xml.*;
import com.ibatis.sqlmap.client.extensions.TypeHandlerCallback;
import com.ibatis.sqlmap.engine.cache.CacheModel;
import com.ibatis.sqlmap.engine.mapping.parameter.*;
import com.ibatis.sqlmap.engine.mapping.result.*;
import com.ibatis.sqlmap.engine.mapping.statement.*;
import com.ibatis.sqlmap.engine.type.*;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;

public class SqlMapParser extends BaseParser {

  private final NodeletParser parser = new NodeletParser();

  public SqlMapParser(Variables vars) {
    super(vars);
    parser.setValidation(true);
    parser.setEntityResolver(new SqlMapClasspathEntityResolver());

    addSqlMapNodelets();
    addTypeAliasNodelets();
    addCacheModelNodelets();
    addParameterMapNodelets();
    addResultMapNodelets();
    addStatementNodelets();

  }

  public void parse(Reader reader) throws IOException, ParserConfigurationException, SAXException {
    parser.parse(reader);
  }

  private void addSqlMapNodelets() {
    parser.addNodelet("/sqlMap", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties attributes = NodeletUtils.parseAttributes(node, vars.properties);
        vars.currentNamespace = attributes.getProperty("namespace");
      }
    });
  }

  private void addTypeAliasNodelets() {
    parser.addNodelet("/sqlMap/typeAlias", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties attributes = NodeletUtils.parseAttributes(node, vars.properties);
        String alias = attributes.getProperty("alias");
        String type = attributes.getProperty("type");
        vars.typeHandlerFactory.putTypeAlias(alias, type);
      }
    });
  }

  private void addCacheModelNodelets() {
    parser.addNodelet("/sqlMap/cacheModel", new Nodelet() {
      public void process(Node node) throws Exception {
        vars.currentProperties = new Properties();
        Properties attributes = NodeletUtils.parseAttributes(node, vars.properties);

        String type = attributes.getProperty("type");
        String id = attributes.getProperty("id");

        id = applyNamespace(id);
        type = vars.typeHandlerFactory.resolveAlias(type);

        CacheModel model = new CacheModel();
        model.setId(id);
        model.setControllerClassName(type);
        model.setReadOnly(NodeletUtils.getBooleanAttribute(attributes, "readOnly", true));
        model.setSerialize(NodeletUtils.getBooleanAttribute(attributes, "serialize", false));
        model.setResource(vars.currentResource);
        vars.delegate.addCacheModel(model);
        vars.currentCacheModel = model;
      }
    });
    parser.addNodelet("/sqlMap/cacheModel/property", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties attributes = NodeletUtils.parseAttributes(node, vars.properties);
        String name = attributes.getProperty("name");
        String value = NodeletUtils.parsePropertyTokens(attributes.getProperty("value"),vars.properties);
        vars.currentProperties.put(name, value);
      }
    });
    parser.addNodelet("/sqlMap/cacheModel/flushOnExecute", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties attributes = NodeletUtils.parseAttributes(node, vars.properties);
        vars.currentCacheModel.addFlushTriggerStatement(attributes.getProperty("statement"));
      }
    });
    parser.addNodelet("/sqlMap/cacheModel/flushInterval", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties attributes = NodeletUtils.parseAttributes(node, vars.properties);
        long t = 0;
        String milliseconds = attributes.getProperty("milliseconds");
        String seconds = attributes.getProperty("seconds");
        String minutes = attributes.getProperty("minutes");
        String hours = attributes.getProperty("hours");
        if (milliseconds != null) t += Integer.parseInt(milliseconds);
        if (seconds != null) t += Integer.parseInt(seconds) * 1000;
        if (minutes != null) t += Integer.parseInt(minutes) * 60 * 1000;
        if (hours != null) t += Integer.parseInt(hours) * 60 * 60 * 1000;
        if (t < 1) throw new RuntimeException("A flush interval must specify one or more of milliseconds, seconds, minutes or hours.");
        vars.currentCacheModel.setFlushInterval(t);
      }
    });
    parser.addNodelet("/sqlMap/cacheModel/end()", new Nodelet() {
      public void process(Node node) throws Exception {
        vars.currentCacheModel.configure(vars.currentProperties);
        vars.currentProperties = null;
        vars.currentCacheModel = null;
      }
    });
  }

  private void addParameterMapNodelets() {
    parser.addNodelet("/sqlMap/parameterMap", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties attributes = NodeletUtils.parseAttributes(node, vars.properties);

        BasicParameterMap parameterMap;
        parameterMap = new BasicParameterMap(vars.delegate);

        String id = applyNamespace(attributes.getProperty("id"));
        String parameterClassName = attributes.getProperty("class");
        parameterClassName = vars.typeHandlerFactory.resolveAlias(parameterClassName);

        parameterMap.setId(id);
        parameterMap.setResource(vars.currentResource);

        Class parameterClass = null;
        try {
          parameterClass = Resources.classForName(parameterClassName);
        } catch (Exception e) {
          //throw new SqlMapException("Error configuring ParameterMap.  Could not set ParameterClass.  Cause: " + e, e);
        }

        parameterMap.setParameterClass(parameterClass);
        vars.currentParameterMap = parameterMap;
        vars.parameterMappingList = new ArrayList();
      }
    });
    parser.addNodelet("/sqlMap/parameterMap/parameter", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties attributes = NodeletUtils.parseAttributes(node, vars.properties);

        String propertyName = attributes.getProperty("property");
        String jdbcType = attributes.getProperty("jdbcType");
        String javaType = attributes.getProperty("javaType");
        String nullValue = attributes.getProperty("nullValue");
        String mode = attributes.getProperty("mode");
        String callback = attributes.getProperty("typeHandler");

        callback = vars.typeHandlerFactory.resolveAlias(callback);
        javaType = vars.typeHandlerFactory.resolveAlias(javaType);

        TypeHandler handler = null;
        if (callback != null) {
          try {
            TypeHandlerCallback typeHandlerCallback = (TypeHandlerCallback) Resources.classForName(callback).newInstance();
            handler = new CustomTypeHandler(typeHandlerCallback);
          } catch (Exception e) {
            throw new RuntimeException("Error occurred during custom type handler configuration.  Cause: " + e, e);
          }
        } else {
          Class parameterClass = vars.currentParameterMap.getParameterClass();
          handler = resolveTypeHandler(vars.delegate.getTypeHandlerFactory(), parameterClass, propertyName, javaType, jdbcType);
        }

        BasicParameterMapping mapping = new BasicParameterMapping();
        mapping.setPropertyName(propertyName);
        mapping.setJdbcTypeName(jdbcType);
        mapping.setNullValue(nullValue);
        if (mode != null && mode.length() > 0) {
          mapping.setMode(mode);
        }
        mapping.setTypeHandler(handler);
        try {
          if (javaType != null && javaType.length() > 0) {
            mapping.setJavaType(Class.forName(javaType));
          }
        } catch (ClassNotFoundException e) {
          throw new RuntimeException("Error setting javaType on parameter mapping.  Cause: " + e);
        }
        vars.parameterMappingList.add(mapping);

      }
    });
    parser.addNodelet("/sqlMap/parameterMap/end()", new Nodelet() {
      public void process(Node node) throws Exception {
        vars.currentParameterMap.setParameterMappingList(vars.parameterMappingList);
        vars.delegate.addParameterMap(vars.currentParameterMap);
      }
    });
  }

  private void addResultMapNodelets() {
    parser.addNodelet("/sqlMap/resultMap", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties attributes = NodeletUtils.parseAttributes(node, vars.properties);

        BasicResultMap resultMap;
        resultMap = new BasicResultMap(vars.delegate);

        String id = applyNamespace(attributes.getProperty("id"));
        String resultClassName = attributes.getProperty("class");
        String extended = applyNamespace(attributes.getProperty("extends"));
        String xmlName = attributes.getProperty("xmlName");
        String groupBy = attributes.getProperty("groupBy");
        resultClassName = vars.typeHandlerFactory.resolveAlias(resultClassName);


        resultMap.setId(id);
        resultMap.setXmlName(xmlName);
        resultMap.setResource(vars.currentResource);

        if (groupBy != null && groupBy.length() > 0) {
          StringTokenizer parser = new StringTokenizer(groupBy, ", ", false);
          while (parser.hasMoreTokens()) {
            resultMap.addGroupByProperty(parser.nextToken());
          }
        }

        Class resultClass = null;
        try {
          resultClass = Resources.classForName(resultClassName);
        } catch (Exception e) {
          throw new RuntimeException("Error configuring Result.  Could not set ResultClass.  Cause: " + e, e);
        }

        resultMap.setResultClass(resultClass);

        vars.resultMappingList = new ArrayList();
        if (extended != null) {
          BasicResultMap extendedResultMap = (BasicResultMap) vars.delegate.getResultMap(extended);
          ResultMapping[] resultMappings = extendedResultMap.getResultMappings();
          for (int i = 0; i < resultMappings.length; i++) {
            vars.resultMappingList.add(resultMappings[i]);
          }
        }

        vars.resultMappingIndex = vars.resultMappingList.size();
        resultMap.setResultMappingList(vars.resultMappingList);

        vars.delegate.addResultMap(resultMap);
        vars.currentResultMap = resultMap;
      }
    });
    parser.addNodelet("/sqlMap/resultMap/result", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties attributes = NodeletUtils.parseAttributes(node, vars.properties);
        String propertyName = attributes.getProperty("property");
        String nullValue = attributes.getProperty("nullValue");
        String jdbcType = attributes.getProperty("jdbcType");
        String javaType = attributes.getProperty("javaType");
        String columnName = attributes.getProperty("column");
        String columnIndex = attributes.getProperty("columnIndex");
        String statementName = attributes.getProperty("select");
        String resultMapName = attributes.getProperty("resultMap");
        String callback = attributes.getProperty("typeHandler");

        callback = vars.typeHandlerFactory.resolveAlias(callback);
        javaType = vars.typeHandlerFactory.resolveAlias(javaType);

        TypeHandler handler = null;
        if (callback != null) {
          try {
            TypeHandlerCallback typeHandlerCallback = (TypeHandlerCallback) Resources.classForName(callback).newInstance();
            handler = new CustomTypeHandler(typeHandlerCallback);
          } catch (Exception e) {
            throw new RuntimeException("Error occurred during custom type handler configuration.  Cause: " + e, e);
          }
        } else {
          Class resultClass = vars.currentResultMap.getResultClass();
          handler = resolveTypeHandler(vars.delegate.getTypeHandlerFactory(), resultClass, propertyName, javaType, jdbcType, true);
        }

        BasicResultMapping mapping = new BasicResultMapping();
        mapping.setPropertyName(propertyName);
        mapping.setColumnName(columnName);
        mapping.setJdbcTypeName(jdbcType);
        mapping.setTypeHandler(handler);
        mapping.setNullValue(nullValue);
        mapping.setStatementName(statementName);
        mapping.setNestedResultMapName(resultMapName);

        if (resultMapName != null && resultMapName.length() > 0) {
          vars.currentResultMap.addNestedResultMappings(mapping);
        }

        try {
          if (javaType != null && javaType.length() > 0) {
            mapping.setJavaType(Class.forName(javaType));
          }
        } catch (ClassNotFoundException e) {
          throw new RuntimeException("Error setting javaType on result mapping.  Cause: " + e);
        }

        if (columnIndex != null && columnIndex.length() > 0) {
          mapping.setColumnIndex(Integer.parseInt(columnIndex));
        } else {
          vars.resultMappingIndex++;
          mapping.setColumnIndex(vars.resultMappingIndex);
        }

        vars.resultMappingList.add(mapping);
      }
    });
  }

  private void addStatementNodelets() {
    parser.addNodelet("/sqlMap/statement", new Nodelet() {
      public void process(Node node) throws Exception {
        vars.currentStatement = new SqlStatementParser(vars).parseGeneralStatement(node, new GeneralStatement());
        vars.delegate.addMappedStatement(vars.currentStatement);
      }
    });
    parser.addNodelet("/sqlMap/insert", new Nodelet() {
      public void process(Node node) throws Exception {
        vars.currentStatement = new SqlStatementParser(vars).parseGeneralStatement(node, new InsertStatement());
        vars.delegate.addMappedStatement(vars.currentStatement);
      }
    });
    parser.addNodelet("/sqlMap/update", new Nodelet() {
      public void process(Node node) throws Exception {
        vars.currentStatement = new SqlStatementParser(vars).parseGeneralStatement(node, new UpdateStatement());
        vars.delegate.addMappedStatement(vars.currentStatement);
      }
    });
    parser.addNodelet("/sqlMap/delete", new Nodelet() {
      public void process(Node node) throws Exception {
        vars.currentStatement = new SqlStatementParser(vars).parseGeneralStatement(node, new DeleteStatement());
        vars.delegate.addMappedStatement(vars.currentStatement);
      }
    });
    parser.addNodelet("/sqlMap/select", new Nodelet() {
      public void process(Node node) throws Exception {
        vars.currentStatement = new SqlStatementParser(vars).parseGeneralStatement(node, new SelectStatement());
        vars.delegate.addMappedStatement(vars.currentStatement);
      }
    });
    parser.addNodelet("/sqlMap/procedure", new Nodelet() {
      public void process(Node node) throws Exception {
        vars.currentStatement = new SqlStatementParser(vars).parseGeneralStatement(node, new ProcedureStatement());
        vars.delegate.addMappedStatement(vars.currentStatement);
      }
    });
  }


}
