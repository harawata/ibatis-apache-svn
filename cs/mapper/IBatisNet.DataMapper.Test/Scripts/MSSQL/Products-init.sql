-- Creating Table

use [IBatisNet]

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[Products]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
BEGIN
	drop table [dbo].[Products]
END

CREATE TABLE [Products] (
	[ID] [int] NOT NULL ,
	[Name] [varchar] (64)  NULL ,
	[Category_Id] [int] NOT NULL ,
	 PRIMARY KEY  CLUSTERED 
	(
		[ID]
	)  ON [PRIMARY] ,
) ON [PRIMARY]