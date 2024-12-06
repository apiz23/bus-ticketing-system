import booking.Booking;
import user.Admin;
import utils.TerminalCommand;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean status = true;

        do {
            try {
                //new TerminalCommand().Clear();
                System.out.println("===== Bus Booking System Menu =====");
                System.out.println("1. Booking");
                System.out.println("2. Admin");
                System.out.println("0. Exit");
                System.out.print("Enter your choice: ");

                if (!scanner.hasNextInt()) {
                    System.out.println("Invalid input. Please enter a valid number.");
                    scanner.nextLine();
                    continue;
                }

                int choice = scanner.nextInt();
                //scanner.nextLine();

                switch (choice) {
                    case 1:
                        new Booking().Menu(scanner);
                        break;

                    case 2:
                        new Admin().Menu();
                        break;

                    case 0:
                        System.out.println("Exiting...");
                        status = false;
                        break;

                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (Exception e) {
                System.out.println("An unexpected error occurred: " + e.getMessage());
                scanner.nextLine();
            }
        } while (status);
        scanner.close();
    }
}
