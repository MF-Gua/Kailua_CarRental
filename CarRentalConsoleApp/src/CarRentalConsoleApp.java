
import java.sql.*;
import java.sql.Date;
import java.util.*;
        import java.io.*;

public class CarRentalConsoleApp {
    private final Scanner sc = new Scanner(System.in);
    private Connection conn;

    public static void main(String[] args) {
        CarRentalConsoleApp app = new CarRentalConsoleApp();
        try {
            app.start();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void start() throws Exception {
        System.out.println("Car Rental Console App");
        connectPrompt();

        mainMenuLoop();

        closeConnection();
        System.out.println("Goodbye.");
    }

    //Input det, der står i parantes i konsollen
    private void connectPrompt() {
        System.out.print("DB Host (localhost): ");
        String host = sc.nextLine().trim();
        if (host.isEmpty()) host = "localhost";

        System.out.print("DB Port (3306): ");
        String portStr = sc.nextLine().trim();
        if (portStr.isEmpty()) portStr = "3306";

        System.out.print("Database name (kailua_rental): ");
        String db = sc.nextLine().trim();
        if (db.isEmpty()) db = "kailua_rental";

        System.out.print("DB Username (root): ");
        String user = sc.nextLine().trim();
        if (user.isEmpty()) user = "root";

        //Jeg tiltror dig koden til min root user på WorkBench
        System.out.print("DB Password (gormerdengustne123): ");
        String password = sc.nextLine();


        //MySql JDBC driver?
        String url = "jdbc:mysql://" + host + ":" + portStr + "/" + db
                + "?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true";

        try {
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to " + db + " at " + host + ":" + portStr);
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
            System.out.println("Try again.");
            connectPrompt();
        }
    }

    private void mainMenuLoop() {
        while (true) {
            System.out.println("\nMAIN MENU");
            System.out.println("1) Manage Cars");
            System.out.println("2) Manage Customers");
            System.out.println("3) Manage Rentals (ActiveContracts)");
            //Hvis du vælger 4 og trykker Enter, køres mit masterscript (init.sql) og databasen resettes.
            System.out.println("4) Init/Reset DB from SQL files (runs init.sql)");
            //
            System.out.println("5) Run SQL file (custom)");
            System.out.println("0) Exit");
            System.out.print("Choose: ");
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1": carsMenu(); break;
                case "2": customersMenu(); break;
                case "3": rentalsMenu(); break;
                case "4":
                    runSqlFileInteractive("sql/init.sql");
                    break;
                case "5":
                    System.out.print("Enter SQL file path: ");
                    String file = sc.nextLine().trim();
                    runSqlFileInteractive(file);
                    break;
                case "0": return;
                default: System.out.println("Unknown option"); break;
            }
        }
    }

    // Cars
    private void carsMenu() {
        while (true) {
            System.out.println("\nCARS");
            System.out.println("1) Insert car");
            System.out.println("2) Update car");
            System.out.println("3) Delete car");
            System.out.println("4) List all cars");
            System.out.println("5) Search cars by group/brand/plate");
            System.out.println("0) Back");
            System.out.print("Choose: ");
            String c = sc.nextLine().trim();
            try {
                switch (c) {
                    case "1": insertCar(); break;
                    case "2": updateCar(); break;
                    case "3": deleteCar(); break;
                    case "4": listAllCars(); break;
                    case "5": searchCars(); break;
                    case "0": return;
                    default: System.out.println("Unknown option"); break;
                }
            } catch (SQLException e) {
                System.err.println("SQL error: " + e.getMessage());
            }
        }
    }

    private void insertCar() throws SQLException {
        System.out.print("PlateNumber: ");
        String plate = sc.nextLine().trim();
        System.out.print("Group (Luxury/Family/Sport): ");
        String group = sc.nextLine().trim();
        System.out.print("Brand: ");
        String brand = sc.nextLine().trim();
        System.out.print("Model: ");
        String model = sc.nextLine().trim();
        System.out.print("FuelType: ");
        String fuel = sc.nextLine().trim();
        System.out.print("RegistrationDate (DD-MM-YYYY): ");
        String reg = sc.nextLine().trim();
        System.out.print("Mileage (int km): ");
        int mileage = parseIntOr(0, sc.nextLine().trim());

        String sql = "INSERT INTO Cars (PlateNumber, CarGroup, Brand, Model, FuelType, RegistrationDate, Mileage) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, plate);
            ps.setString(2, group);
            ps.setString(3, brand);
            ps.setString(4, model);
            ps.setString(5, fuel);
            if (reg.isEmpty()) ps.setNull(6, Types.DATE);
            else ps.setDate(6, Date.valueOf(reg));
            ps.setInt(7, mileage);
            ps.executeUpdate();
            System.out.println("Car inserted.");
        }
    }

