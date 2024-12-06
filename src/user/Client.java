package user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import utils.SupabaseCon;
import utils.TerminalCommand;

public class Client extends User {
    public Client() {
        super();
    }

    public Client(String name, int age, String email, String phoneNumber, String address) {
        super(name, age, email, phoneNumber, address);
    }

    public void viewBookingDetails() {
        System.out.println("Viewing booking details...");
    }

    public void viewClientList() {
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
            System.out.println("\nClient List:");
            System.out.printf("%-20s %-5s %-20s %-15s %-20s\n",
                    "Name", "Age", "Email", "Phone", "Address");
            System.out.println("-----------------------------------------------------------------------------------");

            for (Client client : clients) {
                System.out.printf("%-20s %-5d %-20s %-15s %-20s\n",
                        client.getName(), client.getAge(), client.getEmail(), client.getNoPhone(), client.getAddress());
            }
        }

        new TerminalCommand().waitForEnter();
    }
}
