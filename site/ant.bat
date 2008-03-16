
call setenv.bat

"E:\Program Files\Java\jdk1.5.0_07\bin\java" -classpath %BUILD_CP% org.apache.tools.ant.Main -buildfile build.xml %1
