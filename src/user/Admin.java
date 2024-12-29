package user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

import booking.Booking;
import bus.Bus;
import route.Route;
import utils.SupabaseCon;
import utils.TerminalCommand;

public class Admin extends User {

    private String username;
    private String email;
    private String password;
    private String createdAt;

    public Admin(){}

    public Admin(String username, String email, String password, String createdAt) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.createdAt = createdAt;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getCreatedAt(){
        return createdAt;
    }

    public void Menu() {
        if (Login()) {
            Scanner scanner = new Scanner(System.in);
            boolean status = true;
            do {
                System.out.println("""
                ===== Admin Menu =====
                1. View Booking History
                2. View Clients
                3. View Admins
                4. View Bus Routes
                5. View Bus Models
                6. Add Admin
                7. Add Bus Route
                8. Add Bus Model
                9. Remove Admin
                10. Remove Bus Route
                11. Remove Bus Model
                0. Main Menu
                
                Enter your choice:\s""");

                if (!scanner.hasNextInt()) {
                    System.out.println("Invalid input. Please enter a valid number.");
                    scanner.nextLine();
                    continue;
                }

                int choice = scanner.nextInt();

                Bus bus = new Bus();
                Route route = new Route();

                new TerminalCommand().Clear();
                switch (choice) {
                    case 1 -> new Booking().viewBookingHistory();
                    case 2 -> new Client().viewList();
                    case 3 -> viewList();
                    case 4 -> route.viewRoutes();
                    case 5 -> bus.viewBusModels();
                    case 6 -> addAdmin();
                    case 7 -> route.addRoute(scanner);
                    case 8 -> bus.addBusModel(scanner);
                    case 9 -> removeAdmin();
                    case 10 -> route.deleteRoute(scanner);
                    case 11 -> bus.deleteBusModel(scanner);
                    case 0 -> {
                        System.out.println("Exiting...");
                        status = false;
                    }
                    default -> System.out.println("Invalid choice. Please try again.");
                }

                new TerminalCommand().Clear();

            } while (status);

        } else {
            System.out.println("Login failed. Please try again.");
            new TerminalCommand().waitForEnter();
        }
    }

    private boolean Login() {
        Scanner scanner = new Scanner(System.in);
        boolean isAuthenticated = false;

        while (!isAuthenticated) {
            new TerminalCommand().Clear();

            System.out.print("Enter your username: ");
            String username = scanner.nextLine();

            System.out.print("Enter your password: ");
            String password = scanner.nextLine();

            try (Connection connection = SupabaseCon.connect()) {
                if (connection != null) {
                    new TerminalCommand().Clear();
                    isAuthenticated = validateCredentials(connection, username, password);
                    if (!isAuthenticated) {
                        System.out.println("Invalid credentials. Please try again.");
                        new TerminalCommand().waitForEnter();
                    }
                } else {
                    System.out.println("Failed to connect to the database.");
                    return false;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }


    private boolean validateCredentials(Connection connection, String username, String password) throws SQLException {
        String query = "SELECT * FROM bus_admin WHERE username = ? AND password = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, password);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    new TerminalCommand().customText("Welcome, " + resultSet.getString("username") + "!");
                    return true;
                } else {
                    System.out.println("Invalid username or password.");
                    return false;
                }
            }
        }
    }

    public void viewList() {
        ArrayList<Admin> adminList = new ArrayList<>();

        try (Connection connection = SupabaseCon.connect()) {
            if (connection == null) {
                System.out.println("Failed to connect to the database.");
                return;
            }

            String query = "SELECT username, email, password, created_at FROM public.bus_admin";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String name = resultSet.getString("username");
                String email = resultSet.getString("email");
                String password = resultSet.getString("password");
                String createdAt = resultSet.getString("created_at");

                adminList.add(new Admin(name, email, password, createdAt));
            }

            new TerminalCommand().customText("Admin List");
            System.out.printf("%-20s %-30s %-20s %-30s\n", "Name", "Email", "Password", "Created At");
            System.out.println("--------------------------------------------------------------------------------------------------------");

            for (Admin admin : adminList) {
                System.out.printf("%-20s %-30s %-20s %-30s\n", admin.getUsername(), admin.getEmail(), admin.getPassword(), admin.getCreatedAt());
            }

            new TerminalCommand().waitForEnter();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("An error occurred while fetching the admin list.");
        }
    }

    private void addAdmin() {
        Scanner scanner = new Scanner(System.in);

        new TerminalCommand().customText("Add Admin");
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

    private void removeAdmin() {
        Scanner scanner = new Scanner(System.in);

        viewList();
        new TerminalCommand().customText("Remove Admin");
        System.out.print("Enter the admin username to remove: ");
        String identifier = scanner.nextLine();

        try (Connection connection = SupabaseCon.connect()) {
            if (connection == null) {
                System.out.println("Failed to connect to the database.");
                return;
            }

            String query = "DELETE FROM public.bus_admin WHERE username = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, identifier);
            statement.setString(2, identifier);

            int rowsDeleted = statement.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Admin removed successfully!");
            } else {
                System.out.println("No admin found with the provided username or email.");
            }

            new TerminalCommand().waitForEnter();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("An error occurred while removing the admin.");
        }
    }
}
