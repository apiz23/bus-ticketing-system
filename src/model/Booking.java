package model;

public class Booking {
    private String customerName;
    private String bookingDate;
    private String bookingTime;

    public Booking(String customerName, String bookingDate, String bookingTime) {
        this.customerName = customerName;
        this.bookingDate = bookingDate;
        this.bookingTime = bookingTime;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public String getBookingTime() {
        return bookingTime;
    }

    public void displayBooking() {
        System.out.println("Customer: " + customerName);
        System.out.println("Date: " + bookingDate);
        System.out.println("Time: " + bookingTime);
    }
}

