import java.io.*;
import java.net.*;

public class YahtzeeClient {
    public static void main(String[] args) {
        // AWS server IP address will be placed here in the future
        String serverIp = "localhost"; 
        int serverPort = 12345;
        
        try (Socket socket = new Socket(serverIp, serverPort)) {
            System.out.println("Successfully connected to the server! Waiting for the game to start...");
            
            // Short delay to prevent the program from closing immediately (won't be needed once GUI is added)
            Thread.sleep(5000); 
            
        } catch (IOException | InterruptedException e) {
            System.out.println("Failed to connect to the server. Make sure the server is running.");
            e.printStackTrace();
        }
    }
}