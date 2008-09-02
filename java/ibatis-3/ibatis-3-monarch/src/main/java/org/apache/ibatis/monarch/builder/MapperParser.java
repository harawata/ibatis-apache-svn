package org.apache.ibatis.monarch.builder;

import org.apache.ibatis.mapping.*;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.type.*;
import org.apache.ibatis.xml.*;

import java.io.Reader;
import java.util.*;

public class MapperParser extends BaseParser {

  private ResultMap.Builder resultMapBuilder;
  private List<ResultMapping> resultMappings;

  public MapperParser(Reader reader, MonarchConfiguration configuration) {
    this.reader = reader;

    this.configuration = configuration;
    this.typeAliasRegistry = configuration.getTypeAliasRegistry();
    this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();

    this.parser = new NodeletParser();
    this.parser.addNodeletHandler(this);
    this.parser.setVariables(configuration.getVariables());
    this.parser.setEntityResolver(new MapperEntityResolver());
  }

  //  <resultMap id="" type="" extends="">
  @Nodelet("/mapper/resultMap")
  public void resultMapElement(NodeletContext context) throws Exception {
    String id = context.getStringAttribute("id");
    String type = context.getStringAttribute("type");
    String extend = context.getStringAttribute("extends");

    Class typeClass = resolveClass(type);

    resultMappings = new ArrayList<ResultMapping>();
    resultMapBuilder = new ResultMap.Builder(id, typeClass, resultMappings);

    if (extend != null) {
      ResultMap resultMap = configuration.getResultMap(extend);
      if (resultMap == null) {
        throw new RuntimeException("ResultMap named in extends attribute of " + id + "does not exist or is not defined yet.");
      }
      resultMappings.addAll(resultMap.getResultMappings());
    }
  }

  //  <constructor>
  //    <id column="" javaType="" jdbcType="" typeHandler=""/>
  @Nodelet("/mapper/resultMap/constructor/id")
  public void resultMapConstructorIdElement(NodeletContext context) throws Exception {
    ResultMapping.Builder builder = buildMappingFromContext(context);
    builder.flags(new ArrayList<ResultFlag>(){{
      add(ResultFlag.CONSTRUCTOR);
      add(ResultFlag.ID);
    }});
    resultMappings.add(builder.build());
  }

  //  <constructor>
  //    <result column="" javaType="" jdbcType="" typeHandler=""/>
  @Nodelet("/mapper/resultMap/constructor/result")
  public void resultMapConstructorResultElement(NodeletContext context) throws Exception {
    ResultMapping.Builder builder = buildMappingFromContext(context);
    builder.flags(new ArrayList<ResultFlag>(){{
      add(ResultFlag.CONSTRUCTOR);
    }});
    resultMappings.add(builder.build());
  }

  //  <id property="" column="" javaType="" jdbcType="" typeHandler=""/>
  @Nodelet("/mapper/resultMap/id")
  public void resultMapIdElement(NodeletContext context) throws Exception {
    ResultMapping.Builder builder = buildMappingFromContext(context);
    builder.flags(new ArrayList<ResultFlag>(){{
      add(ResultFlag.ID);
    }});
    resultMappings.add(builder.build());
  }

  //  <result property="" column="" javaType="" jdbcType="" typeHandler=""/>
  @Nodelet("/mapper/resultMap/result")
  public void resultMapResultElement(NodeletContext context) throws Exception {
    ResultMapping.Builder builder = buildMappingFromContext(context);
    resultMappings.add(builder.build());
  }

  //  <collection property="" column="" javaType="" select="" resultMap=""/>
  @Nodelet("/mapper/resultMap/result")
  public void resultMapCollectionElement(NodeletContext context) throws Exception {
    ResultMapping.Builder builder = buildMappingFromContext(context);
    resultMappings.add(builder.build());
  }

  //  </resultMap>
  @Nodelet("/mapper/resultMap/end()")
  public void resultMapClosingElement(NodeletContext context) throws Exception {
    configuration.addResultMap(resultMapBuilder.build());
  }

  private ResultMapping.Builder buildMappingFromContext(NodeletContext context) {
    String property = context.getStringAttribute("property");
    String column = context.getStringAttribute("column");
    String javaType = context.getStringAttribute("javaType");
    String jdbcType = context.getStringAttribute("jdbcType");
    String nestedSelect = context.getStringAttribute("select");
    String nestedResultMap = context.getStringAttribute("resultMap");

    Class resultType = resultMapBuilder.type();
    Class javaTypeClass = resolveResultJavaType(resultType, property, javaType);
    TypeHandler typeHandlerInstance = resolveResultTypeHandler(context, resultType);
    JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);

    ResultMapping.Builder builder = new ResultMapping.Builder(property, column, typeHandlerInstance);
    builder.javaType(javaTypeClass);
    builder.jdbcType(jdbcTypeEnum);
    builder.nestedQueryId(nestedSelect);
    builder.nestedResultMapId(nestedResultMap);

    return builder;
  }

  private Class resolveResultJavaType(Class resultType, String property, String javaType) {
    Class javaTypeClass = resolveClass(javaType);
    if (javaTypeClass == null) {
      MetaClass metaResultType = MetaClass.forClass(resultType);
      javaTypeClass = metaResultType.getSetterType(property);
    }
    if (javaTypeClass == null) {
      throw new RuntimeException("Could not determine javaType for result.  Specify property or javaType attribute.");
    }
    return javaTypeClass;
  }

  private TypeHandler resolveResultTypeHandler(NodeletContext context, Class resultType) {
    String property = context.getStringAttribute("property");
    String javaType = context.getStringAttribute("javaType");
    String jdbcType = context.getStringAttribute("jdbcType");
    String typeHandler = context.getStringAttribute("typeHandler");
    JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);
    Class javaTypeClass = resolveClass(javaType);
    TypeHandler typeHandlerInstance = (TypeHandler) resolveInstance(typeHandler);
    if (typeHandler == null) {
      if (javaTypeClass == null) {
        if (property != null) {
          Class propertyType = resolveResultJavaType(resultType,property,javaType);
          typeHandlerInstance = typeHandlerRegistry.getTypeHandler(propertyType, jdbcTypeEnum);
        }
      } else {
        typeHandlerInstance = typeHandlerRegistry.getTypeHandler(javaTypeClass, jdbcTypeEnum);
      }
    }
    if (typeHandlerInstance == null) {
      throw new RuntimeException("Could not determine typehandler for result.  Specify property, javaType or typeHandler attribute.");
    }
    return typeHandlerInstance;
  }

}
