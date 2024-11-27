package user;

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
}
