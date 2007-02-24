package com.ibatis.sqlmap.engine.builder.xml;

import com.ibatis.common.resources.Resources;
import com.ibatis.common.xml.Nodelet;
import com.ibatis.common.xml.NodeletException;
import com.ibatis.common.xml.NodeletParser;
import com.ibatis.common.xml.NodeletUtils;
import com.ibatis.sqlmap.client.SqlMapException;
import com.ibatis.sqlmap.client.extensions.TypeHandlerCallback;
import com.ibatis.sqlmap.engine.cache.CacheModel;
import com.ibatis.sqlmap.engine.mapping.parameter.BasicParameterMap;
import com.ibatis.sqlmap.engine.mapping.parameter.BasicParameterMapping;
import com.ibatis.sqlmap.engine.mapping.result.*;
import com.ibatis.sqlmap.engine.mapping.statement.*;
import com.ibatis.sqlmap.engine.type.CustomTypeHandler;
import com.ibatis.sqlmap.engine.type.TypeHandler;
import org.w3c.dom.Node;

import java.io.InputStream;
import java.io.Reader;
import java.util.*;

public class SqlMapParser {

  private final NodeletParser parser = new NodeletParser();
  private ParserState state;

  public SqlMapParser(ParserState state) {
    this.state = state;
    parser.setValidation(true);
    parser.setEntityResolver(new SqlMapClasspathEntityResolver());

    addSqlMapNodelets();
    addSqlNodelets();
    addTypeAliasNodelets();
    addCacheModelNodelets();
    addParameterMapNodelets();
    addResultMapNodelets();
    addStatementNodelets();

  }

  public void parse(Reader reader) throws NodeletException {
    parser.parse(reader);
  }

  public void parse(InputStream inputStream) throws NodeletException {
    parser.parse(inputStream);
  }

