package view;

import controller.BookingController;
import service.SupabaseCon;

import java.sql.*;
import java.util.Scanner;

public class BookingView {

    private BookingController bookingController;

    public BookingView() {
        bookingController = new BookingController();
    }

    public void menu() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter From Where:");
        String startStation = scanner.nextLine();

        System.out.println("Enter To Where:");
        String endStation = scanner.nextLine();

        System.out.println("Enter Start Date (DD-MM-YYYY):");
        String startDate = scanner.nextLine();

        System.out.println("Enter Return Date (DD-MM-YYYY) or leave empty for no return date:");
        String endDate = scanner.nextLine();

        System.out.println("\nBooking Details:\nFrom: " + startStation + "\nTo: " + endStation + "\nStart Date: " + startDate);

        if (!endDate.isEmpty()) {
            System.out.println("End Date: " + endDate);
            bookingController.filterRoutes(startStation, endStation);
        } else {
            System.out.println("End Date: Not provided");
        }
    }
}
