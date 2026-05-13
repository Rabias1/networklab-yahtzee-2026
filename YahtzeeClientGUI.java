import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;

public class YahtzeeClientGUI {
    
    private JFrame frame;

    public YahtzeeClientGUI() {
        // Main window setup
        frame = new JFrame("Yahtzee Multiplayer");
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null); // Center the window on screen
        
        // Main panel with a soft background color
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(44, 62, 80)); // Dark blue theme
        mainPanel.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        
        // Game Title
        JLabel titleLabel = new JLabel("YAHTZEE");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(Color.WHITE);
        
        // Start Game Button
        JButton startButton = new JButton("Start Game");
        styleButton(startButton);
        
        // Rules Button
        JButton rulesButton = new JButton("Rules");
        styleButton(rulesButton);
        
        // Add action to Start Button
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startButton.setEnabled(false); // Disable button to prevent multiple clicks
                startButton.setText("Connecting...");

                // Network operations should run in a separate thread to keep UI responsive
                new Thread(() -> {
                    try {
                        String serverIp = "56.228.6.77"; // Change this to AWS IP later
                        int serverPort = 12345;
                        
                        // 1. Establish the connection
                        Socket socket = new Socket(serverIp, serverPort);
                        
                        // 2. If successful, open the board and pass the socket
                        SwingUtilities.invokeLater(() -> {
                            frame.dispose(); // Close start screen
                            YahtzeeBoardGUI gameBoard = new YahtzeeBoardGUI(socket); // Correctly passing the socket
                            gameBoard.show();
                        });
                    } catch (IOException ex) {
                        // If connection fails
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(frame, "Server not found! Start the Server first.", "Error", JOptionPane.ERROR_MESSAGE);
                            startButton.setEnabled(true);
                            startButton.setText("Start Game");
                        });
                    }
                }).start();
            }
        }); // <-- Fazladan olan '});' silindi! Sadece bu kalmali.
        
        // Add components to the panel
        gbc.gridy = 0;
        mainPanel.add(titleLabel, gbc);
        
        gbc.gridy = 1;
        mainPanel.add(startButton, gbc);
        
        gbc.gridy = 2;
        mainPanel.add(rulesButton, gbc);
        
        frame.add(mainPanel, BorderLayout.CENTER);
    }

    // Helper method to make buttons look nice
    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setBackground(new Color(236, 240, 241));
        button.setForeground(new Color(44, 62, 80));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(200, 50));
    }

    public void show() {
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        // Run the GUI
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                YahtzeeClientGUI startScreen = new YahtzeeClientGUI();
                startScreen.show();
            }
        });
    }
}