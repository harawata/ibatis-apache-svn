package org.apache.ibatis.builder.xml;

import org.apache.ibatis.builder.*;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.parsing.*;
import org.apache.ibatis.type.JdbcType;

import java.io.Reader;
import java.util.*;

public class XMLMapperBuilder extends BaseBuilder {

  private XPathParser parser;
  private SequentialMapperBuilder sequentialBuilder;
  private Map<String, XNode> sqlFragments = new HashMap<String, XNode>();

  public XMLMapperBuilder(Reader reader, Configuration configuration, String resource, String namespace) {
    this(reader, configuration, resource);
    this.sequentialBuilder.namespace(namespace);
  }

  public XMLMapperBuilder(Reader reader, Configuration configuration, String resource) {
    super(configuration);
    this.sequentialBuilder = new SequentialMapperBuilder(configuration, resource);
    this.parser = new XPathParser(reader,true,new XMLMapperEntityResolver(),configuration.getVariables());
  }

  public void parse() {
    configurationElement(parser.evalNode("/mapper"));
    bindMapperForNamespace();
  }

  public XNode getSqlFragment(String refid) {
    return sqlFragments.get(refid);
  }

  private void configurationElement(XNode context) {
    try {
      String namespace = context.getStringAttribute("namespace");
      sequentialBuilder.namespace(namespace);
      cacheRefElement(context.evalNode("cache-ref"));
      cacheElement(context.evalNode("cache"));
      parameterMapElement(context.evalNodes("/mapper/parameterMap"));
      resultMapElements(context.evalNodes("/mapper/resultMap"));
      sqlElement(context.evalNodes("/mapper/sql"));
      buildStatementFromContext(context.evalNodes("select|insert|update|delete"));
    } catch (Exception e) {
      throw new RuntimeException("Error parsing Mapper XML. Cause: " + e, e);
    }

  }

  private void cacheRefElement(XNode context) {
    if (context != null) {
      sequentialBuilder.cacheRef(context.getStringAttribute("namespace"));
    }
  }

  private void cacheElement(XNode context) throws Exception {
    if (context != null) {
      String type = context.getStringAttribute("type", "PERPETUAL");
      type = typeAliasRegistry.resolveAlias(type);
      Class typeClass = Class.forName(type);
      String eviction = context.getStringAttribute("eviction", "LRU");
      eviction = typeAliasRegistry.resolveAlias(eviction);
      Class evictionClass = Class.forName(eviction);
      Long flushInterval = context.getLongAttribute("flushInterval");
      Integer size = context.getIntAttribute("size");
      boolean readOnly = context.getBooleanAttribute("readOnly", false);
      Properties props = context.getChildrenAsProperties();
      sequentialBuilder.cache(typeClass, evictionClass, flushInterval, size, readOnly, props);
    }
  }

