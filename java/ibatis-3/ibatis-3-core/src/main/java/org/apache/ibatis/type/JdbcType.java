package org.apache.ibatis.type;

import java.sql.Types;

public enum JdbcType {

  BIT(Types.BIT),
  TINYINT(Types.TINYINT),
  SMALLINT(Types.SMALLINT),
  INTEGER(Types.INTEGER),
  BIGINT(Types.BIGINT),
  FLOAT(Types.FLOAT),
  REAL(Types.REAL),
  DOUBLE(Types.DOUBLE),
  NUMERIC(Types.NUMERIC),
  DECIMAL(Types.DECIMAL),
  CHAR(Types.CHAR),
  VARCHAR(Types.VARCHAR),
  LONGVARCHAR(Types.LONGVARCHAR),
  DATE(Types.DATE),
  TIME(Types.TIME),
  TIMESTAMP(Types.TIMESTAMP),
  BINARY(Types.BINARY),
  VARBINARY(Types.VARBINARY),
  LONGVARBINARY(Types.LONGVARBINARY),
  NULL(Types.NULL),
  OTHER(Types.OTHER),
  BLOB(Types.BLOB),
  CLOB(Types.CLOB),
  BOOLEAN(Types.BOOLEAN),
  CURSOR(-10), // Oracle
  UNDEFINED(Integer.MIN_VALUE + 1000);

  // ----------------
  // -- Unsupported--
  // ----------------
  //JAVA_OBJECT(Types.JAVA_OBJECT),
  //DISTINCT(Types.DISTINCT),
  //STRUCT(Types.STRUCT),
  //ARRAY(Types.ARRAY),
  //REF(Types.REF),
  //DATALINK(Types.DATALINK),

  public final int TYPE_CODE;

  JdbcType(int code) {
    this.TYPE_CODE = code;
  }

}
