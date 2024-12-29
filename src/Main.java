import booking.Booking;
import user.Admin;
import utils.TerminalCommand;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean status = true;
        TerminalCommand cmd = new TerminalCommand();
        do {
            try {
                cmd.Clear();
                System.out.println("==================================================");
                cmd.customText("Bus Booking");
                cmd.customText("System Menu");
                System.out.println("==================================================");
                System.out.println("1. Booking");
                System.out.println("2. Admin");
                System.out.println("0. Exit");
                System.out.print("Enter your choice: ");

                if (!scanner.hasNextInt()) {
                    System.out.println("Invalid input. Please enter a valid number.");
                    scanner.nextLine();
                    cmd.waitForEnter();
                    continue;
                }

                int choice = scanner.nextInt();

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
                        cmd.waitForEnter();
                }
            } catch (Exception e) {
                System.out.println("An unexpected error occurred: " + e.getMessage());
                scanner.nextLine();
            }
        } while (status);
        scanner.close();
    }
}
