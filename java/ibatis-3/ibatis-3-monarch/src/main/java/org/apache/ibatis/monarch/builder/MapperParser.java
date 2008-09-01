package org.apache.ibatis.monarch.builder;

import org.apache.ibatis.xml.NodeletParser;

import java.io.Reader;

public class MapperParser extends BaseParser {

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



}
