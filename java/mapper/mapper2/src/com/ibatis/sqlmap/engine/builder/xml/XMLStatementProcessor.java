package com.ibatis.sqlmap.engine.builder.xml;

import org.w3c.dom.*;
import com.ibatis.sqlmap.engine.mapping.statement.*;
import com.ibatis.sqlmap.engine.mapping.sql.dynamic.DynamicSql;
import com.ibatis.sqlmap.engine.mapping.sql.dynamic.elements.*;
import com.ibatis.sqlmap.engine.mapping.sql.SqlText;
import com.ibatis.sqlmap.engine.mapping.parameter.InlineParameterMapParser;
import com.ibatis.common.xml.NodeletUtils;

import java.util.Properties;

public class XMLStatementProcessor implements StatementProcessor {

  private static final InlineParameterMapParser PARAM_PARSER = new InlineParameterMapParser();

  private ParserState state;
  private Node parentNode;


  public XMLStatementProcessor(ParserState state, Node parentNode) {
    this.state = state;
    this.parentNode = parentNode;
  }

  public void processStatement(GeneralStatement statement) {
    state.errorContext.setActivity("processing an SQL statement");

    boolean isDynamic = false;
    DynamicSql dynamic = new DynamicSql(state.client.getDelegate());
    StringBuffer sqlBuffer = new StringBuffer();

    isDynamic = parseDynamicTags(parentNode, dynamic, sqlBuffer, isDynamic, false);

    if (statement instanceof InsertStatement) {
      InsertStatement insertStatement = ((InsertStatement) statement);
      SelectKeyStatement selectKeyStatement = findAndParseSelectKeyStatement(parentNode, statement.getId(), statement.getParameterClass());
      insertStatement.setSelectKeyStatement(selectKeyStatement);
    }

    String sqlStatement = sqlBuffer.toString();
    if (isDynamic) {
      statement.setSql(dynamic);
    } else {
      state.applyInlineParameterMap(statement, sqlStatement);
    }

  }

  private boolean parseDynamicTags(Node node, DynamicParent dynamic, StringBuffer sqlBuffer, boolean isDynamic, boolean postParseRequired) {
    state.errorContext.setActivity("parsing dynamic SQL tags");

    NodeList children = node.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      String nodeName = child.getNodeName();
      if (child.getNodeType() == Node.CDATA_SECTION_NODE
          || child.getNodeType() == Node.TEXT_NODE) {

        String data = ((CharacterData) child).getData();
        data = NodeletUtils.parsePropertyTokens(data, state.globalProps);

        SqlText sqlText;

        if (postParseRequired) {
          sqlText = new SqlText();
          sqlText.setPostParseRequired(postParseRequired);
          sqlText.setText(data);
        } else {
          sqlText = PARAM_PARSER.parseInlineParameterMap(state.client.getDelegate().getTypeHandlerFactory(), data, null);
          sqlText.setPostParseRequired(postParseRequired);
        }

        dynamic.addChild(sqlText);

        sqlBuffer.append(data);
      } else if ("include".equals(nodeName)) {
        Properties attributes = NodeletUtils.parseAttributes(child, state.globalProps);
        String refid = (String) attributes.get("refid");
        Node includeNode = (Node) state.sqlIncludes.get(refid);
        if (includeNode == null) {
          String nsrefid = state.applyNamespace(refid);
          includeNode = (Node) state.sqlIncludes.get(nsrefid);
          if (includeNode == null) {
            throw new RuntimeException("Could not find SQL statement to include with refid '" + refid + "'");
          }
        }
        isDynamic = parseDynamicTags(includeNode, dynamic, sqlBuffer, isDynamic, false);
      } else {
        state.errorContext.setMoreInfo("Check the dynamic tags.");

        SqlTagHandler handler = SqlTagHandlerFactory.getSqlTagHandler(nodeName);
        if (handler != null) {
          isDynamic = true;

          SqlTag tag = new SqlTag();
          tag.setName(nodeName);
          tag.setHandler(handler);

          Properties attributes = NodeletUtils.parseAttributes(child, state.globalProps);

          tag.setPrependAttr(attributes.getProperty("prepend"));
          tag.setPropertyAttr(attributes.getProperty("property"));
          tag.setRemoveFirstPrepend(attributes.getProperty("removeFirstPrepend"));

          tag.setOpenAttr(attributes.getProperty("open"));
          tag.setCloseAttr(attributes.getProperty("close"));

          tag.setComparePropertyAttr(attributes.getProperty("compareProperty"));
          tag.setCompareValueAttr(attributes.getProperty("compareValue"));
          tag.setConjunctionAttr(attributes.getProperty("conjunction"));

          // an iterate ancestor requires a post parse

          if (dynamic instanceof SqlTag) {
            SqlTag parentSqlTag = (SqlTag) dynamic;
            if (parentSqlTag.isPostParseRequired() ||
                tag.getHandler() instanceof IterateTagHandler) {
              tag.setPostParseRequired(true);
            }
          } else if (dynamic instanceof DynamicSql) {
            if (tag.getHandler() instanceof IterateTagHandler) {
              tag.setPostParseRequired(true);
            }
          }

          dynamic.addChild(tag);

          if (child.hasChildNodes()) {
            isDynamic = parseDynamicTags(child, tag, sqlBuffer, isDynamic, tag.isPostParseRequired());
          }
        }
      }
    }
    state.errorContext.setMoreInfo(null);
    return isDynamic;
  }

  private SelectKeyStatement findAndParseSelectKeyStatement(Node n, String statementId, Class parameterClass) {
    state.errorContext.setActivity("parsing select key tags");

    SelectKeyStatement selectKeyStatement = null;

    boolean foundSQLFirst = false;

    NodeList children = n.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if (child.getNodeType() == Node.CDATA_SECTION_NODE
          || child.getNodeType() == Node.TEXT_NODE) {
        String data = ((CharacterData) child).getData();
        if (data.trim().length() > 0) {
          foundSQLFirst = true;
        }
      } else if (child.getNodeType() == Node.ELEMENT_NODE
          && "selectKey".equals(child.getNodeName())) {

        // get attributes
        Properties attributes = NodeletUtils.parseAttributes(child, state.globalProps);
        String keyPropName = attributes.getProperty("keyProperty");
        String resultClassName = attributes.getProperty("resultClass");
        String type = attributes.getProperty("type");

        selectKeyStatement = state.prepareSelectKeyStatement(new XMLStatementProcessor(state, child), resultClassName, statementId, keyPropName, foundSQLFirst, type, parameterClass);
        break;
      }
    }
    state.errorContext.setMoreInfo(null);
    return selectKeyStatement;
  }
}
