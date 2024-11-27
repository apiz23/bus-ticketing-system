package bus;

import java.util.List;

public class Bus {
    private String busNumber;
    private String driverName;
    private int capacity;

    public Bus(){

    }

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

    //Economy
    public void generateEconomyLayout(int capacity, String[] seats) {
        for (int i = 1; i <= capacity; i++) {
            if (List.of(seats).contains(String.valueOf(i))) {
                System.out.print("[X] ");
            } else {
                System.out.print("[ ] ");
            }
            if (i % 4 == 0) {
                System.out.println();
            } else if (i % 2 == 0) {
                System.out.print("   ");
            }
        }
    }

    // Executive
    public void generateExecutiveLayout(int capacity, String[] seats) {
        for (int i = 1; i <= capacity; i++) {
            if (List.of(seats).contains(String.valueOf(i))) {
                System.out.print("[X] ");
            } else {
                System.out.print("[ ] ");
            }
            if (i % 3 == 0) {
                System.out.println();
            } else {
                System.out.print("   ");
            }
        }
    }

    // Double Decker
    public void generateDoubleDeckerLayout(int capacity, String[] seats) {
        int lowerDeckCapacity = Math.min(6, capacity);
        int upperDeckStart = lowerDeckCapacity + 1;

        System.out.println("Lower Deck:");
        for (int i = 1; i <= lowerDeckCapacity; i++) {
            if (List.of(seats).contains(String.valueOf(i))) {
                System.out.print("[X] ");
            } else {
                System.out.print("[ ] ");
            }
            if (i % 3 == 0) {
                System.out.println();
            }
        }

        System.out.println("\nUpper Deck:");
        for (int i = upperDeckStart; i <= capacity; i++) {
            if (List.of(seats).contains(String.valueOf(i))) {
                System.out.print("[X] ");
            } else {
                System.out.print("[ ] ");
            }
            if ((i - upperDeckStart + 1) % 4 == 0) {
                System.out.println();
            } else if ((i - upperDeckStart + 1) % 2 == 0) {
                System.out.print("   ");
            }
        }
    }
}
