import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MadDriverGame {
    private GameManager gameManager;
    private MadDriverPanel gamePanel;
    private Timer gameTimer;
    private boolean isRunning = false;
    private boolean isPaused = false;
    
    // Game variables
    private int playerX = 400;
    private int playerY = 500;
    private int playerSpeed = 5;
    private List<Rectangle> obstacles;
    private Random random;
    private int score = 0;
    private int lives = 3;
    
    // Track key states
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    
    public MadDriverGame(GameManager manager) {
        this.gameManager = manager;
        this.gamePanel = new MadDriverPanel();
        this.random = new Random();
        initializeGame();
    }
    
    private void initializeGame() {
        obstacles = new ArrayList<>();
        setupKeyListener();
    }
    
    public JPanel getGamePanel() {
        return gamePanel;
    }
    
    public void startGame() {
        isRunning = true;
        isPaused = false;
        playerX = 400;
        playerY = 500;
        score = 0;
        lives = 3;
        obstacles.clear();
        leftPressed = false;
        rightPressed = false;
        
        if (gameTimer != null && gameTimer.isRunning()) {
            gameTimer.stop();
        }
        
        gameTimer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isPaused) {
                    updateGame();
                    gamePanel.repaint();
                }
            }
        });
        gameTimer.start();
        
        // Ensure focus is requested
        gamePanel.requestFocusInWindow();
    }
    
    public void pauseGame() {
        isPaused = true;
    }
    
    public void resumeGame() {
        isPaused = false;
        gamePanel.requestFocusInWindow();
    }
    
    public void stopGame() {
        isRunning = false;
        if (gameTimer != null) {
            gameTimer.stop();
        }
    }
    
    private void updateGame() {
        if (!isRunning) return;
        
        // Handle continuous movement
        if (leftPressed) {
            playerX = Math.max(30, playerX - playerSpeed);
        }
        if (rightPressed) {
            playerX = Math.min(770, playerX + playerSpeed);
        }
        
        // Spawn obstacles
        if (random.nextInt(100) < 5) {
            int obstacleWidth = 50 + random.nextInt(50);
            int obstacleX = random.nextInt(800 - obstacleWidth);
            obstacles.add(new Rectangle(obstacleX, -50, obstacleWidth, 30));
        }
        
        // Move obstacles
        List<Rectangle> obstaclesToRemove = new ArrayList<>();
        for (Rectangle obstacle : obstacles) {
            obstacle.y += 7;
            if (obstacle.y > 600) {
                obstaclesToRemove.add(obstacle);
                score += 10;
            }
            
            // Check collision
            if (obstacle.intersects(playerX - 15, playerY - 25, 30, 50)) {
                obstaclesToRemove.add(obstacle);
                lives--;
                if (lives <= 0) {
                    gameOver();
                }
            }
        }
        obstacles.removeAll(obstaclesToRemove);
    }
    
    private void gameOver() {
        isRunning = false;
        gameTimer.stop();
        JOptionPane.showMessageDialog(gamePanel, "Game Over! Final Score: " + score);
        gameManager.showMainMenu();
    }
    
    private void setupKeyListener() {
        gamePanel.setFocusable(true);
        
        gamePanel.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            
            @Override
            public void keyPressed(KeyEvent e) {
                System.out.println("Key pressed: " + e.getKeyCode()); // Debug
                
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    gameManager.showInGameMenu();
                } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    leftPressed = true;
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    rightPressed = true;
                }
            }
            
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    leftPressed = false;
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    rightPressed = false;
                }
            }
        });
        
        // Also add focus listener to ensure panel gets focus when clicked
        gamePanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                gamePanel.requestFocusInWindow();
            }
        });
    }
    
    class MadDriverPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw road background
            g2.setColor(Color.DARK_GRAY);
            g2.fillRect(0, 0, getWidth(), getHeight());
            
            // Draw road markings
            g2.setColor(Color.YELLOW);
            for (int i = 0; i < getHeight(); i += 40) {
                g2.fillRect(getWidth()/2 - 5, i, 10, 20);
            }
            
            // Draw player car
            g2.setColor(Color.RED);
            g2.fillRect(playerX - 15, playerY - 25, 30, 50);
            g2.setColor(Color.YELLOW);
            g2.fillRect(playerX - 12, playerY - 22, 24, 10);
            g2.fillRect(playerX - 12, playerY - 5, 24, 10);
            
            // Draw obstacles
            g2.setColor(Color.BLUE);
            for (Rectangle obstacle : obstacles) {
                g2.fillRect(obstacle.x, obstacle.y, obstacle.width, obstacle.height);
            }
            
            // Draw score and lives
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 20));
            g2.drawString("Score: " + score, 20, 30);
            g2.drawString("Lives: " + lives, 20, 60);
            
            // Draw controls hint
            g2.setFont(new Font("Arial", Font.PLAIN, 14));
            g2.drawString("Use LEFT/RIGHT arrows to move, ESC for menu", 20, getHeight() - 20);
            g2.drawString("Click on the game area if keys don't work", 20, getHeight() - 40);
            
            if (isPaused) {
                g2.setColor(new Color(0, 0, 0, 150));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(Color.YELLOW);
                g2.setFont(new Font("Arial", Font.BOLD, 36));
                String pauseText = "PAUSED";
                int textWidth = g2.getFontMetrics().stringWidth(pauseText);
                g2.drawString(pauseText, (getWidth() - textWidth) / 2, getHeight() / 2);
            }
        }
    }
}