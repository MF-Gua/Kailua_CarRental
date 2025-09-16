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

