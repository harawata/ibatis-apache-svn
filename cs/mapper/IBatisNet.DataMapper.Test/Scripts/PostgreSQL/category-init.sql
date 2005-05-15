\connect "IBatisNet" "IBatisNet";

DROP SEQUENCE "S_Categories";

CREATE SEQUENCE "S_Categories"
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 20;
ALTER TABLE "S_Categories" OWNER TO "IBatisNet";

DROP TABLE "Categories";

CREATE TABLE "Categories"
(
  "Category_Id" int4 NOT NULL,
  "Category_Name" varchar(32),
  "Category_Guid" varchar(36),
  CONSTRAINT "PK_Categories" PRIMARY KEY ("Category_Id")
) 
WITHOUT OIDS;
ALTER TABLE "Categories" OWNER TO "IBatisNet";
