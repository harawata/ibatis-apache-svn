-- HSQL DATABASE

-- Dropping Tables

DROP TABLE ACCOUNT;

-- Creating Tables

CREATE TABLE ACCOUNT (
    ACC_ID             INTEGER NOT NULL,
    ACC_FIRST_NAME     VARCHAR NOT NULL,
    ACC_LAST_NAME      VARCHAR NOT NULL,
    ACC_EMAIL          VARCHAR,
    ACC_AGE						 NUMERIC,
    ACC_BANNER_OPTION  VARCHAR,
    ACC_CART_OPTION    INTEGER,
    ACC_DATE_ADDED     DATE,
    PRIMARY KEY (ACC_ID)
);

-- Creating Test Data

INSERT INTO ACCOUNT VALUES(1,'Clinton', 'Begin', 'clinton.begin@ibatis.com', 1, 'Oui', 200, NOW());
INSERT INTO ACCOUNT VALUES(2,'Jim', 'Smith', 'jim.smith@somewhere.com', 2, 'Oui', 200, NOW());
INSERT INTO ACCOUNT VALUES(3,'Elizabeth', 'Jones', null, 3, 'Non', 100, NOW());
INSERT INTO ACCOUNT VALUES(4,'Bob', 'Jackson', 'bob.jackson@somewhere.com', 4, 'Non', 100, NOW());
INSERT INTO ACCOUNT VALUES(5,'&manda', 'Goodman', null, 5, 'Oui', 100, NOW());


