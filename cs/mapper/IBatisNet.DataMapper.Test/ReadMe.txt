
To pass tests for MS Sql Server
------------------------------
1/ Create the database with the script 'scripts\MSSQL\DBCreation.sql'

2/ In 'bin/IBatisNet.Test.dll.config' :
		set the database to MSSQL,
		set the providerType key to a provider :
			- 'SqlClient' to run test via native .Net provider for Sql Server.
			- 'Oledb' to run test via Oledb provider for Sql Server.
			- 'Odbc' to run test via Odbc provider for Sql Server.
3/ With the help of the DataBase-Template.config
   create a file named DataBase.config with your own value for datasource.
   (WARNInG : don't included it in the solution and don't commit it in SVN)
	