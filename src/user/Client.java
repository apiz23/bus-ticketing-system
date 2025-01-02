package user;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import utils.SupabaseCon;
import utils.TerminalCommand;

import javax.swing.*;

public class Client extends User {
    public Client() {
        super();
    }

    public Client(String name, int age, String email, String phoneNumber, String address) {
        super(name, age, email, phoneNumber, address);
    }

    public void menu(){
        Scanner scanner = new Scanner(System.in);
        TerminalCommand cmd = new TerminalCommand();
        boolean status = true;

        do {
            System.out.println("===== Client Menu =====");
            System.out.println("1. View Clients");
            System.out.println("2. Generate report");
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
                    break;

                case 2:
                    generateClientReport();
                    break;

                case 0:
                    status = false;
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
                    break;
            }
            cmd.waitForEnter();
            cmd.Clear();
        } while (status);
    }

    @Override
    public void viewList() {
        List<Client> clients = new ArrayList<>();

        try (Connection connection = SupabaseCon.connect()) {
            if (connection == null) {
                System.out.println("Failed to connect to the database.");
                return;
            }

            String query = "SELECT name, age, email, no_phone, address FROM public.bus_booking";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                int age = resultSet.getInt("age");
                String email = resultSet.getString("email");
                String phone = resultSet.getString("no_phone");
                String address = resultSet.getString("address");

                clients.add(new Client(name, age, email, phone, address));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("An error occurred while fetching the client list.");
            return;
        }

        if (clients.isEmpty()) {
            System.out.println("No clients found.");
        } else {
            new TerminalCommand().customText("Client List");
            System.out.printf("%-20s %-5s %-30s %-15s %-20s\n",
                    "Name", "Age", "Email", "Phone", "Address");
            System.out.println("-----------------------------------------------------------------------------------");

            for (Client client : clients) {
                System.out.printf("%-20s %-5d %-30s %-15s %-20s\n",
                        client.getName(), client.getAge(), client.getEmail(), client.getNoPhone(), client.getAddress());
            }
        }
    }
    public void generateClientReport() {
        ArrayList<Client> clientList = new ArrayList<>();

        try (Connection connection = SupabaseCon.connect()) {
            if (connection == null) {
                System.out.println("Failed to connect to the database.");
                return;
            }

            String query = "SELECT name, age, email, no_phone, address FROM public.bus_booking";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                int age = resultSet.getInt("age");
                String email = resultSet.getString("email");
                String phone = resultSet.getString("no_phone");
                String address = resultSet.getString("address");

                clientList.add(new Client(name, age, email, phone, address));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("An error occurred while fetching the client data.");
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
            writer.write("Client Report\n");
            writer.write("============\n\n");

            writer.write(String.format("%-20s %-5s %-30s %-15s %-20s\n", "Name", "Age", "Email", "Phone", "Address"));
            writer.write("---------------------------------------------------------------------------------------------------\n");

            for (Client client : clientList) {
                writer.write(String.format("%-20s %-5d %-30s %-15s %-20s\n",
                        client.getName(),
                        client.getAge(),
                        client.getEmail(),
                        client.getNoPhone(),
                        client.getAddress()));
            }

            writer.write("\nTotal Number of Clients: " + clientList.size() + "\n");

            System.out.println("Report saved successfully at: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("An error occurred while saving the report.");
        }
    }
}
