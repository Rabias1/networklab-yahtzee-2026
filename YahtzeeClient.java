import java.io.*;
import java.net.*;

/**
 * Basic connection client for Yahtzee project.
 * Used for verifying connection to the AWS server.
 */
public class YahtzeeClient {
    public static void main(String[] args) {
        // Mandatory AWS Public IP as per project requirements
        String serverIp = "56.228.6.77"; 
        int serverPort = 12345;
        
        try (Socket socket = new Socket(serverIp, serverPort)) {
            System.out.println("Connected to AWS Server at " + serverIp);
            System.out.println("Waiting for game synchronization...");
            
            // Connection persistence for handshake verification
            Thread.sleep(3000); 
            
        } catch (IOException | InterruptedException e) {
            System.err.println("Error: AWS Server connection failed.");
            System.err.println("Verify AWS Security Group rules for Port " + serverPort);
        }
    }
}