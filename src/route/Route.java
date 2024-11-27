package route;

import booking.Booking;
import utils.SupabaseCon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Route {
    private long routeId;
    private String fromStation;
    private String toStation;
    private String date;
    private double price;
    private String createdAt;
    private long busModelId;
    private String seats;
    private int time;

    public Route(long routeId, String fromStation, String toStation, String date, double price,
                    String createdAt, long busModelId, String seats, int time) {
        this.routeId = routeId;
        this.fromStation = fromStation;
        this.toStation = toStation;
        this.date = date;
        this.price = price;
        this.createdAt = createdAt;
        this.busModelId = busModelId;
        this.seats = seats;
        this.time = time;
    }

    public Route() {

    }

    public long getRouteId() { return routeId; }
    public String getFromStation() { return fromStation; }
    public String getToStation() { return toStation; }
    public String getDate() { return date; }
    public double getPrice() { return price; }
    public String getCreatedAt() { return createdAt; }
    public long getBusModelId() { return busModelId; }
    public String getSeats() { return seats; }
    public int getTime() { return time; }

    @Override
    public String toString() {
        return "Route ID: " + routeId + ", From: " + fromStation + ", To: " + toStation +
                ", Date: " + date + ", Price: " + price + ", Time: " + time +
                ", Bus Model ID: " + busModelId + ", Seats: " + seats;
    }

    public void oneWayRoutes(String startStation, String endStation, String startDate) {
        Connection connection = null;

        try {
            connection = SupabaseCon.connect();
            if (connection == null) {
                System.out.println("Failed to connect to the database.");
                return;
            }

            String query = "SELECT br.route_id, br.from_station, br.to_station, br.date, br.time, br.price, " +
                    "bm.brand, bm.type AS bus_model_type, bm.bus_id AS bus_model_id " +
                    "FROM bus_route br " +
                    "JOIN bus_model bm ON br.bus_model_id = bm.bus_id " +
                    "WHERE LOWER(br.from_station) LIKE ? AND LOWER(br.to_station) LIKE ?";

            if (!startDate.isEmpty()) {
                query += " AND TO_DATE(br.date, 'DD-MM-YYYY') = TO_DATE(?, 'DD-MM-YYYY')";
            }

            PreparedStatement statement = connection.prepareStatement(query);

            statement.setString(1, "%" + startStation.toLowerCase() + "%");
            statement.setString(2, "%" + endStation.toLowerCase() + "%");

            int paramIndex = 3;
            if (!startDate.isEmpty()) {
                statement.setString(paramIndex++, startDate);
            }

            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                System.out.println("No routes found for the specified trip.");
                return;
            }

            System.out.println("+----+----------------------+----------------------+-------------------+--------+---------------------+---------------------+------------------+");
            System.out.println("| No | From Station         | To Station           | Date              | Time   | Price  | Bus Brand           | Bus Model Type      |");
            System.out.println("+----+----------------------+----------------------+-------------------+--------+---------------------+---------------------+------------------+");

            List<Integer> busModelIds = new ArrayList<>();
            List<Integer> routeIds = new ArrayList<>();

            int rowNumber = 1;
            do {
                String fromStation = resultSet.getString("from_station");
                String toStation = resultSet.getString("to_station");
                String date = resultSet.getString("date");
                String time = resultSet.getString("time");
                double price = resultSet.getDouble("price");
                String brandName = resultSet.getString("brand");
                String busModelType = resultSet.getString("bus_model_type");
                int busModelId = resultSet.getInt("bus_model_id");
                int routeId = resultSet.getInt("route_id");

                busModelIds.add(busModelId);
                routeIds.add(routeId);

                System.out.format("| %-2d | %-20s | %-20s | %-17s | %-6s | RM%-6.2f | %-19s | %-19s |\n",
                        rowNumber, fromStation, toStation, date, time, price, brandName, busModelType);

                rowNumber++;
            } while (resultSet.next());

            System.out.println("+----+----------------------+----------------------+-------------------+--------+---------------------+---------------------+------------------+");

            Scanner scanner = new Scanner(System.in);
            System.out.print("Choose a route by entering the number (1-" + busModelIds.size() + "): ");
            int choice = scanner.nextInt();

            if (choice < 1 || choice > busModelIds.size()) {
                System.out.println("Invalid choice. Please try again.");
            } else {
                int selectedBusModelId = busModelIds.get(choice - 1);
                int selectedRouteId = routeIds.get(choice - 1);

                Booking book = new Booking();
                book.chooseSeats(selectedBusModelId, selectedRouteId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("An error occurred while fetching the routes.");
        } finally {
            SupabaseCon.closeConnection(connection);
        }
    }
}
