package user;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

import javax.swing.*;

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

    public void mainMenu() {

        if (Login()) {
            Scanner scanner = new Scanner(System.in);
            Bus bus = new Bus();
            Route route = new Route();
            TerminalCommand cmd = new TerminalCommand();
            boolean status = true;

            do {
                System.out.println("===== Management Menu =====");
                System.out.println("1. Booking");
                System.out.println("2. Client");
                System.out.println("3. Route");
                System.out.println("4. Bus");
                System.out.println("5. Admin");
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
                        new Booking().menu();
                        break;

                    case 2:
                        new Client().menu();
                        break;

                    case 3:
                        route.menu();
                        break;

                    case 4:
                        bus.menu();
                        break;

                    case 5:
                        menu();
                        break;

                    case 0:
                        System.out.println("Returning to Main Menu...");
                        status = false;
                        break;

                    default:
                        System.out.println("Invalid choice. Please try again.");
                        cmd.waitForEnter();
                }
                cmd.Clear();
            } while (status);


        } else {
            System.out.println("Login failed. Please try again.");
            new TerminalCommand().waitForEnter();
        }
    }

    public boolean Login() {
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

    public void menu(){
        Scanner scanner = new Scanner(System.in);
        TerminalCommand cmd = new TerminalCommand();
        boolean status = true;

        do {
            System.out.println("===== Admin Menu =====");
            System.out.println("1. View Admin");
            System.out.println("2. Add a New Admin");
            System.out.println("3. Remove a Admin");
            System.out.println("4. Generate Report");
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
                    viewList();
                    cmd.waitForEnter();
                    break;

                case 2:
                    addAdmin();
                    cmd.waitForEnter();
                    break;

                case 3:
                    deleteAdmin();
                    cmd.waitForEnter();
                    break;

                case 4:
                    generateReport();
                    cmd.waitForEnter();
                    break;

                case 0:
                    System.out.println("Returning to Main Menu...");
                    status = false;
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
                    cmd.waitForEnter();
            }
            cmd.Clear();
        } while (status);
    }

    @Override
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

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("An error occurred while fetching the admin list.");
        }
    }

    private void addAdmin() {
        Scanner scanner = new Scanner(System.in);

        new TerminalCommand().customText("Add Admin");

        while (true) {
            System.out.print("Enter new admin username: ");
            username = scanner.nextLine().trim();
            if (username.isEmpty()) {
                System.out.println("Username cannot be empty. Please try again.");
            } else {
                break;
            }
        }

        while (true) {
            System.out.print("Enter new admin email: ");
            email = scanner.nextLine().trim();

            if (!email.matches("^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$")) {
                System.out.println("Invalid email format. Please enter a valid email.");
            } else {
                break;
            }
        }


        while (true) {
            System.out.print("Enter new admin password: ");
            password = scanner.nextLine();
            if (password.length() < 6) {
                System.out.println("Password must be at least 6 characters long. Please try again.");
            } else {
                break;
            }
        }

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
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("An error occurred while adding the new admin.");
        }
    }

    private void deleteAdmin() {
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

            int rowsDeleted = statement.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Admin removed successfully!");
            } else {
                System.out.println("No admin found with the provided username.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("An error occurred while removing the admin.");
        }
    }

    public void generateReport() {
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
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("An error occurred while fetching the admin data.");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Report");
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
            writer.write("Admin Report\n");
            writer.write("============\n\n");

            writer.write(String.format("%-20s %-30s %-20s %-30s\n", "Name", "Email", "Password", "Created At"));
            writer.write("--------------------------------------------------------------------------------------------------------\n");

            for (Admin admin : adminList) {
                writer.write(String.format("%-20s %-30s %-20s %-30s\n",
                        admin.getUsername(),
                        admin.getEmail(),
                        admin.getPassword(),
                        admin.getCreatedAt()));
            }

            writer.write("\nTotal Number of Admins: " + adminList.size() + "\n");

            System.out.println("Report saved successfully at: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("An error occurred while saving the report.");
        }
    }
}
