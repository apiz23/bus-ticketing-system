package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class TerminalCommand {
    public void Clear(){
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public void waitForEnter(){
        Scanner scanner = new Scanner(System.in);
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }

    public void customText(String text) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", "figlet", "-f", "small", text);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

