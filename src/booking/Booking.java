package booking;

import bus.Bus;
import route.Route;
import user.Client;
import utils.HttpsRequest;
import utils.SupabaseCon;
import utils.TerminalCommand;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Booking {

    public Booking() {
    }

    public void Menu(Scanner scanner) {
        scanner.nextLine();
        System.out.println("Welcome To BBS\n\nEnter From Where:");
        String startStation = scanner.nextLine().toLowerCase();

        System.out.println("Enter To Where:");
        String endStation = scanner.nextLine().toLowerCase();

        System.out.println("Enter Start Date (DD-MM-YYYY):");
        String startDate = scanner.nextLine();

        System.out.println("Enter Return Date (DD-MM-YYYY) or leave empty for no return date:");
        String endDate = scanner.nextLine();

        Route busRoute = new Route();
        if (!endDate.isEmpty()) {
            System.out.println("End Date: " + endDate);
        } else {
            busRoute.oneWayRoutes(startStation, endStation, startDate);
        }
    }

    public void chooseSeats(int busId, int routeId) {
        Scanner scanner = new Scanner(System.in);

        try (Connection connection = SupabaseCon.connect()) {
            if (connection == null) {
                System.out.println("Failed to connect to the database.");
                return;
            }

            String fetchQuery = "SELECT bm.capacity, bm.type AS bus_type, br.seats " +
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
                        return;
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
                createBooking(connection, busId, routeId, selectedSeat, seatsString);
            } else {
                System.out.println("Bus with ID " + busId + " and route " + routeId + " not found.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("An error occurred while processing the seat selection.");
        }
    }

    private void createBooking(Connection connection, int busId, int routeId, int selectedSeat, String seatsString) {
        try {
            ArrayList<String> clientDetails = new Client().fillCredentials();

            clientDetails.add("Route ID: " + routeId);
            clientDetails.add("Bus ID: " + busId);
            clientDetails.add("Selected Seat: " + selectedSeat);

            String insertQuery = "INSERT INTO public.bus_booking (route_id, bus_id, seat_no, name, age, email, no_phone, address) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING book_id";

            PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
            insertStatement.setInt(1, routeId);
            insertStatement.setInt(2, busId);
            insertStatement.setInt(3, selectedSeat);
            insertStatement.setString(4, clientDetails.get(0));
            insertStatement.setInt(5, Integer.parseInt(clientDetails.get(1)));
            insertStatement.setString(6, clientDetails.get(2));
            insertStatement.setString(7, clientDetails.get(3));
            insertStatement.setString(8, clientDetails.get(4));

            ResultSet rs = insertStatement.executeQuery();
            if (rs.next()) {
                int bookId = rs.getInt("book_id");
                System.out.println("Booking successful! The seat has been reserved.");

                String updatedSeatsString = seatsString != null && !seatsString.isEmpty()
                        ? seatsString + "," + selectedSeat
                        : String.valueOf(selectedSeat);

                String updateQuery = "UPDATE bus_route SET seats = ? WHERE route_id = ?";
                PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                updateStatement.setString(1, updatedSeatsString);
                updateStatement.setInt(2, routeId);

                int rowsAffected = updateStatement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Seat information updated successfully.");
                } else {
                    System.out.println("Failed to update seat information.");
                }

                HttpsRequest.sendReceiptRequest(bookId);
            } else {
                System.out.println("Failed to retrieve the book_id.");
            }

            new TerminalCommand().waitForEnter();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("An error occurred while creating the booking.");
        }
    }

    public void viewBookingHistory() {
        try (Connection connection = SupabaseCon.connect()) {
            if (connection == null) {
                System.out.println("Failed to connect to the database.");
                return;
            }

            String query = "SELECT * FROM public.bus_booking";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            System.out.println("\nBooking Details:");
            System.out.printf("%-10s %-10s %-10s %-10s %-20s %-5s %-20s %-15s %-20s\n",
                    "Book ID", "Route ID", "Bus ID", "Seat No", "Name", "Age", "Email", "Phone", "Address");
            System.out.println("----------------------------------------------------------------------------------------------------------------------");

            while (resultSet.next()) {
                int bookId = resultSet.getInt("book_id");
                int routeId = resultSet.getInt("route_id");
                int busId = resultSet.getInt("bus_id");
                int seatNo = resultSet.getInt("seat_no");
                String name = resultSet.getString("name");
                int age = resultSet.getInt("age");
                String email = resultSet.getString("email");
                String phone = resultSet.getString("no_phone");
                String address = resultSet.getString("address");

                System.out.printf("%-10d %-10d %-10d %-10d %-20s %-5d %-20s %-15s %-20s\n",
                        bookId, routeId, busId, seatNo, name, age, email, phone, address);
            }

            Scanner scanner = new Scanner(System.in);
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("An error occurred while fetching booking details.");
        }
    }
}
