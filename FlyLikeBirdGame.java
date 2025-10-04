import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FlyLikeBirdGame {
    private GameManager gameManager;
    private BirdGamePanel gamePanel;
    private Timer gameTimer;
    private boolean isRunning = false;
    private boolean isPaused = false;
    
    // Game variables
    private int birdX = 100;
    private int birdY = 300;
    private int birdVelocity = 0;
    private int gravity = 1;
    private int jumpStrength = -15;
    private List<Rectangle> pipes;
    private Random random;
    private int score = 0;
    private int gameSpeed = 3;
    
    public FlyLikeBirdGame(GameManager manager) {
        this.gameManager = manager;
        this.gamePanel = new BirdGamePanel();
        this.random = new Random();
        initializeGame();
    }
    
    private void initializeGame() {
        pipes = new ArrayList<>();
        setupKeyListener();
    }
    
    public JPanel getGamePanel() {
        return gamePanel;
    }
    
    public void startGame() {
        isRunning = true;
        isPaused = false;
        birdX = 100;
        birdY = 300;
        birdVelocity = 0;
        score = 0;
        pipes.clear();
        
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
        
        // Bird physics
        birdVelocity += gravity;
        birdY += birdVelocity;
        
        // Generate pipes
        if (pipes.isEmpty() || pipes.get(pipes.size() - 1).x < 600) {
            int gapHeight = 150;
            int gapY = 100 + random.nextInt(300);
            pipes.add(new Rectangle(800, 0, 50, gapY)); // Top pipe
            pipes.add(new Rectangle(800, gapY + gapHeight, 50, 600 - gapY - gapHeight)); // Bottom pipe
        }
        
        // Move pipes and check score
        List<Rectangle> pipesToRemove = new ArrayList<>();
        for (Rectangle pipe : pipes) {
            pipe.x -= gameSpeed;
            
            if (pipe.x + pipe.width < 0) {
                pipesToRemove.add(pipe);
            }
            
            // Check if bird passed pipe
            if (pipe.x + pipe.width == birdX && pipe.y == 0) {
                score += 5;
            }
            
            // Check collision
            if (pipe.intersects(birdX - 15, birdY - 15, 30, 30)) {
                gameOver();
            }
        }
        pipes.removeAll(pipesToRemove);
        
        // Check boundaries
        if (birdY <= 0 || birdY >= 600) {
            gameOver();
        }
    }
    
    private void gameOver() {
        isRunning = false;
        gameTimer.stop();
        JOptionPane.showMessageDialog(gamePanel, "Game Over! Final Score: " + score);
        gameManager.showMainMenu();
    }
    
    private void jump() {
        birdVelocity = jumpStrength;
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
                } else if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_UP) {
                    jump();
                }
            }
            
            @Override
            public void keyReleased(KeyEvent e) {}
        });
        
        // Also add focus listener to ensure panel gets focus when clicked
        gamePanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                gamePanel.requestFocusInWindow();
            }
        });
    }
    
    class BirdGamePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw sky background
            GradientPaint skyGradient = new GradientPaint(0, 0, new Color(135, 206, 235), 
                                                        0, getHeight(), new Color(100, 150, 255));
            g2.setPaint(skyGradient);
            g2.fillRect(0, 0, getWidth(), getHeight());
            
            // Draw ground
            g2.setColor(new Color(34, 139, 34));
            g2.fillRect(0, 500, getWidth(), 100);
            
            // Draw pipes
            g2.setColor(new Color(0, 150, 0));
            for (Rectangle pipe : pipes) {
                g2.fillRect(pipe.x, pipe.y, pipe.width, pipe.height);
                // Pipe details
                g2.setColor(new Color(0, 100, 0));
                g2.fillRect(pipe.x - 5, pipe.y, 60, 20);
                g2.fillRect(pipe.x - 5, pipe.y + pipe.height - 20, 60, 20);
                g2.setColor(new Color(0, 150, 0));
            }
            
            // Draw bird
            g2.setColor(Color.YELLOW);
            g2.fillOval(birdX - 15, birdY - 15, 30, 30);
            g2.setColor(Color.ORANGE);
            g2.fillOval(birdX + 10, birdY - 5, 10, 5); // Beak
            g2.setColor(Color.WHITE);
            g2.fillOval(birdX - 5, birdY - 10, 10, 10);
            g2.setColor(Color.BLACK);
            g2.fillOval(birdX - 2, birdY - 7, 5, 5);
            
            // Draw wing (animated)
            g2.setColor(Color.ORANGE);
            int wingY = birdY + (int)(Math.sin(System.currentTimeMillis() / 100.0) * 3);
            g2.fillOval(birdX - 10, wingY, 20, 10);
            
            // Draw score
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 24));
            g2.drawString("Score: " + score, 20, 30);
            
            // Draw controls hint
            g2.setFont(new Font("Arial", Font.PLAIN, 14));
            g2.drawString("Press SPACE/UP to flap, ESC for menu", 20, getHeight() - 20);
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