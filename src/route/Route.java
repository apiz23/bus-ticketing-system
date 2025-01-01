package route;

import bus.Bus;
import utils.SupabaseCon;
import utils.TerminalCommand;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Route {
    private int routeId;
    private String fromStation;
    private String toStation;
    private String date;
    private double price;
    private String time;

    public Route(int routeId, String fromStation, String toStation, String date, double price, String time) {
        this.routeId = routeId;
        this.fromStation = fromStation;
        this.toStation = toStation;
        this.date = date;
        this.price = price;
        this.time = time;
    }

    public Route() {}

    public int getRouteId() { return routeId; }
    public String getFromStation() { return fromStation; }
    public String getToStation() { return toStation; }
    public String getDate() { return date; }
    public double getPrice() { return price; }
    public String getTime() { return time; }

    public void menu(){
        Scanner scanner = new Scanner(System.in);
        TerminalCommand cmd = new TerminalCommand();
        boolean status = true;

        do {
            System.out.println("===== Route Menu =====");
            System.out.println("1. View Bus Routes");
            System.out.println("2. Add a New Bus Route");
            System.out.println("3. Remove a Bus Route");
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
                    viewRoutes();
                    break;

                case 2:
                    addRoute(scanner);
                    break;

                case 3:
                    deleteRoute(scanner);
                    break;

                case 0:
                    System.out.println("Returning to Main Menu...");
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

    public String[] searchRoutes(String startStation, String endStation, String date) {
        Bus bus = new Bus();

        try {
            Connection connection = SupabaseCon.connect();
            if (connection == null) {
                System.out.println("Failed to connect to the database.");
                return null;
            }

            String query = "SELECT br.route_id, br.from_station, br.to_station, br.date, br.time, br.price, " +
                    "bm.brand, bm.type AS bus_model_type, bm.bus_id AS bus_model_id " +
                    "FROM bus_route br " +
                    "JOIN bus_model bm ON br.bus_model_id = bm.bus_id " +
                    "WHERE LOWER(br.from_station) LIKE ?";

            if (!endStation.isEmpty()) {
                query += " AND LOWER(br.to_station) LIKE ?";
            }

            if (!date.isEmpty()) {
                query += " AND TO_DATE(br.date, 'DD-MM-YYYY') = TO_DATE(?, 'DD-MM-YYYY')";
            }

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, "%" + startStation.toLowerCase() + "%");

            int paramIndex = 2;
            if (!endStation.isEmpty()) {
                statement.setString(paramIndex++, "%" + endStation.toLowerCase() + "%");
            }

            if (!date.isEmpty()) {
                statement.setString(paramIndex, date);
            }

            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                System.out.println("No routes found for the specified trip.");
                new TerminalCommand().waitForEnter();
                return null;
            }

            System.out.println("No\tFrom Station\t\tTo Station\t\t\tDate\t\tTime\t\tPrice\t\tBus Brand\t\tBus Model Type");
            System.out.println("+----+----------------------+----------------------+-------------------+--------+---------------------+---------------------+------------------+--------------------+");

            List<Integer> busModelIds = new ArrayList<>();
            List<Integer> routeIds = new ArrayList<>();
            List<Double> prices = new ArrayList<>();

            int rowNumber = 1;
            do {
                String fromStation = resultSet.getString("from_station");
                String toStation = resultSet.getString("to_station");
                String routeDate = resultSet.getString("date");
                String time = resultSet.getString("time");
                double price = resultSet.getDouble("price");
                String brandName = resultSet.getString("brand");
                String busModelType = resultSet.getString("bus_model_type");
                int busModelId = resultSet.getInt("bus_model_id");
                int routeId = resultSet.getInt("route_id");

                busModelIds.add(busModelId);
                routeIds.add(routeId);
                prices.add(price);

                System.out.println(rowNumber + "\t" + fromStation + "\t" + toStation + "\t" + routeDate + "\t" + time +
                        "\tRM" + price + "\t\t" + brandName + "\t" + busModelType);

                rowNumber++;
            } while (resultSet.next());

            System.out.println("+----+----------------------+----------------------+-------------------+--------+---------------------+---------------------+------------------+--------------------+");

            Scanner scanner = new Scanner(System.in);
            System.out.print("Choose a route by entering the number (1-" + busModelIds.size() + "): ");
            int choice = scanner.nextInt();

            if (choice < 1 || choice > busModelIds.size()) {
                System.out.println("Invalid choice. Please try again.");
            } else {
                int selectedBusModelId = busModelIds.get(choice - 1);
                int selectedRouteId = routeIds.get(choice - 1);
                double price = prices.get(choice - 1);

                String[] chosenSeats = bus.chooseSeats(selectedBusModelId, selectedRouteId);
                if (chosenSeats != null) {
                    return new String[] {
                            String.valueOf(selectedRouteId),
                            String.valueOf(selectedBusModelId),
                            String.valueOf(price),
                            chosenSeats[0],
                            chosenSeats[1],
                    };
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("An error occurred while fetching the routes.");
        }
        return null;
    }

    public String[] searchRoutes(String startStation, String startDate) {
        Bus bus = new Bus();

        try {
            Connection connection = SupabaseCon.connect();
            if (connection == null) {
                System.out.println("Failed to connect to the database.");
                return null;
            }

            String query = "SELECT br.route_id, br.from_station, br.to_station, br.date, br.time, br.price, " +
                    "bm.brand, bm.type AS bus_model_type, bm.bus_id AS bus_model_id " +
                    "FROM bus_route br " +
                    "JOIN bus_model bm ON br.bus_model_id = bm.bus_id " +
                    "WHERE LOWER(br.from_station) LIKE ?";

            if (!startDate.isEmpty()) {
                query += " AND TO_DATE(br.date, 'DD-MM-YYYY') = TO_DATE(?, 'DD-MM-YYYY')";
            }

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, "%" + startStation.toLowerCase() + "%");

            if (!startDate.isEmpty()) {
                statement.setString(2, startDate);
            }

            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                System.out.println("No routes found for the specified trip.");
                new TerminalCommand().waitForEnter();
                return null;
            }

            System.out.println("No\tFrom Station\t\tTo Station\t\t\tDate\t\tTime\t\tPrice\t\tBus Brand\t\tBus Model Type");
            System.out.println("+----+----------------------+----------------------+-------------------+--------+---------------------+---------------------+------------------+--------------------+");

            List<Integer> busModelIds = new ArrayList<>();
            List<Integer> routeIds = new ArrayList<>();
            List<Double> prices = new ArrayList<>();

            int rowNumber = 1;
            do {
                String fromStation = resultSet.getString("from_station");
                String toStation = resultSet.getString("to_station");
                String routeDate = resultSet.getString("date");
                String time = resultSet.getString("time");
                double price = resultSet.getDouble("price");
                String brandName = resultSet.getString("brand");
                String busModelType = resultSet.getString("bus_model_type");
                int busModelId = resultSet.getInt("bus_model_id");
                int routeId = resultSet.getInt("route_id");

                busModelIds.add(busModelId);
                routeIds.add(routeId);
                prices.add(price);

                System.out.println(rowNumber + "\t" + fromStation + "\t" + toStation + "\t" + routeDate + "\t" + time +
                        "\tRM" + price + "\t\t" + brandName + "\t" + busModelType);

                rowNumber++;
            } while (resultSet.next());

            System.out.println("+----+----------------------+----------------------+-------------------+--------+---------------------+---------------------+------------------+--------------------+");

            Scanner scanner = new Scanner(System.in);
            System.out.print("Choose a route by entering the number (1-" + busModelIds.size() + "): ");
            int choice = scanner.nextInt();

            if (choice < 1 || choice > busModelIds.size()) {
                System.out.println("Invalid choice. Please try again.");
            } else {
                int selectedBusModelId = busModelIds.get(choice - 1);
                int selectedRouteId = routeIds.get(choice - 1);
                double price = prices.get(choice - 1);

                String[] chosenSeats = bus.chooseSeats(selectedBusModelId, selectedRouteId);
                if (chosenSeats != null) {
                    return new String[]{
                            String.valueOf(selectedRouteId),
                            String.valueOf(selectedBusModelId),
                            String.valueOf(price),
                            chosenSeats[0],
                            chosenSeats[1],
                    };
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("An error occurred while fetching the routes.");
        }
        return null;
    }

    public void viewRoutes() {
        try (Connection connection = SupabaseCon.connect()) {
            if (connection == null) {
                System.out.println("Failed to connect to the database.");
                return;
            }

            String fetchRoutesQuery = "SELECT route_id, from_station, to_station, date, price, time " +
                    "FROM bus_route;";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(fetchRoutesQuery);

            new TerminalCommand().customText("Bus Routes");

            System.out.printf("%-10s %-50s %-50s %-15s %-15s %-10s%n", "Route ID", "From", "To", "Date", "Time", "Price");
            System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------");

            while (resultSet.next()) {
                Route route = new Route(
                        resultSet.getInt("route_id"),
                        resultSet.getString("from_station"),
                        resultSet.getString("to_station"),
                        resultSet.getString("date"),
                        resultSet.getDouble("price"),
                        resultSet.getString("time")
                );

                System.out.printf("%-10d %-50s %-50s %-15s %-15s RM%-10.2f%n",
                        route.getRouteId(),
                        route.getFromStation(),
                        route.getToStation(),
                        route.getDate(),
                        route.getTime(),
                        route.getPrice());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("An error occurred while retrieving the bus routes.");
        }
    }

    public void addRoute(Scanner scanner) {
        try (Connection connection = SupabaseCon.connect()) {
            if (connection == null) {
                System.out.println("Failed to connect to the database.");
                return;
            }

            new TerminalCommand().customText("Add Bus Route");
            System.out.println("Available Bus Models:");
            String busModelsQuery = "SELECT bus_id, brand, capacity, type FROM bus_model";
            PreparedStatement busModelsStatement = connection.prepareStatement(busModelsQuery);
            ResultSet resultSet = busModelsStatement.executeQuery();

            while (resultSet.next()) {
                int busId = resultSet.getInt("bus_id");
                String brand = resultSet.getString("brand");
                int capacity = resultSet.getInt("capacity");
                String type = resultSet.getString("type");
                System.out.println("ID: " + busId + ", Brand: " + brand + ", Capacity: " + capacity + ", Type: " + type);
            }

            System.out.print("Enter the Bus Model ID you want to associate with this route: ");
            int selectedBusModelId = scanner.nextInt();
            scanner.nextLine();

            System.out.print("Enter the from station: ");
            String fromStation = scanner.nextLine();

            System.out.print("Enter the to station: ");
            String toStation = scanner.nextLine();

            System.out.print("Enter the date (e.g., 20-12-2025): ");
            String date = scanner.nextLine();

            System.out.print("Enter the price: ");
            double price = scanner.nextDouble();
            scanner.nextLine();

            System.out.print("Enter the time range (e.g., 9.00 - 13.00): ");
            String time = scanner.nextLine();

            String maxRouteIdQuery = "SELECT MAX(route_id) FROM bus_route";
            PreparedStatement maxRouteIdStatement = connection.prepareStatement(maxRouteIdQuery);
            ResultSet maxRouteIdResult = maxRouteIdStatement.executeQuery();

            long newRouteId = 1;
            if (maxRouteIdResult.next()) {
                newRouteId = maxRouteIdResult.getLong(1) + 1;
            }

            String insertRouteQuery = "INSERT INTO bus_route (route_id, from_station, to_station, date, price, bus_model_id, time) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement insertStatement = connection.prepareStatement(insertRouteQuery);
            insertStatement.setLong(1, newRouteId);
            insertStatement.setString(2, fromStation);
            insertStatement.setString(3, toStation);
            insertStatement.setString(4, date);
            insertStatement.setDouble(5, price);
            insertStatement.setInt(6, selectedBusModelId);
            insertStatement.setString(7, time);

            int rowsAffected = insertStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Bus route added successfully.");
            } else {
                System.out.println("Failed to add the bus route.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("An error occurred while adding the bus route.");
        }
    }

    public void deleteRoute(Scanner scanner) {
        try (Connection connection = SupabaseCon.connect()) {
            if (connection == null) {
                System.out.println("Failed to connect to the database.");
                return;
            }
            new TerminalCommand().customText("Remove Bus Route");

            String fetchRoutesQuery = "SELECT route_id, from_station, to_station, date, price FROM bus_route";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(fetchRoutesQuery);

            System.out.println("Available Bus Routes:");
            while (resultSet.next()) {
                int routeId = resultSet.getInt("route_id");
                String fromStation = resultSet.getString("from_station");
                String toStation = resultSet.getString("to_station");
                String date = resultSet.getString("date");
                double price = resultSet.getDouble("price");

                System.out.printf("Route ID: %d, From: %s, To: %s, Date: %s, Price: RM%.2f%n",
                        routeId, fromStation, toStation, date, price);
            }

            System.out.println("\nEnter the Route ID of the bus route to delete: ");
            int routeIdToDelete = scanner.nextInt();
            scanner.nextLine();

            String deleteQuery = "DELETE FROM bus_route WHERE route_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery);
            preparedStatement.setInt(1, routeIdToDelete);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Bus route deleted successfully.");
            } else {
                System.out.println("No bus route found with the provided ID.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("An error occurred while deleting the bus route.");
        }
    }
}
