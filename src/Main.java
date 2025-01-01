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

                cmd.customText("Bus Booking");
                cmd.customText("System Menu");
                System.out.println("==============");
                System.out.println("Main Menu");
                System.out.println("==============");
                System.out.println("1. Booking");
                System.out.println("2. Management Login");
                System.out.println("3. Check your history");
                System.out.println("0. Exit");
                System.out.print("Enter your choice: ");

                if (!scanner.hasNextInt()) {
                    System.out.println("Invalid input. Please enter a valid number.");
                    scanner.nextLine();
                    cmd.waitForEnter();
                    continue;
                }

                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1:
                        new Booking().menu(scanner);
                        break;

                    case 2:
                        new Admin().mainMenu();
                        break;

                    case 3:
                        cmd.Clear();
                        System.out.print("Enter Your Email: ");
                        String userEmail = scanner.nextLine().trim();
                        if (!userEmail.isEmpty()) {
                            new Booking().viewBookingHistory(userEmail);
                        } else {
                            System.out.println("Email cannot be empty. Please try again.");
                        }
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
