import java.io.*;
import java.net.*;

public class YahtzeeServer {
    public static void main(String[] args) {
        System.out.println("Server is starting...");
        
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            // The outer loop allows the server to host new games after one finished
            while (true) {
                System.out.println("Server is listening on port 12345... Waiting for two players.");
                
                // Connect Player 1
                Socket player1 = serverSocket.accept();
                System.out.println("Player 1 connected.");
                PrintWriter out1 = new PrintWriter(player1.getOutputStream(), true);
                BufferedReader in1 = new BufferedReader(new InputStreamReader(player1.getInputStream()));
                out1.println("WAIT"); // Standard procedure
                
                // Connect Player 2
                Socket player2 = serverSocket.accept();
                System.out.println("Player 2 connected.");
                PrintWriter out2 = new PrintWriter(player2.getOutputStream(), true);
                BufferedReader in2 = new BufferedReader(new InputStreamReader(player2.getInputStream()));
                
                System.out.println("Both players connected. Starting a new 13-round match!");
                out1.println("START");
                out2.println("START");
                
                // --- 13-ROUND GAME LOOP ---
                for (int round = 1; round <= 13; round++) {
                    // Player 1 Turn
                    out1.println("YOUR_TURN");
                    out2.println("WAIT_TURN");
                    in1.readLine(); // Wait for TURN_FINISHED
                    
                    // Player 2 Turn
                    out1.println("WAIT_TURN");
                    out2.println("YOUR_TURN");
                    in2.readLine(); // Wait for TURN_FINISHED
                }
                
                // --- END OF GAME: CALCULATE AND SEND RESULTS ---
                out1.println("GAME_OVER");
                out2.println("GAME_OVER");
                
                out1.println("SEND_SCORE");
                out2.println("SEND_SCORE");
                
                int p1Score = Integer.parseInt(in1.readLine());
                int p2Score = Integer.parseInt(in2.readLine());
                
                String p1Result, p2Result;
                if (p1Score > p2Score) {
                    p1Result = "YOU WON! (Your Score: " + p1Score + " | Opponent: " + p2Score + ")";
                    p2Result = "YOU LOST! (Your Score: " + p2Score + " | Opponent: " + p1Score + ")";
                } else if (p2Score > p1Score) {
                    p1Result = "YOU LOST! (Your Score: " + p1Score + " | Opponent: " + p2Score + ")";
                    p2Result = "YOU WON! (Your Score: " + p2Score + " | Opponent: " + p1Score + ")";
                } else {
                    p1Result = "IT'S A TIE! (Score: " + p1Score + ")";
                    p2Result = "IT'S A TIE! (Score: " + p2Score + ")";
                }
                
                out1.println("RESULT:" + p1Result);
                out2.println("RESULT:" + p2Result);
                
                System.out.println("Match finished. Results sent. Waiting for new players...");
                
                // Close current player sockets to free resources
                player1.close();
                player2.close();
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }
}