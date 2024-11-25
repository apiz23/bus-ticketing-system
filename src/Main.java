import service.SupabaseCon;
import utils.ClearTerminal;
import view.BookingView;

import java.sql.Connection;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        ClearTerminal cls = new ClearTerminal();
        Connection connection = null;

        try {
            connection = SupabaseCon.connect();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        int status = 0;
        int choice;

        do {
            System.out.println("===== Bus Booking System Menu =====");
            System.out.println("1. Booking");
            System.out.println("2. Admin");
            System.out.println("0. Exit");
            System.out.print("Enter your choice: ");

            choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    cls.Clear();
                    BookingView bookV = new BookingView();
                    bookV.menu();
                    break;
                case 2:
                    break;
                case 0:
                    System.out.println("Exiting...");
                    status = 1;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        } while (status == 0);

    }
}
