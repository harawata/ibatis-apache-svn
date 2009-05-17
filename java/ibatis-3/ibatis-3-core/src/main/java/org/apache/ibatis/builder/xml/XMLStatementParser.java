package org.apache.ibatis.builder.xml;

import org.apache.ibatis.mapping.*;
import org.apache.ibatis.builder.BaseParser;
import org.apache.ibatis.builder.ParserException;
import org.apache.ibatis.builder.SequentialMapperBuilder;
import org.apache.ibatis.builder.xml.dynamic.*;
import org.apache.ibatis.parsing.NodeletContext;
import org.apache.ibatis.executor.keygen.*;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XMLStatementParser extends BaseParser {

  private SequentialMapperBuilder sequentialBuilder;
  private XMLMapperParser xmlMapperParser;

  public XMLStatementParser(Configuration configuration, SequentialMapperBuilder sequentialBuilder, XMLMapperParser xmlMapperParser) {
    super(configuration);
    this.sequentialBuilder = sequentialBuilder;
    this.xmlMapperParser = xmlMapperParser;
  }

  public void parseStatementNode(NodeletContext context) {
    String id = context.getStringAttribute("id");
    Integer fetchSize = context.getIntAttribute("fetchSize", null);
    Integer timeout = context.getIntAttribute("timeout", null);
    String parameterMap = context.getStringAttribute("parameterMap");
    String parameterType = context.getStringAttribute("parameterType");
    Class parameterTypeClass = resolveClass(parameterType);
    String resultMap = context.getStringAttribute("resultMap");
    String resultType = context.getStringAttribute("resultType");

    Class resultTypeClass = resolveClass(resultType);
    String resultSetType = context.getStringAttribute("resultSetType");
    StatementType statementType = StatementType.valueOf(context.getStringAttribute("statementType", StatementType.PREPARED.toString()));
    ResultSetType resultSetTypeEnum = resolveResultSetType(resultSetType);

    List<SqlNode> contents = parseDynamicTags(context);
    MixedSqlNode rootSqlNode = new MixedSqlNode(contents);
    SqlSource sqlSource = new DynamicSqlSource(configuration, rootSqlNode);
    String nodeName = context.getNode().getNodeName();
    SqlCommandType sqlCommandType = SqlCommandType.valueOf(nodeName.toUpperCase());
    boolean isSelect = sqlCommandType == SqlCommandType.SELECT;
    boolean flushCache = context.getBooleanAttribute("flushCache", !isSelect);
    boolean useCache = context.getBooleanAttribute("useCache", isSelect);

    String keyProperty = context.getStringAttribute("keyProperty");
    KeyGenerator keyGenerator;
    String keyStatementId = id + SelectKeyGenerator.SELECT_KEY_SUFFIX;
    if (configuration.hasKeyGenerator(keyStatementId)) {
      keyGenerator = configuration.getKeyGenerator(keyStatementId);
    } else {
      keyGenerator = context.getBooleanAttribute("useGeneratedKeys",
        configuration.isUseGeneratedKeys() && SqlCommandType.INSERT.equals(sqlCommandType))
        ? new Jdbc3KeyGenerator() : null;
    }

    sequentialBuilder.statement(id, sqlSource, statementType, sqlCommandType, fetchSize, timeout, parameterMap, parameterTypeClass,
        resultMap, resultTypeClass, resultSetTypeEnum, flushCache, useCache, keyGenerator,keyProperty);
  }


  private List<SqlNode> parseDynamicTags(NodeletContext node) {
    List<SqlNode> contents = new ArrayList<SqlNode>();
    NodeList children = node.getNode().getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      NodeletContext child = new NodeletContext(children.item(i), configuration.getVariables());
      String nodeName = child.getNode().getNodeName();
      if (child.getNode().getNodeType() == Node.CDATA_SECTION_NODE
          || child.getNode().getNodeType() == Node.TEXT_NODE) {
        String data = child.getStringBody("");
        contents.add(new TextSqlNode(data));
      } else {
        NodeHandler handler = nodeHandlers.get(nodeName);
        if (handler == null) {
          throw new ParserException("Unknown element <" + nodeName + "> in SQL statement.");
        }
        handler.handleNode(child, contents);

      }
    }
    return contents;
  }

  private Map<String, NodeHandler> nodeHandlers = new HashMap<String, NodeHandler>() {
    {
      put("include", new IncludeNodeHandler());
      put("prefix", new PrefixHandler());
      put("where", new WhereHandler());
      put("set", new SetHandler());
      put("foreach", new ForEachHandler());
      put("if", new IfHandler());
      put("choose", new ChooseHandler());
      put("when", new IfHandler());
      put("otherwise", new OtherwiseHandler());
      put("selectKey", new SelectKeyHandler());
    }
  };

  private interface NodeHandler {
    void handleNode(NodeletContext nodeToHandle, List<SqlNode> targetContents);
  }

  private class SelectKeyHandler implements NodeHandler {
    public void handleNode(NodeletContext nodeToHandle, List<SqlNode> targetContents) {
      NodeletContext parent = nodeToHandle.getParent();
      String id = parent.getStringAttribute("id") + SelectKeyGenerator.SELECT_KEY_SUFFIX;
      String resultType = nodeToHandle.getStringAttribute("resultType");
      Class resultTypeClass = resolveClass(resultType);
      StatementType statementType = StatementType.valueOf(nodeToHandle.getStringAttribute("statementType", StatementType.PREPARED.toString()));
      String keyProperty = nodeToHandle.getStringAttribute("keyProperty");
      String parameterType = parent.getStringAttribute("parameterType");
      boolean executeBefore = "BEFORE".equals(nodeToHandle.getStringAttribute("order","AFTER"));
      Class parameterTypeClass = resolveClass(parameterType);

      //defaults
      boolean useCache = false;
      KeyGenerator keyGenerator = null;
      Integer fetchSize = null;
      Integer timeout = null;
      boolean flushCache = false;
      String parameterMap = null;
      String resultMap = null;
      ResultSetType resultSetTypeEnum = null;

      List<SqlNode> contents = parseDynamicTags(nodeToHandle);
      MixedSqlNode rootSqlNode = new MixedSqlNode(contents);
      SqlSource sqlSource = new DynamicSqlSource(configuration, rootSqlNode);
      SqlCommandType sqlCommandType = SqlCommandType.SELECT;

      sequentialBuilder.statement(id, sqlSource, statementType, sqlCommandType, fetchSize, timeout, parameterMap, parameterTypeClass,
          resultMap, resultTypeClass, resultSetTypeEnum, flushCache, useCache,
          keyGenerator,keyProperty);

      MappedStatement keyStatement = configuration.getMappedStatement(sequentialBuilder.applyNamespace(id));

      configuration.addKeyGenerator(id, new SelectKeyGenerator(keyStatement,executeBefore));
    }
  }

  private class IncludeNodeHandler implements NodeHandler {
    public void handleNode(NodeletContext nodeToHandle, List<SqlNode> targetContents) {
      String refid = nodeToHandle.getStringAttribute("refid");
      NodeletContext includeNode = xmlMapperParser.getSqlFragment(refid);
      if (includeNode == null) {
        String nsrefid = sequentialBuilder.applyNamespace(refid);
        includeNode = xmlMapperParser.getSqlFragment(nsrefid);
        if (includeNode == null) {
          throw new RuntimeException("Could not find SQL statement to include with refid '" + refid + "'");
        }
      }
      MixedSqlNode mixedSqlNode = new MixedSqlNode(contents(includeNode));
      targetContents.add(mixedSqlNode);
    }

    private List<SqlNode> contents(NodeletContext includeNode) {
      return parseDynamicTags(includeNode);
    }
  }

  private class PrefixHandler implements NodeHandler {
    public void handleNode(NodeletContext nodeToHandle, List<SqlNode> targetContents) {
      List<SqlNode> contents = parseDynamicTags(nodeToHandle);
      MixedSqlNode mixedSqlNode = new MixedSqlNode(contents);
      String with = nodeToHandle.getStringAttribute("with");
      String overrides = nodeToHandle.getStringAttribute("overrides");
      PrefixSqlNode prefix = new PrefixSqlNode(mixedSqlNode, with, overrides);
      targetContents.add(prefix);
    }
  }

  private class WhereHandler implements NodeHandler {
    public void handleNode(NodeletContext nodeToHandle, List<SqlNode> targetContents) {
      List<SqlNode> contents = parseDynamicTags(nodeToHandle);
      MixedSqlNode mixedSqlNode = new MixedSqlNode(contents);
      WhereSqlNode where = new WhereSqlNode(mixedSqlNode);
      targetContents.add(where);
    }
  }

  private class SetHandler implements NodeHandler {
    public void handleNode(NodeletContext nodeToHandle, List<SqlNode> targetContents) {
      List<SqlNode> contents = parseDynamicTags(nodeToHandle);
      MixedSqlNode mixedSqlNode = new MixedSqlNode(contents);
      SetSqlNode set = new SetSqlNode(mixedSqlNode);
      targetContents.add(set);
    }
  }

  private class ForEachHandler implements NodeHandler {
    public void handleNode(NodeletContext nodeToHandle, List<SqlNode> targetContents) {
      List<SqlNode> contents = parseDynamicTags(nodeToHandle);
      MixedSqlNode mixedSqlNode = new MixedSqlNode(contents);
      String collection = nodeToHandle.getStringAttribute("collection");
      String item = nodeToHandle.getStringAttribute("item");
      String index = nodeToHandle.getStringAttribute("index");
      String open = nodeToHandle.getStringAttribute("open");
      String close = nodeToHandle.getStringAttribute("close");
      String separator = nodeToHandle.getStringAttribute("separator");
      ForEachSqlNode forEachSqlNode = new ForEachSqlNode(mixedSqlNode, collection, index, item, open, close, separator);
      targetContents.add(forEachSqlNode);
    }
  }

  private class IfHandler implements NodeHandler {
    public void handleNode(NodeletContext nodeToHandle, List<SqlNode> targetContents) {
      List<SqlNode> contents = parseDynamicTags(nodeToHandle);
      MixedSqlNode mixedSqlNode = new MixedSqlNode(contents);
      String test = nodeToHandle.getStringAttribute("test");
      IfSqlNode ifSqlNode = new IfSqlNode(mixedSqlNode, test);
      targetContents.add(ifSqlNode);
    }
  }

  private class OtherwiseHandler implements NodeHandler {
    public void handleNode(NodeletContext nodeToHandle, List<SqlNode> targetContents) {
      List<SqlNode> contents = parseDynamicTags(nodeToHandle);
      MixedSqlNode mixedSqlNode = new MixedSqlNode(contents);
      targetContents.add(mixedSqlNode);
    }
  }

  private class ChooseHandler implements NodeHandler {
    public void handleNode(NodeletContext nodeToHandle, List<SqlNode> targetContents) {
      List whenSqlNodes = new ArrayList<SqlNode>();
      List<SqlNode> otherwiseSqlNodes = new ArrayList<SqlNode>();
      handleWhenOtherwiseNodes(nodeToHandle, whenSqlNodes, otherwiseSqlNodes);
      SqlNode defaultSqlNode = getDefaultSqlNode(otherwiseSqlNodes);
      ChooseSqlNode chooseSqlNode = new ChooseSqlNode((List<IfSqlNode>) whenSqlNodes, defaultSqlNode);
      targetContents.add(chooseSqlNode);
    }
    private void handleWhenOtherwiseNodes(NodeletContext chooseSqlNode, List ifSqlNodes, List<SqlNode> defaultSqlNodes) {
      List<NodeletContext> children = chooseSqlNode.getChildren();
      for (NodeletContext child : children) {
        String nodeName = child.getNode().getNodeName();
        NodeHandler handler = nodeHandlers.get(nodeName);
        if (handler instanceof IfHandler) {
          handler.handleNode(child, ifSqlNodes);
        } else if (handler instanceof OtherwiseHandler) {
          handler.handleNode(child, defaultSqlNodes);
        }
      }
    }
    private SqlNode getDefaultSqlNode(List<SqlNode> defaultSqlNodes) {
      SqlNode defaultSqlNode = null;
      if (defaultSqlNodes.size() == 1) {
        defaultSqlNode = defaultSqlNodes.get(0);
      } else if (defaultSqlNodes.size() > 1) {
        throw new ParserException("Too many default (otherwise) elements in choose statement.");
      }
      return defaultSqlNode;
    }
  }

}
