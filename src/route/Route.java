package route;

import booking.Booking;
import bus.Bus;
import utils.SupabaseCon;
import utils.TerminalCommand;

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
        this.busModelId = busModelId;
        this.seats = seats;
        this.time = time;
        this.createdAt = createdAt;
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

            System.out.println("No\tFrom Station\t\tTo Station\t\t\tDate\t\tTime\t\tPrice\t\tBus Brand\t\tBus Model Type");
            System.out.println("+----+----------------------+----------------------+-------------------+--------+---------------------+---------------------+------------------+--------------------+");

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

                System.out.println(rowNumber + "\t" + fromStation + "\t" + toStation + "\t" + date + "\t" + time +
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

                new Booking().chooseSeats(selectedBusModelId, selectedRouteId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("An error occurred while fetching the routes.");
        }
    }

    public void addRoute(){
        Scanner scanner = new Scanner(System.in);
        try (Connection connection = SupabaseCon.connect()) {
            if (connection == null) {
                System.out.println("Failed to connect to the database.");
                return;
            }
            String query = "SELECT * FROM public.bus_model";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                System.out.println("Please add bus model before adding bus route.");
                return;
            }
            System.out.println("+------+----------------------------------------+--------------------+--------+");
            System.out.println("No\tBrand\t\t\t\t\tType\t\t     Capacity");
            System.out.println("+------+----------------------------------------+--------------------+--------+");
            do {
                Integer model_id =resultSet.getInt("bus_id");
                String brand = resultSet.getString("brand");
                String type = resultSet.getString("type");
                String capacity = resultSet.getString("capacity");
                System.out.printf("%-6d\t%-36s\t%-22s%-5s\n",model_id,brand,type,capacity);
            } while (resultSet.next());
            System.out.println("+------+----------------------------------------+--------------------+--------+");
            System.out.println("Enter Bus Model No for new bus route: ");
            int bus_model = scanner.nextInt();
            scanner.nextLine();
            System.out.println("Enter Origin Bus Station: ");
            String from_station = scanner.nextLine();
            System.out.println("Enter Destination Bus Station: ");
            String to_station = scanner.nextLine();
            System.out.println("Enter date (DD-MM-YYYY):");
            String date = scanner.nextLine();
            System.out.println("Enter price: RM");
            float price = scanner.nextFloat();
            scanner.nextLine();
            System.out.println("Enter departure time: ");
            String departureTime = scanner.nextLine();
            System.out.println("Enter estimated arrival time: ");
            String arrivalTime = scanner.nextLine();
            String query1 = "INSERT INTO public.bus_route (from_station, to_station, date,bus_model_id, price,time, created_at) VALUES (?, ?, ?, ?, ?, ?,now())";
            statement = connection.prepareStatement(query1);
            statement.setString(1, from_station);
            statement.setString(2, to_station);
            statement.setString(3, date);
            statement.setInt(4, bus_model);
            statement.setFloat(5, price);
            statement.setString(6, departureTime+" - "+arrivalTime);
            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("New bus route added successfully!");
            } else {
                System.out.println("Failed to add the bus route. Please try again.");
            }

            new TerminalCommand().waitForEnter();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("An error occurred while adding the bus route.");
        }
    }

    public void deleteRoute(){
        Scanner scanner = new Scanner(System.in);
        try (Connection connection = SupabaseCon.connect()) {
            if (connection == null) {
                System.out.println("Failed to connect to the database.");
                return;
            }
            String query = "SELECT br.route_id, br.from_station, br.to_station, br.date, br.time, br.price," +
                    "bm.brand, bm.type AS bus_model_type, bm.bus_id AS bus_model_id " +
                    "FROM bus_route br " +
                    "JOIN bus_model bm ON br.bus_model_id = bm.bus_id "+
                    "ORDER BY br.route_id ASC";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                System.out.println("No any bus route found in the database.");
                return;
            }
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
                System.out.println(routeId + "\t" + fromStation + "\t" + toStation + "\t" + date + "\t" + time +
                        "\tRM" + price + "\t\t" + brandName + "\t" + busModelType);
            } while (resultSet.next());
            int[] bus_route =new int[100];
            int count=0,cont=1;
            do{
                System.out.println("Enter Bus Route No to delete: ");
                bus_route[count] = scanner.nextInt();
                scanner.nextLine();
                System.out.println("Is there anymore bus route to delete? (Y=Yes, N=No) ");
                String choice= scanner.nextLine();
                if(choice.toLowerCase().equals("y")){
                    count++;
                }
                else{
                    cont= 0;
                }
            }while(cont==1);
            StringBuilder queryBuilder = new StringBuilder("DELETE FROM bus_route WHERE route_id IN (");
            for (int i = 0; i <=count; i++) {
                queryBuilder.append("?");
                if (i < count) {
                    queryBuilder.append(",");  // Add a comma between placeholders
                }
            }
            queryBuilder.append(")");
            String query1 = queryBuilder.toString();
            statement = connection.prepareStatement(query1);
            for (int i = 0; i <= count; i++) {
                statement.setInt( i+1 , bus_route[i]);
            }
            int rowsDeleted = statement.executeUpdate();
            System.out.println(rowsDeleted);
            if (rowsDeleted != count) {
                System.out.println(rowsDeleted);
                System.out.println("Bus route deleted successfully!");
            } else {
                System.out.println("Failed to deleted the bus route. Please try again.");
            }

            new TerminalCommand().waitForEnter();
        } catch (SQLException e) {
            String errorMessage = e.getMessage();
            if (errorMessage.contains("violates foreign key constraint")){
                System.out.println("Unable to delete the bus route as the bus route had been booked.");
            }
            else{
                System.out.println("An error occurred while deleting the bus route.");
                e.printStackTrace();
            }

        }
    }
}
