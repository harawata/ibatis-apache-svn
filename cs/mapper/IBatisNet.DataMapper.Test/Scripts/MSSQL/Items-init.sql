-- Creating Table

use [IBatisNet]

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[Items]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
BEGIN
	drop table [dbo].[Items]
END

CREATE TABLE [Items] (
	[ID] [int] NOT NULL ,
	[Status] [varchar] (64)  NULL ,
	[Quantity] [int] NOT NULL ,
	[UnitCost] [decimal](18, 2) NULL ,
	[Product_Id] [int] NOT NULL ,
	CONSTRAINT [PK_Items] PRIMARY KEY  CLUSTERED 
	(
		[ID]
	)  ON [PRIMARY] ,
) ON [PRIMARY]