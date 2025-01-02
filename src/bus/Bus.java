package bus;

import utils.SupabaseCon;
import utils.TerminalCommand;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Bus {
    private String busId;
    private String busBrand;
    private String busType;
    private int busCapacity;

    public Bus(){}

    private String getBusId() {
        return busId;
    }

    private void setBusId(String busId) {
        this.busId = busId;
    }

    private String getBusBrand() {
        return busBrand;
    }

    private void setBusBrand(String busBrand) {
        this.busBrand = busBrand;
    }

    private String getBusType() {
        return busType;
    }

    private void setBusType(String busType) {
        this.busType = busType;
    }

    private int getBusCapacity() {
        return busCapacity;
    }

    private void setBusCapacity(int busCapacity) {
        this.busCapacity = busCapacity;
    }

    public void menu(){
        Scanner scanner = new Scanner(System.in);
        TerminalCommand cmd = new TerminalCommand();
        boolean status = true;

        do {
            System.out.println("===== Bus Menu =====");
            System.out.println("1. View Bus Model");
            System.out.println("2. Add a New Bus Model");
            System.out.println("3. Remove a Bus Model");
            System.out.println("4. Generate report");
            System.out.println("0. Exit");
            System.out.print("\nEnter your choice: ");

            if (!scanner.hasNextInt()) {
                System.out.println("Invalid input. Please enter a valid number.");
                scanner.nextLine();
                continue;
            }

            int choice = scanner.nextInt();
            scanner.nextLine();
            cmd.Clear();

            switch (choice) {
                case 1:
                    viewBusModels();
                    break;

                case 2:
                    addBusModel(scanner);
                    break;

                case 3:
                    deleteBusModel(scanner);
                    break;

                case 4:
                    generateReport();
                    break;

                case 0:
                    status = false;
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
                    break;
            }
            cmd.waitForEnter();
            cmd.Clear();
        } while (status);
    }

    //Economy
    public void generateEconomyLayout(int capacity, String[] seats) {
        for (int i = 1; i <= capacity; i++) {
            if (List.of(seats).contains(String.valueOf(i))) {
                System.out.print("[X] ");
            } else {
                System.out.print("[" + i + "] ");
            }

            if (i % 4 == 0) {
                System.out.println();
            } else if (i % 2 == 0) {
                System.out.print("   ");
            }
        }
    }

    // Executive
    public void generateExecutiveLayout(int capacity, String[] seats) {
        for (int i = 1; i <= capacity; i++) {
            if (List.of(seats).contains(String.valueOf(i))) {
                System.out.print("[X] ");
            } else {
                System.out.print("[" + i + "] ");
            }
            if (i % 3 == 0) {
                System.out.println();
            } else {
                System.out.print("   ");
            }
        }
    }

    // Double Decker
    public void generateDoubleDeckerLayout(int capacity, String[] seats) {
        int lowerDeckCapacity = Math.min(6, capacity);
        int upperDeckStart = lowerDeckCapacity + 1;

        System.out.println("Lower Deck:");
        for (int i = 1; i <= lowerDeckCapacity; i++) {
            if (List.of(seats).contains(String.valueOf(i))) {
                System.out.print("[X] ");
            } else {
                System.out.print("[" + i + "] ");
            }
            if (i % 3 == 0) {
                System.out.println();
            }
        }

        System.out.println("\nUpper Deck:");
        for (int i = upperDeckStart; i <= capacity; i++) {
            if (List.of(seats).contains(String.valueOf(i))) {
                System.out.print("[X] ");
            } else {
                System.out.print("[" + i + "] ");
            }
            if ((i - upperDeckStart + 1) % 4 == 0) {
                System.out.println();
            } else if ((i - upperDeckStart + 1) % 2 == 0) {
                System.out.print("   ");
            }
        }
    }

    public String[] chooseSeats(int busId, int routeId) {
        Scanner scanner = new Scanner(System.in);

        try (Connection connection = SupabaseCon.connect()) {
            if (connection == null) {
                System.out.println("Failed to connect to the database.");
                return null;
            }

            String fetchQuery = "SELECT bm.capacity, bm.type AS bus_type, br.seats, br.price " +
                    "FROM bus_route br " +
                    "JOIN bus_model bm ON br.bus_model_id = bm.bus_id " +
                    "WHERE br.route_id = ? AND bm.bus_id = ?";

            PreparedStatement fetchStatement = connection.prepareStatement(fetchQuery);
            fetchStatement.setInt(1, routeId);
            fetchStatement.setInt(2, busId);
            ResultSet resultSet = fetchStatement.executeQuery();

            if (resultSet.next()) {
                int capacity = resultSet.getInt("capacity");
                String busType = resultSet.getString("bus_type");

                String seatsString = resultSet.getString("seats");
                String[] seats = seatsString != null ? seatsString.split(",") : new String[0];

                System.out.println("Seats Layout:");

                Bus busLayout = new Bus();
                switch (busType.toLowerCase()) {
                    case "economy":
                        busLayout.generateEconomyLayout(capacity, seats);
                        break;
                    case "executive":
                        busLayout.generateExecutiveLayout(capacity, seats);
                        break;
                    case "double decker":
                        busLayout.generateDoubleDeckerLayout(capacity, seats);
                        break;
                    default:
                        System.out.println("Unknown bus type: " + busType);
                        return null;
                }

                int selectedSeat;

                do {
                    System.out.println("\nEnter the seat number you want to book (1-" + capacity + "): ");
                    selectedSeat = scanner.nextInt();

                    if (selectedSeat < 1 || selectedSeat > capacity) {
                        System.out.println("Invalid seat number. Please try again.");
                        continue;
                    }

                    if (Arrays.asList(seats).contains(String.valueOf(selectedSeat))) {
                        System.out.println("Seat " + selectedSeat + " is already booked.");
                        continue;
                    }
                    break;
                } while (true);

                if (seatsString == null || seatsString.isEmpty()) {
                    seatsString = String.valueOf(selectedSeat);
                } else {
                    seatsString += "," + selectedSeat;
                }

                return new String[]{String.valueOf(selectedSeat), seatsString};
            } else {
                System.out.println("Bus with ID " + busId + " and route " + routeId + " not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("An error occurred while processing the seat selection.");
        }
        return null;
    }

    public void viewBusModels() {

        new TerminalCommand().Clear();

        try (Connection connection = SupabaseCon.connect()) {
            if (connection == null) {
                System.out.println("Failed to connect to the database.");
                return;
            }

            String selectQuery = "SELECT * FROM bus_model;";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(selectQuery);

            new TerminalCommand().customText("Bus Models");
            System.out.printf("%-15s %-25s %-15s %-10s%n", "Bus Number", "Bus Brand", "Bus Type", "Capacity");
            System.out.println("-------------------------------------------------------------------");

            while (resultSet.next()) {
                setBusId(resultSet.getString("bus_id"));
                setBusBrand(resultSet.getString("brand"));
                setBusType(resultSet.getString("type"));
                setBusCapacity(resultSet.getInt("capacity"));

                System.out.printf("%-15s %-25s %-15s %-10d%n", getBusId(), getBusBrand(), getBusType(), getBusCapacity());
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("An error occurred while retrieving the bus models.");
        }
    }

    public void addBusModel(Scanner scanner) {

        new TerminalCommand().Clear();
        scanner.nextLine();
        new TerminalCommand().customText("Add Bus Model");

        do {
            System.out.print("Enter Bus Brand: ");
            busBrand = scanner.nextLine().trim();
            if (busBrand.isEmpty()) {
                System.out.println("Bus brand cannot be empty. Please enter a valid bus brand.");
            }
        } while (busBrand.isEmpty());

        do {
            System.out.print("Enter Capacity: ");
            while (!scanner.hasNextInt()) {
                System.out.println("Invalid input. Please enter a valid number for capacity.");
                scanner.next();
            }
            busCapacity = scanner.nextInt();
            scanner.nextLine();
            if (busCapacity <= 0) {
                System.out.println("Capacity must be a positive number. Please enter a valid capacity.");
            }
        } while (busCapacity <= 0);

        do {
            System.out.print("Enter Bus Type (e.g., Economy, Executive, Double Decker): ");
            busType = scanner.nextLine().trim();
            if (busType.isEmpty()) {
                System.out.println("Bus type cannot be empty. Please enter a valid bus type.");
            }
        } while (busType.isEmpty());


        try (Connection connection = SupabaseCon.connect()) {
            if (connection == null) {
                System.out.println("Failed to connect to the database.");
                return;
            }

            String insertQuery = "INSERT INTO bus_model (brand, capacity, type) VALUES (?, ?, ?);";
            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);

            preparedStatement.setString(1, busBrand);
            preparedStatement.setInt(2, busCapacity);
            preparedStatement.setString(3, busType);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Bus model added successfully.");
            } else {
                System.out.println("Failed to add the bus model.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("An error occurred while adding the bus model.");
        }
    }

    public void deleteBusModel(Scanner scanner) {

        viewBusModels();
        new TerminalCommand().customText("Delete Bus Model");

        System.out.println("Enter Bus Model ID to delete: ");
        int busModelId = scanner.nextInt();
        scanner.nextLine();

        try (Connection connection = SupabaseCon.connect()) {
            if (connection == null) {
                System.out.println("Failed to connect to the database.");
                return;
            }

            String deleteQuery = "DELETE FROM bus_model WHERE bus_id = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery);

            preparedStatement.setInt(1, busModelId);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Bus model deleted successfully.");
            } else {
                System.out.println("No bus model found with the provided ID.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("An error occurred while deleting the bus model.");
        }
    }

    public void generateReport() {
        new TerminalCommand().Clear();

        try (Connection connection = SupabaseCon.connect()) {
            if (connection == null) {
                System.out.println("Failed to connect to the database.");
                return;
            }

            String selectQuery = "SELECT * FROM bus_model;";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(selectQuery);

            // File chooser to save the report
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Bus Models Report");
            int userSelection = fileChooser.showSaveDialog(null);

            if (userSelection != JFileChooser.APPROVE_OPTION) {
                System.out.println("File selection cancelled.");
                return;
            }

            File file = fileChooser.getSelectedFile();
            if (!file.getName().endsWith(".txt")) {
                file = new File(file.getAbsolutePath() + ".txt");
            }

            try (FileWriter writer = new FileWriter(file)) {
                writer.write("Bus Models Report\n");
                writer.write("=====================\n");

                writer.write(String.format("%-15s %-25s %-15s %-10s%n", "Bus Number", "Bus Brand", "Bus Type", "Capacity"));
                writer.write("-------------------------------------------------------------\n");

                // Writing bus models to the file
                while (resultSet.next()) {
                    setBusId(resultSet.getString("bus_id"));
                    setBusBrand(resultSet.getString("brand"));
                    setBusType(resultSet.getString("type"));
                    setBusCapacity(resultSet.getInt("capacity"));

                    writer.write(String.format("%-15s %-25s %-15s %-10d%n", getBusId(), getBusBrand(), getBusType(), getBusCapacity()));
                }

                // Reporting bus count by brand
                writer.write("\n===== Bus Count by Brand =====\n");

                String countQuery = "SELECT brand, COUNT(*) AS bus_count FROM bus_model GROUP BY brand;";
                Statement countStatement = connection.createStatement();
                ResultSet countResultSet = countStatement.executeQuery(countQuery);

                while (countResultSet.next()) {
                    String brand = countResultSet.getString("brand");
                    int busCount = countResultSet.getInt("bus_count");
                    writer.write(String.format("Brand: %-25s | Number of Buses: %d%n", brand, busCount));
                }

                System.out.println("Bus models report saved successfully at: " + file.getAbsolutePath());

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("An error occurred while saving the bus models report.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("An error occurred while retrieving the bus models.");
        }
    }
}
