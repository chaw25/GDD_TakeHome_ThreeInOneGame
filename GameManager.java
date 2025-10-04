import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameManager {
    private JFrame mainFrame;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private GameState currentGameState = GameState.MAIN_MENU;
    
    // Game instances
    private MadDriverGame madDriverGame;
    private FlyLikeBirdGame flyLikeBirdGame;
    private SumoBallGame sumoBallGame;
    private GameType currentGameType;
    
    public GameManager() {
        initializeGames();
        setupMainFrame();
    }
    
    private void initializeGames() {
        madDriverGame = new MadDriverGame(this);
        flyLikeBirdGame = new FlyLikeBirdGame(this);
        sumoBallGame = new SumoBallGame(this);
    }
    
    private void setupMainFrame() {
        mainFrame = new JFrame("Three-in-One Game Suite with Chaos");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(800, 600);
        mainFrame.setResizable(false);
        mainFrame.setLocationRelativeTo(null);
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        // Add all panels to card layout
        mainPanel.add(createMainMenu(), "MAIN_MENU");
        mainPanel.add(madDriverGame.getGamePanel(), "MAD_DRIVER");
        mainPanel.add(flyLikeBirdGame.getGamePanel(), "FLY_BIRD");
        mainPanel.add(sumoBallGame.getGamePanel(), "SUMO_BALL");
        
        mainFrame.add(mainPanel);
    }
    
    public void showMainMenu() {
        cardLayout.show(mainPanel, "MAIN_MENU");
        currentGameState = GameState.MAIN_MENU;
        mainFrame.setVisible(true);
        
        // Stop all games when returning to main menu
        madDriverGame.stopGame();
        flyLikeBirdGame.stopGame();
        sumoBallGame.stopGame();
    }
    
    public void startGame(GameType gameType) {
        currentGameType = gameType;
        switch (gameType) {
            case MAD_DRIVER:
                cardLayout.show(mainPanel, "MAD_DRIVER");
                madDriverGame.startGame();
                // Request focus after showing the panel
                SwingUtilities.invokeLater(() -> {
                    madDriverGame.getGamePanel().requestFocusInWindow();
                });
                break;
            case FLY_BIRD:
                cardLayout.show(mainPanel, "FLY_BIRD");
                flyLikeBirdGame.startGame();
                SwingUtilities.invokeLater(() -> {
                    flyLikeBirdGame.getGamePanel().requestFocusInWindow();
                });
                break;
            case SUMO_BALL:
                cardLayout.show(mainPanel, "SUMO_BALL");
                sumoBallGame.startGame();
                SwingUtilities.invokeLater(() -> {
                    sumoBallGame.getGamePanel().requestFocusInWindow();
                });
                break;
        }
        currentGameState = GameState.IN_GAME;
    }
    
    public void showInGameMenu() {
        if (currentGameState != GameState.IN_GAME) return;
        
        currentGameState = GameState.PAUSED;
        
        // Pause the current game
        switch (currentGameType) {
            case MAD_DRIVER: madDriverGame.pauseGame(); break;
            case FLY_BIRD: flyLikeBirdGame.pauseGame(); break;
            case SUMO_BALL: sumoBallGame.pauseGame(); break;
        }
        
        InGameMenu menu = new InGameMenu(this);
        menu.showDialog(mainFrame);
    }
    
    public void resumeGame() {
        currentGameState = GameState.IN_GAME;
        
        // Resume the current game
        switch (currentGameType) {
            case MAD_DRIVER: madDriverGame.resumeGame(); break;
            case FLY_BIRD: flyLikeBirdGame.resumeGame(); break;
            case SUMO_BALL: sumoBallGame.resumeGame(); break;
        }
    }
    
    public void restartCurrentGame() {
        switch (currentGameType) {
            case MAD_DRIVER: 
                madDriverGame.stopGame();
                madDriverGame.startGame();
                break;
            case FLY_BIRD:
                flyLikeBirdGame.stopGame();
                flyLikeBirdGame.startGame();
                break;
            case SUMO_BALL:
                sumoBallGame.stopGame();
                sumoBallGame.startGame();
                break;
        }
        currentGameState = GameState.IN_GAME;
    }
    
    public void returnToMainMenu() {
        showMainMenu();
    }
    
    private JPanel createMainMenu() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw background
                GradientPaint gradient = new GradientPaint(0, 0, new Color(30, 30, 50), 
                                                          getWidth(), getHeight(), new Color(20, 20, 40));
                g2.setPaint(gradient);
                g2.fillRect(0, 0, getWidth(), getHeight());
                
                // Draw title
                g2.setColor(Color.YELLOW);
                g2.setFont(new Font("Arial", Font.BOLD, 48));
                String title = "Three-in-One Game Suite";
                int titleWidth = g2.getFontMetrics().stringWidth(title);
                g2.drawString(title, (getWidth() - titleWidth) / 2, 100);
                
                // Draw stars
                g2.setColor(Color.WHITE);
                for (int i = 0; i < 50; i++) {
                    int x = (int)(Math.random() * getWidth());
                    int y = (int)(Math.random() * getHeight());
                    g2.fillOval(x, y, 2, 2);
                }
                
                // Draw creator name
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.ITALIC, 16));
                g2.drawString("Created by: Aye Nyein Moe (6530089)", 20, getHeight() - 20);
            }
        };
        panel.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 50, 10, 50);
        
        // Create buttons
        String[] gameNames = {"Mad Driver", "Fly Like a Bird", "I'm a Sumo and a Ball", "Exit"};
        for (int i = 0; i < gameNames.length; i++) {
            JButton button = createMenuButton(gameNames[i]);
            final int gameIndex = i;
            
            button.addActionListener(e -> {
                switch (gameIndex) {
                    case 0: startGame(GameType.MAD_DRIVER); break;
                    case 1: startGame(GameType.FLY_BIRD); break;
                    case 2: startGame(GameType.SUMO_BALL); break;
                    case 3: System.exit(0); break;
                }
            });
            
            gbc.gridy = i + 1;
            panel.add(button, gbc);
        }
        
        return panel;
    }
    
    private JButton createMenuButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isRollover()) {
                    GradientPaint gp = new GradientPaint(0, 0, new Color(100, 100, 180), 
                                                       0, getHeight(), new Color(70, 70, 150));
                    g2.setPaint(gp);
                } else {
                    GradientPaint gp = new GradientPaint(0, 0, new Color(70, 70, 120), 
                                                       0, getHeight(), new Color(50, 50, 100));
                    g2.setPaint(gp);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                
                // Draw border
                g2.setColor(new Color(255, 255, 255, 100));
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 25, 25);
                
                // Draw text
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 20));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 2;
                g2.drawString(getText(), x, y);
            }
        };
        
        button.setPreferredSize(new Dimension(300, 60));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        
        return button;
    }
}

enum GameState {
    MAIN_MENU, IN_GAME, PAUSED
}

enum GameType {
    MAD_DRIVER, FLY_BIRD, SUMO_BALL
}