  private void addSqlMapNodelets() {
    parser.addNodelet("/sqlMap", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties attributes = NodeletUtils.parseAttributes(node, state.globalProps);
        state.namespace = attributes.getProperty("namespace");
      }
    });
    parser.addNodelet("/sqlMap/end()", new Nodelet() {
      public void process(Node node) throws Exception {
        Iterator names = state.delegate.getResultMapNames();
        while (names.hasNext()) {
          String name = (String)names.next();
          ResultMap rm = state.delegate.getResultMap(name);
          Discriminator disc = rm.getDiscriminator();
          if (disc != null) {
            disc.bindSubMaps();
          }
        }
      }
    });
  }

  private void addSqlNodelets() {
    parser.addNodelet("/sqlMap/sql", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties attributes = NodeletUtils.parseAttributes(node, state.globalProps);
        String id = attributes.getProperty("id");
        if (state.useStatementNamespaces) {
          id = state.applyNamespace(id);
        }
        if (state.sqlIncludes.containsKey(id)) {
          throw new SqlMapException("Duplicate <sql>-include '" + id + "' found.");
        }
        else  {
        	state.sqlIncludes.put(id, node);
        }
      }
    });
  }

  private void addTypeAliasNodelets() {
    parser.addNodelet("/sqlMap/typeAlias", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties prop = NodeletUtils.parseAttributes(node, state.globalProps);
        String alias = prop.getProperty("alias");
        String type = prop.getProperty("type");
        state.typeHandlerFactory.putTypeAlias(alias, type);
      }
    });
  }

  private void addCacheModelNodelets() {
    parser.addNodelet("/sqlMap/cacheModel", new Nodelet() {
      public void process(Node node) throws Exception {
        state.cacheModel = new CacheModel();
        state.cacheProps = new Properties();
      }
    });
    parser.addNodelet("/sqlMap/cacheModel/end()", new Nodelet() {
      public void process(Node node) throws Exception {
        state.errorContext.setActivity("building a cache model");

        Properties attributes = NodeletUtils.parseAttributes(node, state.globalProps);
        String id = state.applyNamespace(attributes.getProperty("id"));
        String type = attributes.getProperty("type");
        type = state.typeHandlerFactory.resolveAlias(type);

        String readOnly = attributes.getProperty("readOnly");
        if (readOnly != null && readOnly.length() > 0) {
          state.cacheModel.setReadOnly("true".equals(readOnly));
        } else {
          state.cacheModel.setReadOnly(true);
        }

        String serialize = attributes.getProperty("serialize");
        if (serialize != null && serialize.length() > 0) {
          state.cacheModel.setSerialize("true".equals(serialize));
        } else {
          state.cacheModel.setSerialize(false);
        }

        state.errorContext.setObjectId(id + " cache model");

        state.errorContext.setMoreInfo("Check the cache model type.");
        state.cacheModel.setId(id);
        state.cacheModel.setResource(state.errorContext.getResource());

        try {
          state.cacheModel.setControllerClassName(type);
        } catch (Exception e) {
          throw new RuntimeException("Error setting Cache Controller Class.  Cause: " + e, e);
        }

        state.errorContext.setMoreInfo("Check the cache model configuration.");
        state.cacheModel.configure(state.cacheProps);

        if (state.client.getDelegate().isCacheModelsEnabled()) {
          state.client.getDelegate().addCacheModel(state.cacheModel);
        }

        state.errorContext.setMoreInfo(null);
        state.errorContext.setObjectId(null);
        state.cacheProps = null;
        state.cacheModel = null;
      }
    });
    parser.addNodelet("/sqlMap/cacheModel/property", new Nodelet() {
      public void process(Node node) throws Exception {
        state.errorContext.setMoreInfo("Check the cache model properties.");
        Properties attributes = NodeletUtils.parseAttributes(node, state.globalProps);
        String name = attributes.getProperty("name");
        String value = NodeletUtils.parsePropertyTokens(attributes.getProperty("value"), state.globalProps);
        state.cacheProps.put(name, value);
      }
    });
    parser.addNodelet("/sqlMap/cacheModel/flushOnExecute", new Nodelet() {
      public void process(Node node) throws Exception {
        state.errorContext.setMoreInfo("Check the cache model flush on statement elements.");
        Properties childAttributes = NodeletUtils.parseAttributes(node, state.globalProps);
        state.cacheModel.addFlushTriggerStatement(childAttributes.getProperty("statement"));
      }
    });
    parser.addNodelet("/sqlMap/cacheModel/flushInterval", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties childAttributes = NodeletUtils.parseAttributes(node, state.globalProps);
        long t = 0;
        try {
          state.errorContext.setMoreInfo("Check the cache model flush interval.");
          String milliseconds = childAttributes.getProperty("milliseconds");
          String seconds = childAttributes.getProperty("seconds");
          String minutes = childAttributes.getProperty("minutes");
          String hours = childAttributes.getProperty("hours");
          if (milliseconds != null) t += Integer.parseInt(milliseconds);
          if (seconds != null) t += Integer.parseInt(seconds) * 1000;
          if (minutes != null) t += Integer.parseInt(minutes) * 60 * 1000;
          if (hours != null) t += Integer.parseInt(hours) * 60 * 60 * 1000;
          if (t < 1) throw new RuntimeException("A flush interval must specify one or more of milliseconds, seconds, minutes or hours.");
          state.cacheModel.setFlushInterval(t);
        } catch (NumberFormatException e) {
          throw new RuntimeException("Error building cache '" + state.cacheModel.getId() + "' in '" + "resourceNAME" + "'.  Flush interval milliseconds must be a valid long integer value.  Cause: " + e, e);
        }
      }
    });
  }

  private void addParameterMapNodelets() {
    parser.addNodelet("/sqlMap/parameterMap/end()", new Nodelet() {
      public void process(Node node) throws Exception {

        state.parameterMap.setParameterMappingList(state.parameterMappingList);

        state.client.getDelegate().addParameterMap(state.parameterMap);

        state.errorContext.setMoreInfo(null);
        state.errorContext.setObjectId(null);
      }
    });
    parser.addNodelet("/sqlMap/parameterMap", new Nodelet() {
      public void process(Node node) throws Exception {
        state.errorContext.setActivity("building a parameter map");

        state.parameterMap = new BasicParameterMap(state.client.getDelegate());

        Properties attributes = NodeletUtils.parseAttributes(node, state.globalProps);
        String id = state.applyNamespace(attributes.getProperty("id"));
        String parameterClassName = attributes.getProperty("class");
        parameterClassName = state.typeHandlerFactory.resolveAlias(parameterClassName);

        state.parameterMap.setId(id);
        state.parameterMap.setResource(state.errorContext.getResource());

        state.errorContext.setObjectId(id + " parameter map");

        Class parameterClass = null;
        try {
          state.errorContext.setMoreInfo("Check the parameter class.");
          parameterClass = Resources.classForName(parameterClassName);
        } catch (Exception e) {
          //TODO: Why is this commented out?
          //throw new SqlMapException("Error configuring ParameterMap.  Could not set ParameterClass.  Cause: " + e, e);
        }

        state.parameterMap.setParameterClass(parameterClass);

        state.parameterMappingList = new ArrayList();

        state.errorContext.setMoreInfo("Check the parameter mappings.");
      }
    });
    parser.addNodelet("/sqlMap/parameterMap/parameter", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties childAttributes = NodeletUtils.parseAttributes(node, state.globalProps);
        String propertyName = childAttributes.getProperty("property");
        String jdbcType = childAttributes.getProperty("jdbcType");
        String type     = childAttributes.getProperty("typeName");
        String javaType = childAttributes.getProperty("javaType");
        String resultMap = childAttributes.getProperty("resultMap");
        String nullValue = childAttributes.getProperty("nullValue");
        String mode = childAttributes.getProperty("mode");
        String callback = childAttributes.getProperty("typeHandler");
        String numericScale = childAttributes.getProperty("numericScale");

        callback = state.typeHandlerFactory.resolveAlias(callback);
        javaType = state.typeHandlerFactory.resolveAlias(javaType);
        resultMap = state.applyNamespace( resultMap );

        state.errorContext.setObjectId(propertyName + " mapping of the " + state.parameterMap.getId() + " parameter map");

        TypeHandler handler = null;
        if (callback != null) {
          state.errorContext.setMoreInfo("Check the parameter mapping typeHandler attribute '" + callback + "' (must be a TypeHandler or TypeHandlerCallback implementation).");
          try {
            Object impl = Resources.instantiate(callback);
            if (impl instanceof TypeHandlerCallback) {
              handler = new CustomTypeHandler((TypeHandlerCallback) impl);
            } else if (impl instanceof TypeHandler) {
              handler = (TypeHandler) impl;
            } else {
              throw new RuntimeException ("The class '"+callback+"' is not a valid implementation of TypeHandler or TypeHandlerCallback");
            }
          } catch (Exception e) {
            throw new RuntimeException("Error occurred during custom type handler configuration.  Cause: " + e, e);
          }
        } else {
          state.errorContext.setMoreInfo("Check the parameter mapping property type or name.");
          handler = state.resolveTypeHandler(state.client.getDelegate().getTypeHandlerFactory(), state.parameterMap.getParameterClass(), propertyName, javaType, jdbcType);
        }

        BasicParameterMapping mapping = new BasicParameterMapping();
        mapping.setPropertyName(propertyName);
        mapping.setJdbcTypeName(jdbcType);
        mapping.setTypeName(type);
        mapping.setResultMapName( resultMap );
        mapping.setNullValue(nullValue);
        if (mode != null && mode.length() > 0) {
          mapping.setMode(mode);
        }
        mapping.setTypeHandler(handler);
        try {
          if (javaType != null && javaType.length() > 0) {
            mapping.setJavaType(Resources.classForName(javaType));
          }
        } catch (ClassNotFoundException e) {
          throw new RuntimeException("Error setting javaType on parameter mapping.  Cause: " + e);
        }
        
        if (numericScale != null) {
          try {
            Integer scale = Integer.valueOf(numericScale);
            if (scale.intValue() < 0) {
              throw new RuntimeException("Error setting numericScale on parameter mapping.  Cause: scale must be greater than or equal to zero");
            }
            
            mapping.setNumericScale(scale);
          } catch (NumberFormatException e) {
            throw new RuntimeException("Error setting numericScale on parameter mapping.  Cause: " + numericScale + " is not a valid integer");
          }
        }

        state.parameterMappingList.add(mapping);

      }
    });
  }

  private void addResultMapNodelets() {
    parser.addNodelet("/sqlMap/resultMap/end()", new Nodelet() {
      public void process(Node node) throws Exception {
        
        if (state.resultMappingList.size() == 0) {
          throw new RuntimeException("resultMap " + state.resultMap.getId() + " must have at least one result mapping");
        }
        
        state.resultMap.setResultMappingList(state.resultMappingList);

        state.resultMap.setDiscriminator(state.discriminator);
        state.discriminator = null;
        
        state.client.getDelegate().addResultMap(state.resultMap);

        state.errorContext.setMoreInfo(null);

        state.errorContext.setObjectId(null);
      }
    });
    parser.addNodelet("/sqlMap/resultMap", new Nodelet() {
      public void process(Node node) throws Exception {
        state.errorContext.setActivity("building a result map");

        state.resultMap = new BasicResultMap(state.client.getDelegate());

        Properties attributes = NodeletUtils.parseAttributes(node, state.globalProps);
        String id = state.applyNamespace(attributes.getProperty("id"));
        String resultClassName = attributes.getProperty("class");
        String extended = state.applyNamespace(attributes.getProperty("extends"));
        String xmlName = attributes.getProperty("xmlName");
        String groupBy = attributes.getProperty("groupBy");
        resultClassName = state.typeHandlerFactory.resolveAlias(resultClassName);

        state.errorContext.setObjectId(id + " result map");

        state.resultMap.setId(id);
        state.resultMap.setXmlName(xmlName);
        state.resultMap.setResource(state.errorContext.getResource());

        if (groupBy != null && groupBy.length() > 0) {
          StringTokenizer parser = new StringTokenizer(groupBy, ", ", false);
          while (parser.hasMoreTokens()) {
            state.resultMap.addGroupByProperty(parser.nextToken());
          }
        }

        Class resultClass = null;
        try {
          state.errorContext.setMoreInfo("Check the result class.");
          resultClass = Resources.classForName(resultClassName);
        } catch (Exception e) {
          throw new RuntimeException("Error configuring Result.  Could not set ResultClass.  Cause: " + e, e);

        }

        state.resultMap.setResultClass(resultClass);

        state.resultMappingList = new ArrayList();

        state.errorContext.setMoreInfo("Check the extended result map.");
        if (extended != null) {
          BasicResultMap extendedResultMap = (BasicResultMap) state.client.getDelegate().getResultMap(extended);
          ResultMapping[] resultMappings = extendedResultMap.getResultMappings();
          for (int i = 0; i < resultMappings.length; i++) {
            state.resultMappingList.add(resultMappings[i]);
          }

          List nestedResultMappings = extendedResultMap.getNestedResultMappings();
          if (nestedResultMappings != null) {
            Iterator iter = nestedResultMappings.iterator();
            while (iter.hasNext()) {
              state.resultMap.addNestedResultMappings((ResultMapping) iter.next());
            }
          }
          
          if (groupBy == null || groupBy.length() == 0) {
            if (extendedResultMap.hasGroupBy()) {
              Iterator i = extendedResultMap.groupByProps();
              while (i.hasNext()) {
                state.resultMap.addGroupByProperty((String) i.next());
              }
            }
          }
        }

        state.errorContext.setMoreInfo("Check the result mappings.");
        state.resultMappingIndex = state.resultMappingList.size();

      }
    });
    parser.addNodelet("/sqlMap/resultMap/result", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties childAttributes = NodeletUtils.parseAttributes(node, state.globalProps);
        String propertyName = childAttributes.getProperty("property");
        String nullValue = childAttributes.getProperty("nullValue");
        String jdbcType = childAttributes.getProperty("jdbcType");
        String javaType = childAttributes.getProperty("javaType");
        String columnName = childAttributes.getProperty("column");
        String columnIndex = childAttributes.getProperty("columnIndex");
        String statementName = childAttributes.getProperty("select");
        String resultMapName = childAttributes.getProperty("resultMap");
        String callback = childAttributes.getProperty("typeHandler");

        callback = state.typeHandlerFactory.resolveAlias(callback);
        javaType = state.typeHandlerFactory.resolveAlias(javaType);

        state.errorContext.setObjectId(propertyName + " mapping of the " + state.resultMap.getId() + " result map");

        TypeHandler handler = null;
        if (callback != null) {
          state.errorContext.setMoreInfo("Check the result mapping typeHandler attribute '" + callback + "' (must be a TypeHandler or TypeHandlerCallback implementation).");
          try {
            Object impl = Resources.instantiate(callback);
            if (impl instanceof TypeHandlerCallback) {
              handler = new CustomTypeHandler((TypeHandlerCallback) impl);
            } else if (impl instanceof TypeHandler) {
              handler = (TypeHandler) impl;
            } else {
              throw new RuntimeException ("The class '"+callback+"' is not a valid implementation of TypeHandler or TypeHandlerCallback");
            }
          } catch (Exception e) {
            throw new RuntimeException("Error occurred during custom type handler configuration.  Cause: " + e, e);
          }
        } else {
          state.errorContext.setMoreInfo("Check the result mapping property type or name.");
          handler = state.resolveTypeHandler(state.client.getDelegate().getTypeHandlerFactory(), state.resultMap.getResultClass(), propertyName, javaType, jdbcType, true);
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
          state.resultMap.addNestedResultMappings(mapping);
        }

        try {
          if (javaType != null && javaType.length() > 0) {
            mapping.setJavaType(Resources.classForName(javaType));
          }
        } catch (ClassNotFoundException e) {
          throw new RuntimeException("Error setting javaType on result mapping.  Cause: " + e);
        }

        if (columnIndex != null && columnIndex.length() > 0) {
          mapping.setColumnIndex(Integer.parseInt(columnIndex));
        } else {
          state.resultMappingIndex++;
          mapping.setColumnIndex(state.resultMappingIndex);
        }

        state.resultMappingList.add(mapping);
      }
    });

    parser.addNodelet("/sqlMap/resultMap/discriminator/subMap", new Nodelet() {
      public void process(Node node) throws Exception {
        if (state.discriminator == null) {
          throw new RuntimeException ("The discriminator is null, but somehow a subMap was reached.  This is a bug.");
        }
        Properties childAttributes = NodeletUtils.parseAttributes(node, state.globalProps);
        String value = childAttributes.getProperty("value");
        String resultMap = childAttributes.getProperty("resultMap");
        state.discriminator.addSubMap(value, state.applyNamespace(resultMap));
      }
    });

    parser.addNodelet("/sqlMap/resultMap/discriminator", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties childAttributes = NodeletUtils.parseAttributes(node, state.globalProps);
        String nullValue = childAttributes.getProperty("nullValue");
        String jdbcType = childAttributes.getProperty("jdbcType");
        String javaType = childAttributes.getProperty("javaType");
        String columnName = childAttributes.getProperty("column");
        String columnIndex = childAttributes.getProperty("columnIndex");
        String callback = childAttributes.getProperty("typeHandler");

        callback = state.typeHandlerFactory.resolveAlias(callback);
        javaType = state.typeHandlerFactory.resolveAlias(javaType);

        TypeHandler handler = null;
        if (callback != null) {
          state.errorContext.setMoreInfo("Check the result mapping typeHandler attribute '" + callback + "' (must be a TypeHandlerCallback implementation).");
          try {
            Object impl = Resources.instantiate(callback);
            if (impl instanceof TypeHandlerCallback) {
              handler = new CustomTypeHandler((TypeHandlerCallback) impl);
            } else if (impl instanceof TypeHandler) {
              handler = (TypeHandler) impl;
            } else {
              throw new RuntimeException ("The class '' is not a valid implementation of TypeHandler or TypeHandlerCallback");
            }
          } catch (Exception e) {
            throw new RuntimeException("Error occurred during custom type handler configuration.  Cause: " + e, e);
          }
        } else {
          state.errorContext.setMoreInfo("Check the result mapping property type or name.");
          handler = state.resolveTypeHandler(state.client.getDelegate().getTypeHandlerFactory(), state.resultMap.getResultClass(), "", javaType, jdbcType, true);
        }

        BasicResultMapping mapping = new BasicResultMapping();
        mapping.setColumnName(columnName);
        mapping.setJdbcTypeName(jdbcType);
        mapping.setTypeHandler(handler);
        mapping.setNullValue(nullValue);

        try {
          if (javaType != null && javaType.length() > 0) {
            mapping.setJavaType(Resources.classForName(javaType));
          }
        } catch (ClassNotFoundException e) {
          throw new RuntimeException("Error setting javaType on result mapping.  Cause: " + e);
        }

        if (columnIndex != null && columnIndex.length() > 0) {
          mapping.setColumnIndex(Integer.parseInt(columnIndex));
        }

        state.discriminator = new Discriminator (state.delegate, mapping);
      }
    });
  }

  protected void addStatementNodelets() {
    parser.addNodelet("/sqlMap/statement", new Nodelet() {
      public void process(Node node) throws Exception {
        state.currentStatement = new SqlStatementParser(state).parseGeneralStatement(node, new GeneralStatement());
        state.delegate.addMappedStatement(state.currentStatement);
      }
    });
    parser.addNodelet("/sqlMap/insert", new Nodelet() {
      public void process(Node node) throws Exception {
        state.currentStatement = new SqlStatementParser(state).parseGeneralStatement(node, new InsertStatement());
        state.delegate.addMappedStatement(state.currentStatement);
      }
    });
    parser.addNodelet("/sqlMap/update", new Nodelet() {
      public void process(Node node) throws Exception {
        state.currentStatement = new SqlStatementParser(state).parseGeneralStatement(node, new UpdateStatement());
        state.delegate.addMappedStatement(state.currentStatement);
      }
    });
    parser.addNodelet("/sqlMap/delete", new Nodelet() {
      public void process(Node node) throws Exception {
        state.currentStatement = new SqlStatementParser(state).parseGeneralStatement(node, new DeleteStatement());
        state.delegate.addMappedStatement(state.currentStatement);
      }
    });
    parser.addNodelet("/sqlMap/select", new Nodelet() {
      public void process(Node node) throws Exception {
        state.currentStatement = new SqlStatementParser(state).parseGeneralStatement(node, new SelectStatement());
        state.delegate.addMappedStatement(state.currentStatement);
      }
    });
    parser.addNodelet("/sqlMap/procedure", new Nodelet() {
      public void process(Node node) throws Exception {
        state.currentStatement = new SqlStatementParser(state).parseGeneralStatement(node, new ProcedureStatement());
        state.delegate.addMappedStatement(state.currentStatement);
      }
    });
  }


}
