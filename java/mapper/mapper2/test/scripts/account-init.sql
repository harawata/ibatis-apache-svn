-- HSQL DATABASE

-- Dropping Tables

DROP TABLE ACCOUNT;

-- Creating Tables

CREATE TABLE ACCOUNT (
    ACC_ID             INTEGER NOT NULL,
    ACC_FIRST_NAME     VARCHAR NOT NULL,
    ACC_LAST_NAME      VARCHAR NOT NULL,
    ACC_EMAIL          VARCHAR,
    PRIMARY KEY (ACC_ID)
);

-- Creating Test Data

INSERT INTO ACCOUNT VALUES(1,'Clinton', 'Begin', 'clinton.begin@ibatis.com');
INSERT INTO ACCOUNT VALUES(2,'Jim', 'Smith', 'jim.smith@somewhere.com');
INSERT INTO ACCOUNT VALUES(3,'Elizabeth', 'Jones', null);
INSERT INTO ACCOUNT VALUES(4,'Bob', 'Jackson', 'bob.jackson@somewhere.com');
INSERT INTO ACCOUNT VALUES(5,'Amanda', 'Goodman', null);



