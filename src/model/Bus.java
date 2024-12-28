package model;

import utils.SupabaseCon;
import utils.TerminalCommand;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Bus {
    private String busNumber;
    private String driverName;
    private int capacity;

    public Bus(String busNumber, String driverName, int capacity) {
        this.busNumber = busNumber;
        this.driverName = driverName;
        this.capacity = capacity;
    }

    public Bus() {

    }

    public String getBusNumber() {
        return busNumber;
    }

    public String getDriverName() {
        return driverName;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void displayDetails() {
        System.out.println("Bus Number: " + busNumber + ", Driver: " + driverName + ", Capacity: " + capacity);
    }

    public void addModel(){
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter brand: ");
        String brand = scanner.nextLine();

        System.out.print("Enter type: ");
        String type = scanner.nextLine();

        System.out.print("Enter capacity: ");
        String capacity = scanner.nextLine();

        try (Connection connection = SupabaseCon.connect()) {
            if (connection == null) {
                System.out.println("Failed to connect to the database.");
                return;
            }

            String query = "INSERT INTO public.bus_model (brand, type, capacity, created_at) VALUES (?, ?, ?, now())";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, brand);
            statement.setString(2, type);
            statement.setString(3, capacity);

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("New bus model added successfully!");
            } else {
                System.out.println("Failed to add the new bus model. Please try again.");
            }

            new TerminalCommand().waitForEnter();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("An error occurred while adding the new bus model.");
        }
    }

    public void deleteModel(){
        Scanner scanner = new Scanner(System.in);
        try (Connection connection = SupabaseCon.connect()) {
            if (connection == null) {
                System.out.println("Failed to connect to the database.");
                return;
            }
            String query = "SELECT * FROM public.bus_model";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                System.out.println("No any bus model found in the database.");
                return;
            }
            do {
                int bus_id =resultSet.getInt("bus_id");
                String brand = resultSet.getString("brand");
                String type = resultSet.getString("type");
                String capacity = resultSet.getString("capacity");
                System.out.println(bus_id+"\t"+brand + "\t" + type + "\t" + capacity);
            } while (resultSet.next());
            int[] bus_model=new int[100];
            int count=0,cont=1;
            do{
                System.out.println("Enter Bus Model No to delete: ");
                bus_model[count] = scanner.nextInt();
                scanner.nextLine();
                System.out.println("Is there anymore bus model to delete? (Y=Yes, N=No) ");
                String choice= scanner.nextLine();
                if(choice.toLowerCase().equals("y")){
                    count++;
                }
                else{
                    cont= 0;
                }
            }while(cont==1);
            StringBuilder queryBuilder = new StringBuilder("DELETE FROM bus_model WHERE bus_id IN (");
            for (int i = 0; i <=count; i++) {
                queryBuilder.append("?");
                if (i < count) {
                    queryBuilder.append(",");  // Add a comma between placeholders
                }
            }
            queryBuilder.append(")");
            String query1 = queryBuilder.toString();
            statement = connection.prepareStatement(query1);
            for (int i = 0; i <= count; i++) {
                statement.setInt( i+1 , bus_model[i]);
            }
            int rowsDeleted = statement.executeUpdate();
            System.out.println(rowsDeleted);
            if (rowsDeleted != count) {
                System.out.println(rowsDeleted);
                System.out.println("Bus model deleted successfully!");
            } else {
                System.out.println("Failed to deleted the bus model. Please try again.");
            }

            new TerminalCommand().waitForEnter();
        } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("An error occurred while deleting the bus model.");
        }
    }
}
