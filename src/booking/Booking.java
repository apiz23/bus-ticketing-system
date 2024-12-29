package booking;

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

    public Booking() {}

    public void Menu(Scanner scanner) {
        scanner.nextLine();

        new TerminalCommand().Clear();
        new TerminalCommand().customText("Welcome To BBS");

        System.out.println("\n\nEnter From Where:");
        String startStation = scanner.nextLine().toLowerCase();

        System.out.println("Enter To Where:");
        String endStation = scanner.nextLine().toLowerCase();

        System.out.println("Enter Start Date (DD-MM-YYYY):");
        String startDate = scanner.nextLine();

        System.out.println("Enter Return Date (DD-MM-YYYY) or leave empty for no return date:");
        String endDate = scanner.nextLine();

        Route busRoute = new Route();

        if (!endDate.isEmpty()) {
            String[] result1 =  busRoute.searchRoutes(startStation, endStation, startDate);
            System.out.println("\nChoose your return trip bus:");

            if (result1 != null) {
                String[] result2 =  busRoute.searchRoutes(endStation, "", endDate);

                if (result2 != null) {

                    double totalPrice = Double.parseDouble(result1[2]) + Double.parseDouble(result2[2]);
                    ArrayList<String> clientDetails = new Client().fillCredentials();

                    do {
                        System.out.print("Enter payment amount (Route Price: RM " + totalPrice + "): RM ");
                        double paymentAmount = scanner.nextDouble();

                        if (paymentAmount == totalPrice) {
                            System.out.println("Payment successful. Booking your seat...");
                            createBooking(Integer.parseInt(result1[1]), Integer.parseInt(result1[0]), Integer.parseInt(result1[3]), result1[4], clientDetails);
                            createBooking(Integer.parseInt(result2[1]), Integer.parseInt(result2[0]), Integer.parseInt(result2[3]), result2[4], clientDetails);
                            System.out.println("Seat has been booked successfully.");
                            System.out.println("Check your email for the receipt. Thank you!");
                            break;
                        } else {
                            System.out.println("Payment failed. Incorrect payment amount.");
                        }
                    } while (true);
                } else {
                    System.out.println("No return trip route found or an error occurred.");
                }
            } else {
                System.out.println("No outbound route found or an error occurred.");
            }
        } else {

            String[] result =  busRoute.searchRoutes(startStation, endStation, startDate);

            if (result != null) {
                ArrayList<String> clientDetails = new Client().fillCredentials();

                do {
                    System.out.print("Enter payment amount (Route Price: RM " + result[2] + "): RM ");
                    double paymentAmount = scanner.nextDouble();

                    if (paymentAmount == Double.parseDouble(result[2])) {
                        System.out.println("Payment successful. Booking your seat...");
                        createBooking(Integer.parseInt(result[1]), Integer.parseInt(result[0]), Integer.parseInt(result[3]),result[4], clientDetails);
                        System.out.println("Seat has been booked successfully.");
                        System.out.println("Check your email for the receipt. Thank you!");
                        break;
                    } else {
                        System.out.println("Payment failed. Incorrect payment amount.");
                    }
                } while (true);
            } else {
                System.out.println("No route found or an error occurred.");
            }
        }
    }

    public void createBooking(int busId, int routeId, int selectedSeat, String seatsString, ArrayList<String> clientDetails) {

        try (Connection connection = SupabaseCon.connect()) {
            if (connection == null) {
                System.out.println("Failed to connect to the database.");
                return;
            }

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
                    HttpsRequest.sendEmailReceipt(bookId);
                } else {
                    System.out.println("Failed to update seat information.");
                }
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

            new TerminalCommand().customText("Booking Details");
            System.out.printf("%-10s %-10s %-10s %-10s %-20s %-5s %-30s %-15s %-20s\n",
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

                System.out.printf("%-10d %-10d %-10d %-10d %-20s %-5d %-30s %-15s %-20s\n",
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
