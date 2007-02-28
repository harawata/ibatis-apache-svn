package com.ibatis.sqlmap.engine.builder.xml;

import com.ibatis.common.xml.Nodelet;
import com.ibatis.common.xml.NodeletException;
import com.ibatis.common.xml.NodeletParser;
import com.ibatis.common.xml.NodeletUtils;
import com.ibatis.sqlmap.client.SqlMapException;
import com.ibatis.sqlmap.engine.cache.CacheModel;
import com.ibatis.sqlmap.engine.mapping.statement.*;
import org.w3c.dom.Node;

import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

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
        state.bindDelegateSubMaps();
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
        } else {
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
        state.addTypeAlias(alias, type);
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
        Properties attributes = NodeletUtils.parseAttributes(node, state.globalProps);
        String id = state.applyNamespace(attributes.getProperty("id"));
        String type = attributes.getProperty("type");
        String readOnlyAttr = attributes.getProperty("readOnly");
        Boolean readOnly = readOnlyAttr == null || readOnlyAttr.length() <= 0 ? null : new Boolean("true".equals(readOnlyAttr));
        String serializeAttr = attributes.getProperty("serialize");
        Boolean serialize = serializeAttr == null || serializeAttr.length() <= 0 ? null : new Boolean("true".equals(serializeAttr));
        state.addCacheModel(id, type, readOnly, serialize, state.cacheProps);
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
        Properties childAttributes = NodeletUtils.parseAttributes(node, state.globalProps);
        String statement = childAttributes.getProperty("statement");
        state.addFlushTriggerStatement(statement);
      }
    });
    parser.addNodelet("/sqlMap/cacheModel/flushInterval", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties childAttributes = NodeletUtils.parseAttributes(node, state.globalProps);
        try {
          int milliseconds = childAttributes.getProperty("milliseconds") == null ? 0 : Integer.parseInt(childAttributes.getProperty("milliseconds"));
          int seconds = childAttributes.getProperty("seconds") == null ? 0 : Integer.parseInt(childAttributes.getProperty("seconds"));
          int minutes = childAttributes.getProperty("minutes") == null ? 0 : Integer.parseInt(childAttributes.getProperty("minutes"));
          int hours = childAttributes.getProperty("hours") == null ? 0 : Integer.parseInt(childAttributes.getProperty("hours"));
          state.setFlushInterval(hours, minutes, seconds, milliseconds);
        } catch (NumberFormatException e) {
          throw new RuntimeException("Error building cache '" + state.cacheModel.getId() + "' in '" + "resourceNAME" + "'.  Flush interval milliseconds must be a valid long integer value.  Cause: " + e, e);
        }
      }
    });
  }


  private void addParameterMapNodelets() {
    parser.addNodelet("/sqlMap/parameterMap/end()", new Nodelet() {
      public void process(Node node) throws Exception {

        state.finalizeParameterMap();
      }
    });
    parser.addNodelet("/sqlMap/parameterMap", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties attributes = NodeletUtils.parseAttributes(node, state.globalProps);
        String id = state.applyNamespace(attributes.getProperty("id"));
        String parameterClassName = attributes.getProperty("class");

        state.addParameterMap(id, parameterClassName);
      }
    });
    parser.addNodelet("/sqlMap/parameterMap/parameter", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties childAttributes = NodeletUtils.parseAttributes(node, state.globalProps);
        String propertyName = childAttributes.getProperty("property");
        String jdbcType = childAttributes.getProperty("jdbcType");
        String type = childAttributes.getProperty("typeName");
        String javaType = childAttributes.getProperty("javaType");
        String resultMap = childAttributes.getProperty("resultMap");
        String nullValue = childAttributes.getProperty("nullValue");
        String mode = childAttributes.getProperty("mode");
        String callback = childAttributes.getProperty("typeHandler");
        String numericScale = childAttributes.getProperty("numericScale");

        state.addParameterMapping(callback, javaType, resultMap, propertyName, jdbcType, type, nullValue, mode, numericScale);

      }
    });
  }


  private void addResultMapNodelets() {
    parser.addNodelet("/sqlMap/resultMap/end()", new Nodelet() {
      public void process(Node node) throws Exception {
        state.finalizeResultMap();
      }
    });
    parser.addNodelet("/sqlMap/resultMap", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties attributes = NodeletUtils.parseAttributes(node, state.globalProps);
        String id = state.applyNamespace(attributes.getProperty("id"));
        String resultClassName = attributes.getProperty("class");
        String extended = state.applyNamespace(attributes.getProperty("extends"));
        String xmlName = attributes.getProperty("xmlName");
        String groupBy = attributes.getProperty("groupBy");
        state.addResultMap(id, resultClassName, xmlName, groupBy, extended);
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

        state.addResultMapping(callback, javaType, propertyName, jdbcType, columnName, nullValue, statementName, resultMapName, columnIndex);
      }
    });

    parser.addNodelet("/sqlMap/resultMap/discriminator/subMap", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties childAttributes = NodeletUtils.parseAttributes(node, state.globalProps);
        String value = childAttributes.getProperty("value");
        String resultMap = childAttributes.getProperty("resultMap");
        state.addSubMap(value, resultMap);
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

        state.addDiscriminator(callback, javaType, jdbcType, columnName, nullValue, columnIndex);
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
