CREATE DATABASE BankDB;

USE BankDB;


CREATE TABLE BankAccount (
    accountNumber INT PRIMARY KEY NOT NULL,
    accountHolderName VARCHAR(100) NOT NULL,
    balance DOUBLE NOT NULL DEFAULT 1000.0
);


CREATE TABLE Transaction (
    transactionId INT AUTO_INCREMENT PRIMARY KEY,
    accountNumber INT,
    transactionType VARCHAR(10), 
    amount DOUBLE,
    transactionDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (accountNumber) REFERENCES BankAccount(accountNumber)
);

ALTER TABLE Transaction MODIFY COLUMN transactionType VARCHAR(20);

