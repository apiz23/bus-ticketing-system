package controller;

import service.SupabaseCon;
import java.sql.*;

public class BookingController {

    public void filterRoutes(String startStation, String endStation) {
        try (Connection connection = SupabaseCon.connect()) {

            if (connection == null) {
                System.out.println("Failed to connect to the database.");
                return;
            }

            String query = "SELECT * FROM routes WHERE fromStation = ? AND toStation = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, startStation);
                statement.setString(2, endStation);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        System.out.println("No routes found for the specified trip.");
                    } else {
                        System.out.println("\nAvailable Routes:");
                        do {
                            String routeDetails = resultSet.getString("routeDetails");
                            System.out.println("Route Details: " + routeDetails);
                        } while (resultSet.next());
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("An error occurred while fetching the routes.");
        }
    }

    public void createBooking(String startStation, String endStation, String startDate, String endDate) {
        System.out.println("Booking confirmed from " + startStation + " to " + endStation + " on " + startDate);
        if (!endDate.isEmpty()) {
            System.out.println("Return date: " + endDate);
        } else {
            System.out.println("No return date provided.");
        }
    }
}
