
use IBatisNet;

drop table if exists Others;

create table Others
(
   Other_Int                       int,
   Other_Long                     bigint,
   Other_Bit					            bit not null default 0
) TYPE=INNODB;

INSERT INTO Others VALUES(1, 8888888, 0);
INSERT INTO Others VALUES(2, 9999999999, 1);