-- Creating Table

use [IBatisNet]

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[Others]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
BEGIN
	drop table [dbo].[Others]
END

CREATE TABLE [dbo].[Others] (
	[Other_Int] [int]  NULL ,
	[Other_Long] [BigInt] NULL,
	[Other_Bit] [Bit] NOT NULL DEFAULT (0) 
) ON [PRIMARY]

-- Creating Test Data

INSERT INTO [dbo].[Others] VALUES(1, 8888888, 0);
INSERT INTO [dbo].[Others] VALUES(2, 9999999999, 1);