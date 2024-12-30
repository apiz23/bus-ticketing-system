package bus;

import utils.SupabaseCon;
import utils.TerminalCommand;

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
        new TerminalCommand().waitForEnter();
    }

    public void addBusModel(Scanner scanner) {

        new TerminalCommand().Clear();
        scanner.nextLine();
        new TerminalCommand().customText("Add Bus Model");

        System.out.println("Enter Bus Brand: ");
        String busBrand = scanner.nextLine();

        System.out.println("Enter Capacity: ");
        int capacity = scanner.nextInt();
        scanner.nextLine();

        System.out.println("Enter Bus Type (e.g., Economy, Executive, Double Decker): ");
        String busType = scanner.nextLine();

        try (Connection connection = SupabaseCon.connect()) {
            if (connection == null) {
                System.out.println("Failed to connect to the database.");
                return;
            }

            String insertQuery = "INSERT INTO bus_model (brand, busCapacity, type) VALUES (?, ?, ?);";
            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);

            preparedStatement.setString(1, busBrand);
            preparedStatement.setInt(2, capacity);
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
        new TerminalCommand().waitForEnter();
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
        new TerminalCommand().waitForEnter();
    }
}
