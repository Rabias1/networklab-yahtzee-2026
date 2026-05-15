import java.io.*;
import java.net.*;

/**
 * Yahtzee Game Server hosted on AWS.
 * Manages game synchronization and turn-based communication between two players.
 */
public class YahtzeeServer {
    public static void main(String[] args) {
        System.out.println("Yahtzee Server is initializing...");
        
        // Server runs on port 12345
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            
            // Outer loop allows multiple matches consecutively
            while (true) {
                System.out.println("Waiting for two players to connect on port 12345...");
                
                // Establish connection for Player 1
                Socket player1 = serverSocket.accept();
                System.out.println("Player 1 connected from: " + player1.getInetAddress());
                PrintWriter out1 = new PrintWriter(player1.getOutputStream(), true);
                BufferedReader in1 = new BufferedReader(new InputStreamReader(player1.getInputStream()));
                out1.println("WAIT_TURN"); 
                
                // Establish connection for Player 2
                Socket player2 = serverSocket.accept();
                System.out.println("Player 2 connected from: " + player2.getInetAddress());
                PrintWriter out2 = new PrintWriter(player2.getOutputStream(), true);
                BufferedReader in2 = new BufferedReader(new InputStreamReader(player2.getInputStream()));
                
                // Notify both clients that match is starting
                System.out.println("Match started: 13 rounds of Yahtzee.");
                out1.println("START");
                out2.println("START");
                
                // --- 13-ROUND GAME LOGIC ---
                for (int round = 1; round <= 13; round++) {
                    // Handle Player 1's Turn
                    out1.println("YOUR_TURN");
                    out2.println("WAIT_TURN");
                    in1.readLine(); // Synchronization: Wait for Client 1 to finish
                    
                    // Handle Player 2's Turn
                    out1.println("WAIT_TURN");
                    out2.println("YOUR_TURN");
                    in2.readLine(); // Synchronization: Wait for Client 2 to finish
                }
                
                // --- POST-GAME: SCORE CALCULATION ---
                out1.println("GAME_OVER");
                out2.println("GAME_OVER");
                
                out1.println("SEND_SCORE");
                out2.println("SEND_SCORE");
                
                int p1Score = Integer.parseInt(in1.readLine());
                int p2Score = Integer.parseInt(in2.readLine());
                
                String p1Result, p2Result;
                if (p1Score > p2Score) {
                    p1Result = "YOU WON! (You: " + p1Score + " | Opponent: " + p2Score + ")";
                    p2Result = "YOU LOST! (You: " + p2Score + " | Opponent: " + p1Score + ")";
                } else if (p2Score > p1Score) {
                    p1Result = "YOU LOST! (You: " + p1Score + " | Opponent: " + p2Score + ")";
                    p2Result = "YOU WON! (You: " + p2Score + " | Opponent: " + p1Score + ")";
                } else {
                    p1Result = "IT'S A TIE! (Both scored: " + p1Score + ")";
                    p2Result = "IT'S A TIE! (Both scored: " + p2Score + ")";
                }
                
                // Send final results and reset for next session
                out1.println("RESULT:" + p1Result);
                out2.println("RESULT:" + p2Result);
                
                System.out.println("Match concluded. Sockets released.");
                
                player1.close();
                player2.close();
            }
        } catch (IOException e) {
            System.err.println("Critical Server Error: " + e.getMessage());
        }
    }
}