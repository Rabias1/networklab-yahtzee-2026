import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Random;

public class YahtzeeBoardGUI {
    private JFrame frame;
    private JToggleButton[] diceButtons;
    private JButton rollButton;
    private JButton[] scoreButtons;
    private Random random;
    private int rollCount; // Tracks the number of rolls (max 3 per turn)
    private int totalScore; // Tracks the total game score
    private JLabel totalScoreLabel;

    // Yahtzee scoring categories
    private final String[] CATEGORIES = {
        "Ones", "Twos", "Threes", "Fours", "Fives", "Sixes",
        "Three of a Kind", "Four of a Kind", "Full House",
        "Small Straight", "Large Straight", "Yahtzee", "Chance"
    };

    public YahtzeeBoardGUI() {
        random = new Random();
        rollCount = 0;
        totalScore = 0;

        // Main window setup
        frame = new JFrame("Yahtzee Game Board");
        frame.setSize(850, 650);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);

        // --- Dice Panel (Center) ---
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
                    btn.setSelected(false); // Cannot hold an unrolled die
                }
            });
            dicePanel.add(diceButtons[i]);
        }
        frame.add(dicePanel, BorderLayout.CENTER);

        // --- Control Panel (Bottom) ---
        JPanel controlPanel = new JPanel();
        controlPanel.setBackground(new Color(44, 62, 80));
        controlPanel.setPreferredSize(new Dimension(850, 80));
        
        rollButton = new JButton("Roll Dice (3 left)");
        rollButton.setFont(new Font("Arial", Font.BOLD, 24));
        rollButton.setPreferredSize(new Dimension(250, 50));
        rollButton.setBackground(new Color(236, 240, 241));
        
        rollButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (rollCount < 3) {
                    rollDice();
                    rollCount++;
                    int rollsLeft = 3 - rollCount;
                    rollButton.setText("Roll Dice (" + rollsLeft + " left)");
                    
                    if (rollCount == 3) {
                        rollButton.setEnabled(false); 
                    }
                }
            }
        });
        
        controlPanel.add(rollButton);
        frame.add(controlPanel, BorderLayout.SOUTH);

        // --- Score Panel (Right Side) ---
        JPanel scorePanel = new JPanel();
        scorePanel.setPreferredSize(new Dimension(250, 650));
        scorePanel.setBackground(new Color(236, 240, 241));
        scorePanel.setLayout(new GridLayout(CATEGORIES.length + 2, 1, 5, 5)); 
        scorePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); 
        
        JLabel scoreLabel = new JLabel("SCORECARD", SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 18));
        scoreLabel.setForeground(new Color(44, 62, 80));
        scorePanel.add(scoreLabel);

        scoreButtons = new JButton[CATEGORIES.length];
        for (int i = 0; i < CATEGORIES.length; i++) {
            final int categoryIndex = i; // Needed for the action listener
            scoreButtons[i] = new JButton(CATEGORIES[i] + " - [ ? ]");
            scoreButtons[i].setFont(new Font("Arial", Font.PLAIN, 14));
            scoreButtons[i].setFocusPainted(false);
            scoreButtons[i].setBackground(Color.WHITE);
            
            // Add action when a score category is clicked
            scoreButtons[i].addActionListener(e -> {
                if (rollCount == 0) {
                    JOptionPane.showMessageDialog(frame, "You must roll the dice first!", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // Calculate score
                int score = calculateScore(categoryIndex);
                scoreButtons[categoryIndex].setText(CATEGORIES[categoryIndex] + " - [ " + score + " ]");
                scoreButtons[categoryIndex].setEnabled(false); // Lock the category
                scoreButtons[categoryIndex].setBackground(new Color(189, 195, 199));
                
                // Update total score
                totalScore += score;
                totalScoreLabel.setText("Total Score: " + totalScore);
                
                // Reset for next turn
                resetTurn();
            });
            scorePanel.add(scoreButtons[i]);
        }

        totalScoreLabel = new JLabel("Total Score: 0", SwingConstants.CENTER);
        totalScoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalScoreLabel.setForeground(new Color(192, 57, 43)); // Red color for emphasis
        scorePanel.add(totalScoreLabel);
        
        frame.add(scorePanel, BorderLayout.EAST);
    }

    // Rolls the unselected dice
    private void rollDice() {
        for (int i = 0; i < 5; i++) {
            if (!diceButtons[i].isSelected()) {
                int result = random.nextInt(6) + 1; 
                diceButtons[i].setText(String.valueOf(result));
            }
        }
    }

    // Resets the board for the next turn
    private void resetTurn() {
        rollCount = 0;
        rollButton.setEnabled(true);
        rollButton.setText("Roll Dice (3 left)");
        for (int i = 0; i < 5; i++) {
            diceButtons[i].setSelected(false);
            diceButtons[i].setBackground(Color.WHITE);
            diceButtons[i].setText("?");
        }
    }

    // Yahtzee Scoring Logic Algorithm
    private int calculateScore(int categoryIndex) {
        int[] dice = new int[5];
        int sum = 0;
        int[] counts = new int[7]; // Index 1-6 will store the frequency of each die face
        
        for (int i = 0; i < 5; i++) {
            dice[i] = Integer.parseInt(diceButtons[i].getText());
            sum += dice[i];
            counts[dice[i]]++;
        }
        
        Arrays.sort(dice);

        // Upper Section (Ones to Sixes: index 0 to 5)
        if (categoryIndex >= 0 && categoryIndex <= 5) {
            int targetNumber = categoryIndex + 1;
            return counts[targetNumber] * targetNumber;
        }
        
        // Lower Section
        switch (categoryIndex) {
            case 6: // Three of a Kind
                for (int count : counts) if (count >= 3) return sum;
                return 0;
            case 7: // Four of a Kind
                for (int count : counts) if (count >= 4) return sum;
                return 0;
            case 8: // Full House
                boolean hasThree = false, hasTwo = false;
                for (int count : counts) {
                    if (count == 3) hasThree = true;
                    if (count == 2) hasTwo = true;
                }
                if ((hasThree && hasTwo) || (Arrays.stream(counts).anyMatch(c -> c == 5))) return 25; // 5 of a kind also counts as Full House in some rules
                return 0;
            case 9: // Small Straight (4 consecutive)
                String diceStr = Arrays.toString(dice).replaceAll("[\\[\\]\\, ]", "");
                // Remove duplicates for straight checking
                String uniqueDice = diceStr.chars().distinct().sorted().collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
                if (uniqueDice.contains("1234") || uniqueDice.contains("2345") || uniqueDice.contains("3456")) return 30;
                return 0;
            case 10: // Large Straight (5 consecutive)
                if (Arrays.equals(dice, new int[]{1, 2, 3, 4, 5}) || Arrays.equals(dice, new int[]{2, 3, 4, 5, 6})) return 40;
                return 0;
            case 11: // Yahtzee (5 of a kind)
                for (int count : counts) if (count == 5) return 50;
                return 0;
            case 12: // Chance
                return sum;
        }
        return 0;
    }

    public void show() {
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new YahtzeeBoardGUI().show();
        });
    }
}