package booking;

import route.Route;
import user.Client;
import utils.HttpsRequest;
import utils.SupabaseCon;
import utils.TerminalCommand;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Booking {

    public Booking() {}

    public void menu(Scanner scanner) {
        TerminalCommand cmd = new TerminalCommand();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        dateFormat.setLenient(false);
        Route busRoute = new Route();

        cmd.Clear();
        cmd.customText("Welcome To BBS");

        System.out.println("\nEnter From Where:");
        String startStation = scanner.nextLine().toLowerCase();

        System.out.println("Enter To Where:");
        String endStation = scanner.nextLine().toLowerCase();

        String startDate = null;
        String endDate;

        while (startDate == null) {
            System.out.println("Enter Start Date (DD-MM-YYYY):");
            String inputStartDate = scanner.nextLine();

            if (isValidDate(inputStartDate, dateFormat)) {
                startDate = inputStartDate;
            } else {
                System.out.println("Invalid start date format. Please enter the date in DD-MM-YYYY format.");
            }
        }

        while (true) {
            System.out.println("Enter Return Date (DD-MM-YYYY) or leave empty for no return date:");
            String inputEndDate = scanner.nextLine();

            if (inputEndDate.isEmpty()) {
                endDate = null;
                break;
            } else if (isValidDate(inputEndDate, dateFormat) && isEndDateAfterStartDate(inputEndDate, startDate, dateFormat)) {
                endDate = inputEndDate;
                break;
            } else {
                System.out.println("Invalid end date. The return date should be after the start date.");
            }
        }

        if (endDate != null) {
            String[] result1 = busRoute.searchRoutes(startStation, endStation, startDate);

            if (result1 != null) {
                System.out.println("\nChoose your return trip bus:");
                String[] result2 = busRoute.searchRoutes(endStation, startStation, endDate);

                if (result2 != null) {
                    double totalPrice = Double.parseDouble(result1[2]) + Double.parseDouble(result2[2]);
                    ArrayList<String> clientDetails = new Client().fillCredentials();

                    while (true) {
                        System.out.print("Enter payment amount (Route Price: RM " + totalPrice + "): RM ");
                        double paymentAmount = scanner.nextDouble();

                        if (paymentAmount == totalPrice) {
                            System.out.println("Payment successful. Booking your seat...");
                            createBooking(Integer.parseInt(result1[1]), Integer.parseInt(result1[0]), Integer.parseInt(result1[3]), result1[4], clientDetails);
                            createBooking(Integer.parseInt(result2[1]), Integer.parseInt(result2[0]), Integer.parseInt(result2[3]), result2[4], clientDetails);
                            System.out.println("Seat has been booked successfully.");
                            System.out.println("Check your email for the receipt. Thank you!");
                            new TerminalCommand().waitForEnter();
                            break;
                        } else {
                            System.out.println("Payment failed. Incorrect payment amount.");
                        }
                    }
                } else {
                    System.out.println("No return trip route found or an error occurred.");
                }
            } else {
                System.out.println("No outbound route found or an error occurred.");
            }
        } else {
            String[] result = busRoute.searchRoutes(startStation, endStation, startDate);

            if (result != null) {
                ArrayList<String> clientDetails = new Client().fillCredentials();

                while (true) {
                    System.out.print("Enter payment amount (Route Price: RM " + result[2] + "): RM ");
                    double paymentAmount = scanner.nextDouble();

                    if (paymentAmount == Double.parseDouble(result[2])) {
                        System.out.println("Payment successful. Booking your seat...");
                        createBooking(Integer.parseInt(result[1]), Integer.parseInt(result[0]), Integer.parseInt(result[3]), result[4], clientDetails);
                        System.out.println("Seat has been booked successfully.");
                        System.out.println("Check your email for the receipt. Thank you!");
                        new TerminalCommand().waitForEnter();
                        break;
                    } else {
                        System.out.println("Payment failed. Incorrect payment amount.");
                    }
                }
            } else {
                System.out.println("No route found or an error occurred.");
            }
        }
    }

    public void menu(){
        Scanner scanner = new Scanner(System.in);
        TerminalCommand cmd = new TerminalCommand();
        boolean status = true;

        do {
            System.out.println("===== Booking Menu =====");
            System.out.println("1. View All Booking Histories");
            System.out.println("2. View Booking History by Email");
            System.out.println("3. Generate report");
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
                    new Booking().viewBookingHistory();
                    cmd.waitForEnter();

                    break;

                case 2:
                    System.out.print("Enter Your Email: ");
                    String userEmail = scanner.nextLine().trim();
                    if (!userEmail.isEmpty()) {
                        new Booking().viewBookingHistory(userEmail);
                    } else {
                        System.out.println("Email cannot be empty. Please try again.");
                    }
                    cmd.waitForEnter();
                    break;

                case 3:
                    generateBookingReport();
                    break;

                case 0:
                    status = false;
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
                    cmd.waitForEnter();
            }
            cmd.Clear();
        } while (status);
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

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("An error occurred while fetching booking details.");
        }
    }

    public void viewBookingHistory(String userEmail) {
        try (Connection connection = SupabaseCon.connect()) {
            if (connection == null) {
                System.out.println("Failed to connect to the database.");
                return;
            }

            String query = "SELECT * FROM public.bus_booking WHERE email = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, userEmail);
            ResultSet resultSet = statement.executeQuery();

            new TerminalCommand().customText("Booking Details for: " + userEmail);
            System.out.printf("%-10s %-10s %-10s %-10s %-20s %-5s %-15s %-20s\n",
                    "Book ID", "Route ID", "Bus ID", "Seat No", "Name", "Age", "Phone", "Address");
            System.out.println("----------------------------------------------------------------------------------------------");
            boolean hasResults = false;

            while (resultSet.next()) {
                hasResults = true;

                int bookId = resultSet.getInt("book_id");
                int routeId = resultSet.getInt("route_id");
                int busId = resultSet.getInt("bus_id");
                int seatNo = resultSet.getInt("seat_no");
                String name = resultSet.getString("name");
                int age = resultSet.getInt("age");
                String phone = resultSet.getString("no_phone");
                String address = resultSet.getString("address");

                System.out.printf("%-10d %-10d %-10d %-10d %-20s %-5d %-15s %-20s\n",
                        bookId, routeId, busId, seatNo, name, age, phone, address);
            }

            if (!hasResults) {
                System.out.println("No booking history found for the provided email.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("An error occurred while fetching booking details.");
        }
    }

    public void generateBookingReport() {
        try (Connection connection = SupabaseCon.connect()) {
            if (connection == null) {
                System.out.println("Failed to connect to the database.");
                return;
            }

            String query = "SELECT book_id, route_id, bus_id, seat_no, name, age, email, no_phone, address FROM public.bus_booking";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Booking Report");
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
                writer.write("Booking History Report\n");
                writer.write("========================\n\n");

                writer.write(String.format("%-10s %-10s %-10s %-10s %-20s %-5s %-30s %-15s %-20s\n",
                        "Book ID", "Route ID", "Bus ID", "Seat No", "Name", "Age", "Email", "Phone", "Address"));
                writer.write("--------------------------------------------------------------------------------------------------------\n");

                int totalBookings = 0;
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

                    writer.write(String.format("%-10d %-10d %-10d %-10d %-20s %-5d %-30s %-15s %-20s\n",
                            bookId, routeId, busId, seatNo, name, age, email, phone, address));

                    totalBookings++;
                }

                writer.write("\nTotal Number of Bookings: " + totalBookings + "\n");

                System.out.println("Booking report saved successfully at: " + file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("An error occurred while saving the booking report.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("An error occurred while fetching the booking data.");
        }
    }

    private static boolean isValidDate(String date, SimpleDateFormat dateFormat) {
        try {
            Date parsedDate = dateFormat.parse(date);
            return date.equals(dateFormat.format(parsedDate));
        } catch (ParseException e) {
            return false;
        }
    }

    private static boolean isEndDateAfterStartDate(String endDate, String startDate, SimpleDateFormat dateFormat) {
        try {
            Date start = dateFormat.parse(startDate);
            Date end = dateFormat.parse(endDate);
            return end.after(start);
        } catch (ParseException e) {
            return false;
        }
    }
}
