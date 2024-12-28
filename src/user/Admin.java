package user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

import booking.Booking;
import route.Route;
import model.Bus;
import utils.SupabaseCon;
import utils.TerminalCommand;

public class Admin extends User {

    public Admin(){
    }

    public void Menu() {

        if (Login()) {
            Scanner scanner = new Scanner(System.in);
            boolean status = true;
            do{
                System.out.println("===== Admin Menu =====");
                System.out.println("1. View Booking History");
                System.out.println("2. View Clients");
                System.out.println("3. View Admins");
                System.out.println("4. Add Admin");
                System.out.println("5. Add Bus Route");
                System.out.println("6. Add Bus Model");
                System.out.println("7. Remove Bus Route");
                System.out.println("8. Remove Bus Model");
                System.out.println("0. Main Menu");
                System.out.print("Enter your choice: ");

                if (!scanner.hasNextInt()) {
                    System.out.println("Invalid input. Please enter a valid number.");
                    scanner.nextLine();
                    continue;
                }

                int choice = scanner.nextInt();

                switch (choice) {
                    case 1:
                        new Booking().viewBookingHistory();
                        break;

                    case 2:
                        new Client().viewClientList();
                        break;

                    case 3:
                        viewAdmins();
                        break;

                    case 4:
                        addAdmin();
                        break;

                    case 5:
                        new Route().addRoute();
                        break;

                    case 6:
                        new Bus().addModel();
                        break;

                    case 7:
                        new Route().deleteRoute();
                        break;

                    case 8:
                        new Bus().deleteModel();
                        break;

                    case 0:
                        System.out.println("Exiting...");
                        status = false;
                        break;

                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
                new TerminalCommand().Clear();
            }while(status);
        } else {
            System.out.println("Login failed. Please try again.");
        }
    }

    private boolean Login() {
        Scanner scanner = new Scanner(System.in);
        new TerminalCommand().Clear();

        System.out.print("Enter your username: ");
        String username = scanner.nextLine();

        System.out.print("Enter your password: ");
        String password = scanner.nextLine();

        try (Connection connection = SupabaseCon.connect()) {
            if (connection != null) {
                new TerminalCommand().Clear();
                return validateCredentials(connection, username, password);
            } else {
                System.out.println("Failed to connect to the database.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean validateCredentials(Connection connection, String username, String password) throws SQLException {
        String query = "SELECT * FROM bus_admin WHERE username = ? AND password = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, password);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    System.out.println("Welcome, " + resultSet.getString("username") + "!");
                    return true;
                } else {
                    System.out.println("Invalid username or password.");
                    return false;
                }
            }
        }
    }

    private void viewAdmins() {
        try (Connection connection = SupabaseCon.connect()) {
            if (connection == null) {
                System.out.println("Failed to connect to the database.");
                return;
            }

            String query = "SELECT username, email, created_at FROM public.bus_admin";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            System.out.println("\nAdmin List:");
            System.out.printf("%-20s %-30s %-20s\n", "Name", "Email", "Created At");
            System.out.println("--------------------------------------------------------------------------------");

            while (resultSet.next()) {
                String name = resultSet.getString("username");
                String email = resultSet.getString("email");
                String createdAt = resultSet.getString("created_at");

                System.out.printf("%-20s %-30s %-20s\n", name, email, createdAt);
            }
            new TerminalCommand().waitForEnter();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("An error occurred while fetching the admin list.");
        }
    }

    private void addAdmin() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter new admin username: ");
        String username = scanner.nextLine();

        System.out.print("Enter new admin email: ");
        String email = scanner.nextLine();

        System.out.print("Enter new admin password: ");
        String password = scanner.nextLine();

        try (Connection connection = SupabaseCon.connect()) {
            if (connection == null) {
                System.out.println("Failed to connect to the database.");
                return;
            }

            String query = "INSERT INTO public.bus_admin (username, email, password, created_at) VALUES (?, ?, ?, now())";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, email);
            statement.setString(3, password);

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("New admin added successfully!");
            } else {
                System.out.println("Failed to add the new admin. Please try again.");
            }

            new TerminalCommand().waitForEnter();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("An error occurred while adding the new admin.");
        }
    }

}
