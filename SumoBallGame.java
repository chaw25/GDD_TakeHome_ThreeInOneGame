import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SumoBallGame {
    private GameManager gameManager;
    private SumoBallPanel gamePanel;
    private Timer gameTimer;
    private boolean isRunning = false;
    private boolean isPaused = false;
    
    // Game variables
    private PlayerBall player;
    private List<EnemyBall> enemies;
    private Random random;
    private int score = 0;
    private int arenaSize = 500;
    
    public SumoBallGame(GameManager manager) {
        this.gameManager = manager;
        this.gamePanel = new SumoBallPanel();
        this.random = new Random();
        initializeGame();
    }
    
    private void initializeGame() {
        player = new PlayerBall(400, 300);
        enemies = new ArrayList<>();
        setupKeyListener();
    }
    
    public JPanel getGamePanel() {
        return gamePanel;
    }
    
    public void startGame() {
        isRunning = true;
        isPaused = false;
        player = new PlayerBall(400, 300);
        score = 0;
        enemies.clear();
        
        // Create initial enemies
        for (int i = 0; i < 5; i++) {
            spawnEnemy();
        }
        
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
    }
    
    public void pauseGame() {
        isPaused = true;
    }
    
    public void resumeGame() {
        isPaused = false;
    }
    
    public void stopGame() {
        isRunning = false;
        if (gameTimer != null) {
            gameTimer.stop();
        }
    }
    
    private void updateGame() {
        if (!isRunning) return;
        
        // Update player
        player.update();
        
        // Update enemies
        for (EnemyBall enemy : enemies) {
            enemy.update(player);
            
            // Check collision with player
            if (checkCollision(player, enemy)) {
                handleCollision(player, enemy);
            }
            
            // Check collision between enemies
            for (EnemyBall other : enemies) {
                if (enemy != other && checkCollision(enemy, other)) {
                    handleCollision(enemy, other);
                }
            }
        }
        
        // Remove enemies that fell off and spawn new ones
        List<EnemyBall> enemiesToRemove = new ArrayList<>();
        for (EnemyBall enemy : enemies) {
            if (isOutOfArena(enemy)) {
                enemiesToRemove.add(enemy);
                score += 10;
            }
        }
        enemies.removeAll(enemiesToRemove);
        
        // Spawn new enemies
        while (enemies.size() < 5 + score / 50) {
            spawnEnemy();
        }
        
        // Check if player fell off
        if (isOutOfArena(player)) {
            gameOver();
        }
    }
    
    private void spawnEnemy() {
        int x, y;
        do {
            x = 200 + random.nextInt(400);
            y = 150 + random.nextInt(300);
        } while (Math.hypot(x - player.x, y - player.y) < 100);
        
        enemies.add(new EnemyBall(x, y, 20 + random.nextInt(20)));
    }
    
    private boolean checkCollision(Ball b1, Ball b2) {
        double dx = b1.x - b2.x;
        double dy = b1.y - b2.y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance < (b1.radius + b2.radius);
    }
    
    private void handleCollision(Ball b1, Ball b2) {
        // Simple elastic collision
        double dx = b2.x - b1.x;
        double dy = b2.y - b1.y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance == 0) return;
        
        double overlap = (b1.radius + b2.radius - distance) / 2.0;
        
        // Separate balls
        b1.x -= overlap * (dx / distance);
        b1.y -= overlap * (dy / distance);
        b2.x += overlap * (dx / distance);
        b2.y += overlap * (dy / distance);
        
        // Calculate new velocities (simplified)
        double force = 2.0;
        b1.vx -= force * (dx / distance);
        b1.vy -= force * (dy / distance);
        b2.vx += force * (dx / distance);
        b2.vy += force * (dy / distance);
    }
    
    private boolean isOutOfArena(Ball ball) {
        double distanceFromCenter = Math.hypot(ball.x - 400, ball.y - 300);
        return distanceFromCenter > arenaSize / 2;
    }
    
    private void gameOver() {
        isRunning = false;
        gameTimer.stop();
        JOptionPane.showMessageDialog(gamePanel, "Game Over! Final Score: " + score);
        gameManager.showMainMenu();
    }
    
    private void setupKeyListener() {
        gamePanel.setFocusable(true);
        gamePanel.requestFocusInWindow();
        
        gamePanel.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    gameManager.showInGameMenu();
                } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    player.vx = -5;
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    player.vx = 5;
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    player.vy = -5;
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    player.vy = 5;
                }
            }
            
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    player.vx = 0;
                } else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
                    player.vy = 0;
                }
            }
        });
    }
    
    // Ball classes
    class Ball {
        double x, y;
        double vx, vy;
        int radius;
        Color color;
        
        public Ball(double x, double y) {
            this.x = x;
            this.y = y;
        }
        
        public void update() {
            x += vx;
            y += vy;
            
            // Friction
            vx *= 0.95;
            vy *= 0.95;
        }
    }
    
    class PlayerBall extends Ball {
        public PlayerBall(double x, double y) {
            super(x, y);
            this.radius = 25;
            this.color = Color.RED;
        }
    }
    
    class EnemyBall extends Ball {
        public EnemyBall(double x, double y, int radius) {
            super(x, y);
            this.radius = radius;
            this.color = new Color(
                random.nextInt(200),
                random.nextInt(200), 
                random.nextInt(200)
            );
        }
        
        public void update(PlayerBall player) {
            // Simple AI: move towards player but avoid getting too close
            double dx = player.x - x;
            double dy = player.y - y;
            double distance = Math.sqrt(dx * dx + dy * dy);
            
            if (distance > 0) {
                double speed = 1.5;
                if (distance < 100) {
                    // Move away if too close
                    vx -= speed * (dx / distance);
                    vy -= speed * (dy / distance);
                } else {
                    // Move towards player
                    vx += speed * (dx / distance);
                    vy += speed * (dy / distance);
                }
            }
            
            // Apply friction and update position
            super.update();
        }
    }
    
    // Separate GamePanel class for Sumo Ball
    class SumoBallPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw background
            GradientPaint bgGradient = new GradientPaint(0, 0, new Color(50, 50, 80), 
                                                       0, getHeight(), new Color(30, 30, 50));
            g2.setPaint(bgGradient);
            g2.fillRect(0, 0, getWidth(), getHeight());
            
            // Draw arena
            g2.setColor(new Color(200, 200, 220));
            g2.fillOval(400 - arenaSize/2, 300 - arenaSize/2, arenaSize, arenaSize);
            g2.setColor(new Color(100, 100, 120));
            g2.setStroke(new BasicStroke(5));
            g2.drawOval(400 - arenaSize/2, 300 - arenaSize/2, arenaSize, arenaSize);
            
            // Draw center circle
            g2.setColor(new Color(150, 150, 170));
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(400 - 50, 300 - 50, 100, 100);
            
            // Draw enemy balls
            for (EnemyBall enemy : enemies) {
                g2.setColor(enemy.color);
                g2.fillOval((int)(enemy.x - enemy.radius), (int)(enemy.y - enemy.radius), 
                           enemy.radius * 2, enemy.radius * 2);
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(2));
                g2.drawOval((int)(enemy.x - enemy.radius), (int)(enemy.y - enemy.radius), 
                           enemy.radius * 2, enemy.radius * 2);
            }
            
            // Draw player ball
            g2.setColor(player.color);
            g2.fillOval((int)(player.x - player.radius), (int)(player.y - player.radius), 
                       player.radius * 2, player.radius * 2);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(3));
            g2.drawOval((int)(player.x - player.radius), (int)(player.y - player.radius), 
                       player.radius * 2, player.radius * 2);
            
            // Draw player eyes
            g2.setColor(Color.WHITE);
            g2.fillOval((int)(player.x - 8), (int)(player.y - 8), 10, 10);
            g2.fillOval((int)(player.x - 2), (int)(player.y - 8), 10, 10);
            g2.setColor(Color.BLACK);
            g2.fillOval((int)(player.x - 6), (int)(player.y - 6), 5, 5);
            g2.fillOval((int)(player.x + 4), (int)(player.y - 6), 5, 5);
            
            // Draw score
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 24));
            g2.drawString("Score: " + score, 20, 30);
            
            // Draw controls hint
            g2.setFont(new Font("Arial", Font.PLAIN, 14));
            g2.drawString("Use ARROW KEYS to move, ESC for menu", 20, getHeight() - 20);
            g2.drawString("Push other balls out of the arena!", 20, getHeight() - 40);
            
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