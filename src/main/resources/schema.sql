DROP TABLE IF EXISTS accounts;

CREATE TABLE accounts (
  id INT AUTO_INCREMENT  PRIMARY KEY,
  owner_name VARCHAR(250) NOT NULL,
  balance DOUBLE DEFAULT 0
);