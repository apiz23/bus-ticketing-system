package utils;

import java.util.Scanner;

public class TerminalCommand {

    public void Clear() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public void waitForEnter() {
        System.out.print("\nPress Enter to continue...");
        new Scanner(System.in).nextLine();
    }

    public void customText(String text) {
        System.out.println("\033[1m\033[5m" + text + "\033[0m");
    }
}
