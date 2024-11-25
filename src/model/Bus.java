package model;

public class Bus {
    private String busNumber;
    private String driverName;
    private int capacity;

    public Bus(String busNumber, String driverName, int capacity) {
        this.busNumber = busNumber;
        this.driverName = driverName;
        this.capacity = capacity;
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
}
