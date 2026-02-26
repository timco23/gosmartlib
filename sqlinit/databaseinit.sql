create database if not exists LibraryDB;
use LibraryDB;

CREATE DATABASE IF NOT EXISTS LibraryDB;
USE LibraryDB;

CREATE TABLE IF NOT EXISTS School (
    Id INT PRIMARY KEY AUTO_INCREMENT,
    Name VARCHAR(255) NOT NULL,
    Address VARCHAR(500) NOT NULL,
    LendingPeriodDays INT NOT NULL,
    ExtendPeriodDays INT NOT NULL,
    MaxExtends INT NOT NULL,
    MaxLendsStudent INT NOT NULL,
    MaxLendTeacher INT NOT NULL,
    LenderNotificationPeriod INT NOT NULL,
    LenderLateNotificationPeriod INT NOT NULL,
    LibrarianNotificationNotReturned INT NOT NULL,
    LibrarianRequestNotification INT NOT NULL,
    ExternalVisibility BOOLEAN NOT NULL,
    ExternalLending BOOLEAN NOT NULL,
    Reviews BOOLEAN NOT NULL,
    ReviewModeration BOOLEAN NOT NULL,
    CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UpdatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);