    private void updateCar() throws SQLException {
        System.out.print("PlateNumber of car to update: ");
        String plate = sc.nextLine().trim();
        if (!exists("Cars", "PlateNumber", plate)) {
            System.out.println("No such car.");
            return;
        }
        System.out.print("New Group (leave blank to keep): ");
        String group = sc.nextLine().trim();
        System.out.print("New Brand (leave blank to keep): ");
        String brand = sc.nextLine().trim();
        System.out.print("New Model (leave blank to keep): ");
        String model = sc.nextLine().trim();
        System.out.print("New FuelType (leave blank to keep): ");
        String fuel = sc.nextLine().trim();
        System.out.print("New Mileage (leave blank to keep): ");
        String mileageStr = sc.nextLine().trim();

        String sql = "UPDATE Cars SET CarGroup = COALESCE(NULLIF(?,''), CarGroup)," +
                " Brand = COALESCE(NULLIF(?,''), Brand)," +
                " Model = COALESCE(NULLIF(?,''), Model)," +
                " FuelType = COALESCE(NULLIF(?,''), FuelType)," +
                " Mileage = COALESCE(NULLIF(?,''), Mileage)" +
                " WHERE PlateNumber = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, group);
            ps.setString(2, brand);
            ps.setString(3, model);
            ps.setString(4, fuel);
            if (mileageStr.isEmpty()) ps.setString(5, "");
            else ps.setString(5, mileageStr);
            ps.setString(6, plate);
            int rows = ps.executeUpdate();
            System.out.println("Updated rows: " + rows);
        }
    }

    private void deleteCar() throws SQLException {
        System.out.print("PlateNumber to delete: ");
        String plate = sc.nextLine().trim();
        String sql = "DELETE FROM Cars WHERE PlateNumber = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, plate);
            int r = ps.executeUpdate();
            System.out.println("Deleted rows: " + r);
        } catch (SQLException e) {
            System.err.println("Delete failed: " + e.getMessage());
        }
    }

    private void listAllCars() throws SQLException {
        String sql = "SELECT PlateNumber, CarGroup, Brand, Model, FuelType, RegistrationDate, Mileage FROM Cars";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            System.out.println("Plate | Group | Brand | Model | Fuel | RegDate | Mileage");
            while (rs.next()) {
                System.out.printf("%s | %s | %s | %s | %s | %s | %d%n",
                        rs.getString("PlateNumber"),
                        safe(rs.getString("CarGroup")),
                        safe(rs.getString("Brand")),
                        safe(rs.getString("Model")),
                        safe(rs.getString("FuelType")),
                        rs.getDate("RegistrationDate"),
                        rs.getInt("Mileage"));
            }
        }
    }

    private void searchCars() throws SQLException {
        System.out.print("Search by Group (leave blank to skip): ");
        String group = sc.nextLine().trim();
        System.out.print("Search by Brand (leave blank to skip): ");
        String brand = sc.nextLine().trim();
        System.out.print("Search by Plate (leave blank to skip): ");
        String plate = sc.nextLine().trim();

        StringBuilder sb = new StringBuilder("SELECT * FROM Cars WHERE 1=1");
        List<String> params = new ArrayList<>();
        if (!group.isEmpty()) { sb.append(" AND CarGroup LIKE ?"); params.add("%"+group+"%"); }
        if (!brand.isEmpty()) { sb.append(" AND Brand LIKE ?"); params.add("%"+brand+"%"); }
        if (!plate.isEmpty()) { sb.append(" AND PlateNumber LIKE ?"); params.add("%"+plate+"%"); }

        try (PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setString(i+1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("Results:");
                while (rs.next()) {
                    System.out.printf("%s | %s | %s | %s | %s | %s | %d%n",
                            rs.getString("PlateNumber"),
                            safe(rs.getString("CarGroup")),
                            safe(rs.getString("Brand")),
                            safe(rs.getString("Model")),
                            safe(rs.getString("FuelType")),
                            rs.getDate("RegistrationDate"),
                            rs.getInt("Mileage"));
                }
            }
        }
    }

    //Customers
    private void customersMenu() {
        while (true) {
            System.out.println("\n--- CUSTOMERS ---");
            System.out.println("1) Insert customer");
            System.out.println("2) Update customer");
            System.out.println("3) Delete customer");
            System.out.println("4) List all customers");
            System.out.println("5) Search customers by name/license/city");
            System.out.println("0) Back");
            System.out.print("Choose: ");
            String c = sc.nextLine().trim();
            try {
                switch (c) {
                    case "1": insertCustomer(); break;
                    case "2": updateCustomer(); break;
                    case "3": deleteCustomer(); break;
                    case "4": listAllCustomers(); break;
                    case "5": searchCustomers(); break;
                    case "0": return;
                    default: System.out.println("Unknown option"); break;
                }
            } catch (SQLException e) {
                System.err.println("SQL error: " + e.getMessage());
            }
        }
    }

    private void insertCustomer() throws SQLException {
        System.out.print("DriverLicenseNumber: ");
        String dl = sc.nextLine().trim();
        System.out.print("Name: ");
        String name = sc.nextLine().trim();
        System.out.print("Address: ");
        String address = sc.nextLine().trim();
        System.out.print("ZipCode: ");
        String zip = sc.nextLine().trim();
        System.out.print("City: ");
        String city = sc.nextLine().trim();
        System.out.print("PhoneNumber: ");
        String phone = sc.nextLine().trim();
        System.out.print("Email: ");
        String email = sc.nextLine().trim();
        System.out.print("LicenseIssueDate (DD-MM-YYYY): ");
        String issue = sc.nextLine().trim();

        String sql = "INSERT INTO Customers (DriverLicenseNumber, Name, Address, ZipCode, City, PhoneNumber, Email, LicenseIssueDate) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dl);
            ps.setString(2, name);
            ps.setString(3, address);
            ps.setString(4, zip);
            ps.setString(5, city);
            ps.setString(6, phone);
            ps.setString(7, email.isEmpty() ? null : email);
            if (issue.isEmpty()) ps.setNull(8, Types.DATE);
            else ps.setDate(8, Date.valueOf(issue));
            ps.executeUpdate();
            System.out.println("Customer inserted.");
        }
    }

    private void updateCustomer() throws SQLException {
        System.out.print("DriverLicenseNumber to update: ");
        String dl = sc.nextLine().trim();
        if (!exists("Customers", "DriverLicenseNumber", dl)) {
            System.out.println("No such customer.");
            return;
        }
        System.out.print("New Name (leave blank to keep): ");
        String name = sc.nextLine().trim();
        System.out.print("New Address (leave blank to keep): ");
        String addr = sc.nextLine().trim();
        System.out.print("New ZipCode (leave blank to keep): ");
        String zip = sc.nextLine().trim();
        System.out.print("New City (leave blank to keep): ");
        String city = sc.nextLine().trim();
        System.out.print("New PhoneNumber (leave blank to keep): ");
        String phone = sc.nextLine().trim();
        System.out.print("New Email (leave blank to keep): ");
        String email = sc.nextLine().trim();

        String sql = "UPDATE Customers SET Name = COALESCE(NULLIF(?,''), Name)," +
                " Address = COALESCE(NULLIF(?,''), Address)," +
                " ZipCode = COALESCE(NULLIF(?,''), ZipCode)," +
                " City = COALESCE(NULLIF(?,''), City)," +
                " PhoneNumber = COALESCE(NULLIF(?,''), PhoneNumber)," +
                " Email = COALESCE(NULLIF(?,''), Email) WHERE DriverLicenseNumber = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, addr);
            ps.setString(3, zip);
            ps.setString(4, city);
            ps.setString(5, phone);
            ps.setString(6, email);
            ps.setString(7, dl);
            int rows = ps.executeUpdate();
            System.out.println("Updated rows: " + rows);
        }
    }

    private void deleteCustomer() throws SQLException {
        System.out.print("DriverLicenseNumber to delete: ");
        String dl = sc.nextLine().trim();
        String sql = "DELETE FROM Customers WHERE DriverLicenseNumber = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dl);
            int r = ps.executeUpdate();
            System.out.println("Deleted rows: " + r);
        } catch (SQLException e) {
            System.err.println("Delete failed: " + e.getMessage());
        }
    }

    private void listAllCustomers() throws SQLException {
        String sql = "SELECT DriverLicenseNumber, Name, Address, ZipCode, City, PhoneNumber, Email, LicenseIssueDate FROM Customers";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            System.out.println("DL | Name | Address | Zip | City | Phone | Email | LicenseIssueDate");
            while (rs.next()) {
                System.out.printf("%s | %s | %s | %s | %s | %s | %s | %s%n",
                        rs.getString("DriverLicenseNumber"),
                        safe(rs.getString("Name")),
                        safe(rs.getString("Address")),
                        safe(rs.getString("ZipCode")),
                        safe(rs.getString("City")),
                        safe(rs.getString("PhoneNumber")),
                        safe(rs.getString("Email")),
                        rs.getDate("LicenseIssueDate"));
            }
        }
    }

    private void searchCustomers() throws SQLException {
        System.out.print("Search by Name (leave blank to skip): ");
        String name = sc.nextLine().trim();
        System.out.print("Search by DriverLicenseNumber (leave blank to skip): ");
        String dl = sc.nextLine().trim();
        System.out.print("Search by City (leave blank to skip): ");
        String city = sc.nextLine().trim();

        StringBuilder sb = new StringBuilder("SELECT * FROM Customers WHERE 1=1");
        List<String> params = new ArrayList<>();
        if (!name.isEmpty()) { sb.append(" AND Name LIKE ?"); params.add("%"+name+"%"); }
        if (!dl.isEmpty()) { sb.append(" AND DriverLicenseNumber LIKE ?"); params.add("%"+dl+"%"); }
        if (!city.isEmpty()) { sb.append(" AND City LIKE ?"); params.add("%"+city+"%"); }

        try (PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setString(i+1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("Results:");
                while (rs.next()) {
                    System.out.printf("%s | %s | %s | %s | %s%n",
                            rs.getString("DriverLicenseNumber"),
                            safe(rs.getString("Name")),
                            safe(rs.getString("City")),
                            safe(rs.getString("PhoneNumber")),
                            rs.getDate("LicenseIssueDate"));
                }
            }
        }
    }

    // ActiveContracts
    private void rentalsMenu() {
        while (true) {
            System.out.println("\n--- RENTALS (ActiveContracts) ---");
            System.out.println("1) Create rental (insert contract)");
            System.out.println("2) Update rental");
            System.out.println("3) Delete rental");
            System.out.println("4) List all rentals");
            System.out.println("5) Search rentals by driver/license/plate/date range");
            System.out.println("0) Back");
            System.out.print("Choose: ");
            String c = sc.nextLine().trim();
            try {
                switch (c) {
                    case "1": insertRental(); break;
                    case "2": updateRental(); break;
                    case "3": deleteRental(); break;
                    case "4": listAllRentals(); break;
                    case "5": searchRentals(); break;
                    case "0": return;
                    default: System.out.println("Unknown option"); break;
                }
            } catch (SQLException e) {
                System.err.println("SQL error: " + e.getMessage());
            }
        }
    }

    private void insertRental() throws SQLException {
        System.out.print("DriverLicenseNumber: ");
        String dl = sc.nextLine().trim();
        if (!exists("Customers", "DriverLicenseNumber", dl)) {
            System.out.println("Customer does not exist. Insert the customer first.");
            return;
        }
        System.out.print("PlateNumber: ");
        String plate = sc.nextLine().trim();
        if (!exists("Cars", "PlateNumber", plate)) {
            System.out.println("Car does not exist. Insert the car first.");
            return;
        }
        System.out.print("RentingFrom (DD-MM-YYYY HH:MM): ");
        String from = sc.nextLine().trim();
        System.out.print("RentingTo (YYYY-MM-DD HH:MM): ");
        String to = sc.nextLine().trim();
        System.out.print("MaxKm (int): ");
        int maxKm = parseIntOr(0, sc.nextLine().trim());
        System.out.print("StartMileage (int): ");
        int startMileage = parseIntOr(0, sc.nextLine().trim());

        String sql = "INSERT INTO ActiveContracts (RentingFrom, RentingTo, MaxKm, StartMileage, DriverLicenseNumber, PlateNumber) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(from));
            ps.setTimestamp(2, Timestamp.valueOf(to));
            ps.setInt(3, maxKm);
            ps.setInt(4, startMileage);
            ps.setString(5, dl);
            ps.setString(6, plate);
            ps.executeUpdate();
            System.out.println("Rental contract created.");
        } catch (IllegalArgumentException iae) {
            System.err.println("Bad date/time format. Use 'YYYY-MM-DD HH:MM:SS'.");
        }
    }

    private void updateRental() throws SQLException {
        System.out.print("ContractID to update: ");
        String idStr = sc.nextLine().trim();
        int id = parseIntOr(-1, idStr);
        if (id <= 0) { System.out.println("Invalid id."); return; }
        if (!exists("ActiveContracts", "ContractID", String.valueOf(id))) {
            System.out.println("No such contract.");
            return;
        }
        System.out.print("New RentingFrom (leave blank to keep): ");
        String from = sc.nextLine().trim();
        System.out.print("New RentingTo (leave blank to keep): ");
        String to = sc.nextLine().trim();
        System.out.print("New MaxKm (leave blank to keep): ");
        String maxKm = sc.nextLine().trim();
        System.out.print("New StartMileage (leave blank to keep): ");
        String startMileage = sc.nextLine().trim();

        String sql = "UPDATE ActiveContracts SET " +
                "RentingFrom = COALESCE(NULLIF(?,''), RentingFrom), " +
                "RentingTo = COALESCE(NULLIF(?,''), RentingTo), " +
                "MaxKm = COALESCE(NULLIF(?,''), MaxKm), " +
                "StartMileage = COALESCE(NULLIF(?,''), StartMileage) " +
                "WHERE ContractID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, from);
            ps.setString(2, to);
            ps.setString(3, maxKm);
            ps.setString(4, startMileage);
            ps.setInt(5, id);
            int rows = ps.executeUpdate();
            System.out.println("Updated rows: " + rows);
        }
    }

    private void deleteRental() throws SQLException {
        System.out.print("ContractID to delete: ");
        int id = parseIntOr(-1, sc.nextLine().trim());
        String sql = "DELETE FROM ActiveContracts WHERE ContractID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int r = ps.executeUpdate();
            System.out.println("Deleted rows: " + r);
        } catch (SQLException e) {
            System.err.println("Delete failed: " + e.getMessage());
        }
    }

    private void listAllRentals() throws SQLException {
        String sql = "SELECT ContractID, RentingFrom, RentingTo, MaxKm, StartMileage, DriverLicenseNumber, PlateNumber FROM ActiveContracts";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            System.out.println("ID | From | To | MaxKm | StartMileage | DL | Plate");
            while (rs.next()) {
                System.out.printf("%d | %s | %s | %d | %d | %s | %s%n",
                        rs.getInt("ContractID"),
                        rs.getTimestamp("RentingFrom"),
                        rs.getTimestamp("RentingTo"),
                        rs.getInt("MaxKm"),
                        rs.getInt("StartMileage"),
                        rs.getString("DriverLicenseNumber"),
                        rs.getString("PlateNumber"));
            }
        }
    }

    private void searchRentals() throws SQLException {
        System.out.print("DriverLicenseNumber (leave blank to skip): ");
        String dl = sc.nextLine().trim();
        System.out.print("PlateNumber (leave blank to skip): ");
        String plate = sc.nextLine().trim();
        System.out.print("From date (DD-MM-YYYY, leave blank to skip): ");
        String fromDate = sc.nextLine().trim();
        System.out.print("To date (DD-MM-YYYY, leave blank to skip): ");
        String toDate = sc.nextLine().trim();

        StringBuilder sb = new StringBuilder("SELECT * FROM ActiveContracts WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (!dl.isEmpty()) { sb.append(" AND DriverLicenseNumber LIKE ?"); params.add("%"+dl+"%"); }
        if (!plate.isEmpty()) { sb.append(" AND PlateNumber LIKE ?"); params.add("%"+plate+"%"); }
        if (!fromDate.isEmpty()) { sb.append(" AND RentingFrom >= ?"); params.add(Timestamp.valueOf(fromDate + " 00:00:00")); }
        if (!toDate.isEmpty()) { sb.append(" AND RentingTo <= ?"); params.add(Timestamp.valueOf(toDate + " 23:59:59")); }

        try (PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof String) ps.setString(i+1, (String)p);
                else if (p instanceof Timestamp) ps.setTimestamp(i+1, (Timestamp)p);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.printf("%d | %s | %s | %d | %d | %s | %s%n",
                            rs.getInt("ContractID"),
                            rs.getTimestamp("RentingFrom"),
                            rs.getTimestamp("RentingTo"),
                            rs.getInt("MaxKm"),
                            rs.getInt("StartMileage"),
                            rs.getString("DriverLicenseNumber"),
                            rs.getString("PlateNumber"));
                }
            }
        } catch (IllegalArgumentException iae) {
            System.err.println("Bad date format. Use DD-MM-YYYY");
        }
    }

    // ---------- Utilities ----------
    private boolean exists(String table, String column, String value) throws SQLException {
        String sql = "SELECT 1 FROM " + table + " WHERE " + column + " = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private int parseIntOr(int fallback, String s) {
        try { return Integer.parseInt(s); }
        catch (Exception e) { return fallback; }
    }

    private String safe(String s) { return s == null ? "" : s; }

    private void closeConnection() {
        if (conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }

    // Execute SQL file by reading it and splitting by semicolon
    private void runSqlFileInteractive(String path) {
        System.out.println("Running SQL file: " + path);
        File f = new File(path);
        if (!f.exists()) {
            System.err.println("File not found: " + path);
            return;
        }
        try {
            runSqlFile(path);
            System.out.println("SQL file executed successfully.");
        } catch (Exception e) {
            System.err.println("Error running SQL file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void runSqlFile(String path) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                // remove SQL comments that start with --
                if (line.trim().startsWith("--")) continue;
                sb.append(line).append("\n");
            }
        }
        // naive split by semicolon; keep it simple for school assignment
        String[] statements = sb.toString().split(";(\\s*\\n)|;\\s*$");
        conn.setAutoCommit(false);
        try (Statement st = conn.createStatement()) {
            for (String stmt : statements) {
                String s = stmt.trim();
                if (s.isEmpty()) continue;
                st.execute(s);
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
}