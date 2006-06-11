@REM ---------------------------------------------------------
@REM -- This batch file sets the CLASSPATH environment variable
@REM -- for use with Derby products in embedded mode
@REM --
@REM -- To use this script from other locations, change the 
@REM -- value assigned to DERBY_INSTALL to be an absolute path 
@REM -- (set DERBY_INSTALL=C:\derby) instead of the current relative path
@REM --
@REM -- This file for use on Windows systems
@REM -- 
@REM ---------------------------------------------------------

set DERBY_INSTALL=..

set DCP=%DERBY_INSTALL%\devlib\derby.jar;%DERBY_INSTALL%\devlib\derbytools.jar
