echo off

set BUILD_CP=%JAVA_HOME%\lib\tools.jar;..\devlib\ant.jar;..\devlib\optional.jar;..\devlib\junit.jar;..\lib\optional\xml\xercesImpl-2-4-0.jar;..\lib\optional\xml\xmlParserAPIs-2-4-0.jar

%JAVA_HOME%\bin\java -classpath %BUILD_CP% org.apache.tools.ant.Main -buildfile build.xml clean

set BUILD_CP=

pause