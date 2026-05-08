import java.io.*;
import java.net.*;

public class YahtzeeServer {
    public static void main(String[] args) {
        System.out.println("Server is starting...");
        
        // Server starts listening on port 12345
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Server is listening on port 12345... Waiting for players.");
            
            // Wait for Player 1 to connect
            Socket player1 = serverSocket.accept();
            System.out.println("Player 1 connected: " + player1.getInetAddress());
            
            // Wait for Player 2 to connect
            Socket player2 = serverSocket.accept();
            System.out.println("Player 2 connected: " + player2.getInetAddress());
            
            System.out.println("Both players connected. The game is ready to start!");
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}