package com.ibatis.sqlmap.engine.builder.xml;

import com.ibatis.sqlmap.engine.mapping.statement.GeneralStatement;

public interface StatementProcessor {

  void processStatement(GeneralStatement statement);

}
