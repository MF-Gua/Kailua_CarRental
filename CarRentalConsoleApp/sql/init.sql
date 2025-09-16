SET FOREIGN_KEY_CHECKS=0;

DROP DATABASE IF EXISTS kailua_rental;
CREATE DATABASE kailua_rental;
USE kailua_rental;

DROP TABLE IF EXISTS Cars;
CREATE TABLE Cars (
    PlateNumber VARCHAR(15) PRIMARY KEY,
    CarGroup VARCHAR(50),
    Brand VARCHAR(50),
    Model VARCHAR(50),
    FuelType VARCHAR(20),
    RegistrationDate DATE,
    Mileage INT
);

INSERT INTO Cars VALUES
('AB123CD', 'Luxury', 'BMW', 'X5', 'Diesel', '2020-05-10', 45000),
('XY987ZT', 'Family', 'Toyota', 'Corolla', 'Petrol', '2021-03-15', 30000),
('SP555GT', 'Sport', 'Porsche', '911', 'Petrol', '2019-08-20', 60000);




CREATE TABLE Customers (
    DriverLicenseNumber VARCHAR(20) PRIMARY KEY,
    CName VARCHAR(50),
    CAddress VARCHAR(250),
    CZipcode INTEGER(10),
    City VARCHAR(50),
    CPhone VARCHAR(20),
    C_Email VARCHAR(100),
    LicenseIssueDate DATE
);

INSERT INTO Customers VALUES
('DL1001', 'Alice Johnson', '123 Main St', '12345', 'New York', '5551234', 'alice@example.com', '2018-02-12'),
('DL1002', 'Bob Smith', '45 River Rd', '67890', 'Chicago', '5555678', 'bob@example.com', '2019-07-19'),
('DL1003', 'Charlie Brown', '78 Lake Ave', '54321', 'Boston', '5559876', 'charlie@example.com', '2020-09-23');



CREATE TABLE ActiveContracts (
    ContractID          INT PRIMARY KEY AUTO_INCREMENT,
    RentingFrom         DATETIME NOT NULL,
    RentingTo           DATETIME NOT NULL,
    MaxKm               INT,
    StartMileage        INT,
    DriverLicenseNumber VARCHAR(20),
    PlateNumber         VARCHAR(15),

    CONSTRAINT fk_customers
        FOREIGN KEY (DriverLicenseNumber) REFERENCES Customers (DriverLicenseNumber)
            ON UPDATE CASCADE ON DELETE CASCADE,

    CONSTRAINT fk_cars
        FOREIGN KEY (PlateNumber) REFERENCES Cars (Platenumber)
            ON UPDATE CASCADE ON DELETE CASCADE
);

INSERT INTO ActiveContracts (RentingFrom, RentingTo, MaxKm, StartMileage, DriverLicenseNumber, PlateNumber)
VALUES
('2025-09-01 10:00:00', '2025-09-05 10:00:00', 500, 45000, 'DL1001', 'AB123CD'),
('2025-09-10 09:00:00', '2025-09-15 12:00:00', 700, 30000, 'DL1002', 'XY987ZT'),
('2025-09-20 08:30:00', '2025-09-22 20:00:00', 400, 60000, 'DL1003', 'SP555GT');

SET FOREIGN_KEY_CHECKS=1;