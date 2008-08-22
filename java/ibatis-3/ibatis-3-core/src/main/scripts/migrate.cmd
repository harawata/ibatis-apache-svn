@ECHO off

set IBATIS_LIB=/home/clinton/Development/ibatis-3/ibatis-3-core/target/classes

java -cp %IBATIS_LIB% org.apache.ibatis.migration.Migrator %*
