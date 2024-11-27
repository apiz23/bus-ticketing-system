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

        System.out.print("Name: ");
        credentials.add(scanner.nextLine());

        System.out.print("Age: ");
        credentials.add(scanner.nextLine());

        System.out.print("Email: ");
        credentials.add(scanner.nextLine());

        System.out.print("Phone Number: ");
        credentials.add(scanner.nextLine());

        System.out.print("Address: ");
        credentials.add(scanner.nextLine());

        return credentials;
    }
}
