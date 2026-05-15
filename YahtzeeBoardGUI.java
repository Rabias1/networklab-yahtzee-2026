import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Random;

public class YahtzeeBoardGUI {
    private JFrame frame;
    private JToggleButton[] diceButtons;
    private JButton rollButton;
    private JButton[] scoreButtons;
    private Random random;
    
    private int rollCount;      
    private int totalScore;     
    private JLabel totalScoreLabel;
    
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private final String[] CATEGORIES = {
        "Ones", "Twos", "Threes", "Fours", "Fives", "Sixes",
        "Three of a Kind", "Four of a Kind", "Full House",
        "Small Straight", "Large Straight", "Yahtzee", "Chance"
    };

    public YahtzeeBoardGUI(Socket socket) {
        this.socket = socket;
        random = new Random();
        rollCount = 0;
        totalScore = 0;

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // GUI Setup
        frame = new JFrame("Yahtzee Game Board");
        frame.setSize(850, 650);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);

        JPanel dicePanel = new JPanel();
        dicePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 200));
        dicePanel.setBackground(new Color(39, 174, 96)); 

        diceButtons = new JToggleButton[5];
        for (int i = 0; i < 5; i++) {
            diceButtons[i] = new JToggleButton("?");
            diceButtons[i].setFont(new Font("Arial", Font.BOLD, 40));
            diceButtons[i].setPreferredSize(new Dimension(80, 80));
            diceButtons[i].setBackground(Color.WHITE);
            diceButtons[i].setFocusPainted(false);
            
            diceButtons[i].addItemListener(e -> {
                JToggleButton btn = (JToggleButton) e.getItem();
                if (btn.isSelected() && !btn.getText().equals("?")) {
                    btn.setBackground(Color.LIGHT_GRAY); 
                } else {
                    btn.setBackground(Color.WHITE); 
                    btn.setSelected(false); 
                }
            });
            dicePanel.add(diceButtons[i]);
        }
        frame.add(dicePanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        controlPanel.setBackground(new Color(44, 62, 80));
        controlPanel.setPreferredSize(new Dimension(850, 80));
        
        rollButton = new JButton("Waiting for server...");
        rollButton.setFont(new Font("Arial", Font.BOLD, 24));
        rollButton.setPreferredSize(new Dimension(300, 50));
        rollButton.setBackground(new Color(236, 240, 241));
        rollButton.setEnabled(false); 
        
        rollButton.addActionListener(e -> {
            if (rollCount < 3) {
                rollDice();
                rollCount++;
                int rollsLeft = 3 - rollCount;
                rollButton.setText("Roll Dice (" + rollsLeft + " left)");
                if (rollCount == 3) rollButton.setEnabled(false); 
            }
        });
        
        controlPanel.add(rollButton);
        frame.add(controlPanel, BorderLayout.SOUTH);

        JPanel scorePanel = new JPanel();
        scorePanel.setPreferredSize(new Dimension(250, 650));
        scorePanel.setBackground(new Color(236, 240, 241));
        scorePanel.setLayout(new GridLayout(CATEGORIES.length + 2, 1, 5, 5)); 
        scorePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); 
        
        JLabel scoreLabel = new JLabel("SCORECARD", SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 18));
        scorePanel.add(scoreLabel);

        scoreButtons = new JButton[CATEGORIES.length];
        for (int i = 0; i < CATEGORIES.length; i++) {
            final int index = i; 
            scoreButtons[i] = new JButton(CATEGORIES[i] + " - [ ? ]");
            scoreButtons[i].setFont(new Font("Arial", Font.PLAIN, 14));
            scoreButtons[i].setBackground(Color.WHITE);
            
            scoreButtons[i].addActionListener(e -> {
                if (rollCount == 0) {
                    JOptionPane.showMessageDialog(frame, "Roll the dice first!");
                    return;
                }
                int score = calculateScore(index);
                scoreButtons[index].setText(CATEGORIES[index] + " - [ " + score + " ]");
                scoreButtons[index].setEnabled(false); 
                totalScore += score;
                totalScoreLabel.setText("Total Score: " + totalScore);
                resetTurn();
                if (out != null) out.println("TURN_FINISHED"); 
            });
            scorePanel.add(scoreButtons[i]);
        }

        totalScoreLabel = new JLabel("Total Score: 0", SwingConstants.CENTER);
        totalScoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
        scorePanel.add(totalScoreLabel);
        frame.add(scorePanel, BorderLayout.EAST);

        startListening();
    }

    private void startListening() {
        new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    final String msg = message;
                    SwingUtilities.invokeLater(() -> handleMessage(msg));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void handleMessage(String message) {
        if (message.equals("YOUR_TURN")) {
            rollButton.setEnabled(true);
            rollButton.setText("Roll Dice (3 left)");
        } else if (message.equals("WAIT_TURN")) {
            rollButton.setEnabled(false);
            rollButton.setText("Opponent's turn...");
        } else if (message.equals("SEND_SCORE")) {
            out.println(totalScore); 
        } else if (message.startsWith("RESULT:")) {
            JOptionPane.showMessageDialog(frame, message.substring(7));
            frame.dispose();
            new YahtzeeClientGUI().show(); // Replay functionality [cite: 50]
        }
    }

    private void rollDice() {
        for (int i = 0; i < 5; i++) {
            if (!diceButtons[i].isSelected()) {
                diceButtons[i].setText(String.valueOf(random.nextInt(6) + 1));
            }
        }
    }

    private void resetTurn() {
        rollCount = 0;
        for (JToggleButton btn : diceButtons) {
            btn.setSelected(false);
            btn.setBackground(Color.WHITE);
            btn.setText("?");
        }
    }

    private int calculateScore(int index) {
        int[] dice = new int[5];
        int sum = 0;
        int[] counts = new int[7]; 
        for (int i = 0; i < 5; i++) {
            dice[i] = Integer.parseInt(diceButtons[i].getText());
            sum += dice[i];
            counts[dice[i]]++;
        }
        Arrays.sort(dice);

        if (index <= 5) return counts[index + 1] * (index + 1);
        
        switch (index) {
            case 6: for (int c : counts) if (c >= 3) return sum; break;
            case 7: for (int c : counts) if (c >= 4) return sum; break;
            case 8: 
                boolean h3 = false, h2 = false;
                for (int c : counts) { if (c == 3) h3 = true; if (c == 2) h2 = true; }
                if (h3 && h2) return 25; break;
            case 9: 
                String u = "";
                for(int d : dice) if(!u.contains(String.valueOf(d))) u += d;
                if (u.contains("1234") || u.contains("2345") || u.contains("3456")) return 30; break;
            case 10:
                String u2 = "";
                for(int d : dice) if(!u2.contains(String.valueOf(d))) u2 += d;
                if (u2.contains("12345") || u2.contains("23456")) return 40; break;
            case 11: for (int c : counts) if (c == 5) return 50; break;
            case 12: return sum;
        }
        return 0;
    }

    public void show() { frame.setVisible(true); }
}