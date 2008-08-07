package org.apache.ibatis.mapping;

import java.util.List;

public interface SqlSource {

  String getSql(Object parameterObject);

  List<ParameterMapping> getParameterMappings(Object parameterObject);

}
