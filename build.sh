BUILD_CP=$CLASSPATH:$JAVA_HOME/lib/tools.jar:lib/ant-1.5.1.jar:.
$JAVA_HOME/bin/java -classpath $BUILD_CP org.apache.tools.ant.Main -buildfile build.xml $1
