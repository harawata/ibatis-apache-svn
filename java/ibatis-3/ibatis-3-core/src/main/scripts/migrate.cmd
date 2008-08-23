@ECHO off

set CURDIR=%~dp0
set JARS=%CURDIR%lib\
for /F %%a in ('dir %JARS% /a /b /-p /o') do set MYCP=%JARS%%%a

java -cp %MYCP% org.apache.ibatis.migration.Migrator %*
