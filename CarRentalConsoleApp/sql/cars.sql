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

