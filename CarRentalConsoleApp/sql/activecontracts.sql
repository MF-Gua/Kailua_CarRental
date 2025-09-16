CREATE TABLE ActiveContracts
(
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
