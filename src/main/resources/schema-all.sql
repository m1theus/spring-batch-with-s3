CREATE TABLE IF NOT EXISTS `people`  (
       id BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
       first_name VARCHAR(20) NOT NULL,
       last_name VARCHAR(20) NOT NULL,
       role VARCHAR(20) NOT NULL,
       age INT NOT NULL
);