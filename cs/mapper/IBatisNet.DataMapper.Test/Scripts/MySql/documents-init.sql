use iBatisNet;

drop table if exists DOCUMENTS;

create table DOCUMENTS
(
   DOCUMENT_ID                    int                            not null,
   DOCUMENT_TITLE                  varchar(32),
   DOCUMENT_TYPE                  varchar(32),
   DOCUMENT_PAGENUMBER				int,
   DOCUMENT_CITY					varchar(32),
   primary key (DOCUMENT_ID)
) TYPE=INNODB;

INSERT INTO DOCUMENTS VALUES (1, 'The World of Null-A', 'Book', 55, null);
INSERT INTO DOCUMENTS VALUES (2, 'Le Progres de Lyon', 'Newspaper', null , 'Lyon');
INSERT INTO DOCUMENTS VALUES (3, 'Lord of the Rings', 'Book', 3587, null);
INSERT INTO DOCUMENTS VALUES (4, 'Le Canard enchaine', 'Tabloid', null , 'Paris');
INSERT INTO DOCUMENTS VALUES (5, 'Le Monde', 'Broadsheet', null , 'Paris');
INSERT INTO DOCUMENTS VALUES (6, 'Foundation', 'Monograph', 557, null);