  private void parameterMapElement(List<XNode> list) throws Exception {
    for (XNode parameterMapNode : list) {
      String id = parameterMapNode.getStringAttribute("id");
      String type = parameterMapNode.getStringAttribute("type");
      Class parameterClass = resolveClass(type);
      sequentialBuilder.parameterMapStart(id, parameterClass);
      List<XNode> parameterNodes = parameterMapNode.evalNodes("parameter");
      for (XNode parameterNode : parameterNodes) {
        String property = parameterNode.getStringAttribute("property");
        String javaType = parameterNode.getStringAttribute("javaType");
        String jdbcType = parameterNode.getStringAttribute("jdbcType");
        String resultMap = parameterNode.getStringAttribute("resultMap");
        String mode = parameterNode.getStringAttribute("mode");
        String typeHandler = parameterNode.getStringAttribute("typeHandler");
        Integer numericScale = parameterNode.getIntAttribute("numericScale", null);
        ParameterMode modeEnum = resolveParameterMode(mode);
        Class javaTypeClass = resolveClass(javaType);
        JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);
        Class typeHandlerClass = resolveClass(typeHandler);
        sequentialBuilder.parameterMapping(property, javaTypeClass, jdbcTypeEnum, resultMap, modeEnum, typeHandlerClass, numericScale);
      }
      sequentialBuilder.parameterMapEnd();
    }
  }


  private void resultMapElements(List<XNode> list) throws Exception {
    for (XNode resultMapNode : list) {
      resultMapElement(resultMapNode);
    }
  }
  private void resultMapElement(XNode resultMapNode) throws Exception {
      ErrorContext.instance().activity("processing " + resultMapNode.getValueBasedIdentifier());
      String id = resultMapNode.getStringAttribute("id");
      String type = resultMapNode.getStringAttribute("type");
      String extend = resultMapNode.getStringAttribute("extends");
      if (id == null) {
        id = resultMapNode.getValueBasedIdentifier();
      }
      Class typeClass = resolveClass(type);
      processChildrenAsResultMap(resultMapNode);
      sequentialBuilder.resultMapStart(id, typeClass, extend);
      List<XNode> resultChildren = resultMapNode.getChildren();
      for (XNode resultChild : resultChildren) {
        if ("constructor".equals(resultChild.getName())) {
          processConstructorElement(resultChild);
        } else if ("discriminator".equals(resultChild.getName())) {
          processDiscriminatorElement(resultChild);
        } else {
          ArrayList<ResultFlag> flags = new ArrayList<ResultFlag>();
          if ("id".equals(resultChild.getName())) {
            flags.add(ResultFlag.ID);
          }
          buildResultMappingFromContext(resultChild, flags);
        }
      }
      sequentialBuilder.resultMapEnd();
  }

  private void processChildrenAsResultMap(XNode resultChild) throws Exception {
    List<String> acceptedResultMapElements = Arrays.asList(new String[]{"association","collection","case"});
    for (XNode arg : resultChild.getChildren()) {
      if (acceptedResultMapElements.contains(resultChild.getName()) && arg.getChildren().size() > 0) {
        resultMapElement(arg);
      }
    }
  }

  private void processConstructorElement(XNode resultChild) throws Exception {
    List<XNode> argChildren = resultChild.getChildren();
    for (XNode argChild : argChildren) {
      ArrayList<ResultFlag> flags = new ArrayList<ResultFlag>();
      flags.add(ResultFlag.CONSTRUCTOR);
      if ("idArg".equals(argChild.getName())) {
        flags.add(ResultFlag.ID);
      }
      buildResultMappingFromContext(argChild, flags);
    }
  }

  private void processDiscriminatorElement(XNode context) throws Exception {
    String column = context.getStringAttribute("column");
    String javaType = context.getStringAttribute("javaType");
    String jdbcType = context.getStringAttribute("jdbcType");
    String typeHandler = context.getStringAttribute("typeHandler");
    Class javaTypeClass = resolveClass(javaType);
    Class typeHandlerClass = resolveClass(typeHandler);
    JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);
    sequentialBuilder.resultMapDiscriminatorStart(column, javaTypeClass, jdbcTypeEnum, typeHandlerClass);
    for (XNode caseChild : context.getChildren()) {
      processChildrenAsResultMap(caseChild);
      String value = caseChild.getStringAttribute("value");
      String resultMap = caseChild.getStringAttribute("resultMap");
      sequentialBuilder.resultMapDiscriminatorCase(value, resultMap);
    }
    sequentialBuilder.resultMapDiscriminatorEnd();
  }

  private void sqlElement(List<XNode> list) throws Exception {
    for (XNode context : list) {
      String id = context.getStringAttribute("id");
      sqlFragments.put(id, context);
    }
  }

  private void buildStatementFromContext(List<XNode> list) {
    for (XNode context : list) {
      final XMLStatementBuilder statementParser = new XMLStatementBuilder(configuration, sequentialBuilder, this);
      statementParser.parseStatementNode(context);
    }
  }

  private void buildResultMappingFromContext(XNode context, ArrayList<ResultFlag> flags) {
    String property = context.getStringAttribute("property");
    String column = context.getStringAttribute("column");
    String javaType = context.getStringAttribute("javaType");
    String jdbcType = context.getStringAttribute("jdbcType");
    String nestedSelect = context.getStringAttribute("select");
    String nestedResultMap = context.getStringAttribute("resultMap");
    String typeHandler = context.getStringAttribute("typeHandler");
    Class javaTypeClass = resolveClass(javaType);
    Class typeHandlerClass = resolveClass(typeHandler);
    JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);
    sequentialBuilder.resultMapping(property, column, javaTypeClass, jdbcTypeEnum, nestedSelect, nestedResultMap, typeHandlerClass, flags);
  }

  private void bindMapperForNamespace() {
    String namespace = sequentialBuilder.getNamespace();
    if (namespace != null) {
      Class boundType = null;
      try {
        boundType = Class.forName(namespace);
      } catch (ClassNotFoundException e) {
        //ignore, bound type is not required
      }
      if (boundType != null) {
        if (!configuration.hasMapper(boundType)) {
          configuration.addMapper(boundType);
        }
      }
    }
  }

}
