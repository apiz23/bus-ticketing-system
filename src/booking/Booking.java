package booking;

import bus.Bus;
import route.Route;
import user.Client;
import utils.QrCodeGen;
import utils.SupabaseCon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Booking {
    private String customerName;
    private String bookingDate;
    private String bookingTime;

    public Booking(){
    }

    public Booking(String customerName, String bookingDate, String bookingTime) {
        this.customerName = customerName;
        this.bookingDate = bookingDate;
        this.bookingTime = bookingTime;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public String getBookingTime() {
        return bookingTime;
    }

    public void displayBooking() {
        System.out.println("Customer: " + customerName);
        System.out.println("Date: " + bookingDate);
        System.out.println("Time: " + bookingTime);
    }

    public void menu() {
        Scanner scanner = new Scanner(System.in);

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

                System.out.println("\nEnter the seat number you want to book (1-" + capacity + "): ");
                int selectedSeat = scanner.nextInt();

                if (selectedSeat < 1 || selectedSeat > capacity) {
                    System.out.println("Invalid seat number. Please try again.");
                    return;
                }

                if (Arrays.asList(seats).contains(String.valueOf(selectedSeat))) {
                    System.out.println("Seat " + selectedSeat + " is already booked.");
                    return;
                }
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
            String updatedSeatsString = seatsString != null && !seatsString.isEmpty()
                    ? seatsString + "," + selectedSeat
                    : String.valueOf(selectedSeat);

            String updateQuery = "UPDATE bus_route SET seats = ? WHERE route_id = ?";
            PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
            updateStatement.setString(1, updatedSeatsString);
            updateStatement.setInt(2, routeId);

            int rowsAffected = updateStatement.executeUpdate();
            if (rowsAffected > 0) {
                Client client = new Client();
                ArrayList<String> clientDetails = client.fillCredentials();
                clientDetails.add("Route ID: " + routeId);
                clientDetails.add("Bus ID: " + busId);
                clientDetails.add("Selected Seat: " + selectedSeat);

                String insertQuery = "INSERT INTO public.bus_booking (route_id, bus_id, seat_no, name, age, email, no_phone, address) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

                PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
                insertStatement.setInt(1, routeId);
                insertStatement.setInt(2, busId);
                insertStatement.setInt(3, selectedSeat);
                insertStatement.setString(4, clientDetails.get(0));
                insertStatement.setInt(5, Integer.parseInt(clientDetails.get(1)));
                insertStatement.setString(6, clientDetails.get(2));
                insertStatement.setString(7, clientDetails.get(3));
                insertStatement.setString(8, clientDetails.get(4));

                int insertedRows = insertStatement.executeUpdate();
                if (insertedRows > 0) {
                    System.out.println("Booking successful! The seat has been reserved.\nPlease Scan the QR Code to generate your pdf.");
                    QrCodeGen.generateDefaultQRCode("https://hafizu-blog.vercel.app/");
                } else {
                    System.out.println("Failed to insert booking data.");
                }
            } else {
                System.out.println("Failed to book the seat.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("An error occurred while creating the booking.");
        }
    }
}

