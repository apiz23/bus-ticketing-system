package user;

import java.util.ArrayList;
import java.util.Scanner;

public class User {

    private String name, email, phoneNumber, address;
    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getAge() {
        return age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNoPhone() {
        return phoneNumber;
    }

    public void setNoPhone(String noPhone) {
        this.phoneNumber = noPhone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public User() {
    }

    public User(String name, int age, String email, String phoneNumber, String address) {
        this.name = name;
        this.age = age;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    public ArrayList<String> fillCredentials() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please fill in your details:");
        ArrayList<String> credentials = new ArrayList<>();
        String name, age, email, phoneNumber, address;

        do {
            System.out.print("Name (only letters, at least 2 characters): ");
            name = scanner.nextLine().trim();
        } while (!name.matches("[a-zA-Z\\s]{2,}"));
        credentials.add(name);

        do {
            System.out.print("Age (integer between 1 and 100): ");
            age = scanner.nextLine().trim();
        } while (!age.matches("\\d{1,3}") || Integer.parseInt(age) < 1 || Integer.parseInt(age) > 100);
        credentials.add(age);

        do {
            System.out.print("Email (valid format): ");
            email = scanner.nextLine().trim();
        } while (!email.matches("^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$"));
        credentials.add(email);

        do {
            System.out.print("Phone Number (10-15 digits, numeric): ");
            phoneNumber = scanner.nextLine().trim();
        } while (!phoneNumber.matches("\\d{10,15}"));
        credentials.add(phoneNumber);

        do {
            System.out.print("Address (non-empty): ");
            address = scanner.nextLine().trim();
        } while (address.isEmpty());
        credentials.add(address);

        return credentials;
    }
}
