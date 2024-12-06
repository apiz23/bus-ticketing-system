package utils;

import java.util.Scanner;

public class TerminalCommand {
    public void Clear(){
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public void waitForEnter(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
}

