import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class InGameMenu extends JDialog {
    private GameManager gameManager;
    
    public InGameMenu(GameManager manager) {
        this.gameManager = manager;
        setupDialog();
    }
    
    private void setupDialog() {
        setModal(true);
        setUndecorated(true);
        setSize(300, 400);
        setLocationRelativeTo(null);
        
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw semi-transparent background
                g2.setColor(new Color(0, 0, 0, 200));
                g2.fillRect(0, 0, getWidth(), getHeight());
                
                // Draw border
                g2.setColor(new Color(255, 255, 255, 100));
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(5, 5, getWidth()-10, getHeight()-10, 20, 20);
                
                // Draw title
                g2.setColor(Color.YELLOW);
                g2.setFont(new Font("Arial", Font.BOLD, 24));
                String title = "Game Paused";
                int titleWidth = g2.getFontMetrics().stringWidth(title);
                g2.drawString(title, (getWidth() - titleWidth) / 2, 40);
            }
        };
        panel.setLayout(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(60, 20, 50, 20));
        
        // Create menu buttons
        String[] options = {"Resume Game", "Restart Game", "Main Menu", "Exit Game"};
        for (String option : options) {
            JButton button = createDialogButton(option);
            button.addActionListener(new MenuActionListener(option));
            panel.add(button);
        }
        
        add(panel);
    }
    
    public void showDialog(JFrame parent) {
        setLocationRelativeTo(parent);
        setVisible(true);
    }
    
    private JButton createDialogButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isRollover()) {
                    g2.setColor(new Color(100, 100, 150, 200));
                } else {
                    g2.setColor(new Color(70, 70, 120, 200));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 2;
                g2.drawString(getText(), x, y);
            }
        };
        
        button.setPreferredSize(new Dimension(200, 50));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        
        return button;
    }
    
    private class MenuActionListener implements ActionListener {
        private String option;
        
        public MenuActionListener(String option) {
            this.option = option;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            switch (option) {
                case "Resume Game":
                    gameManager.resumeGame();
                    dispose();
                    break;
                case "Restart Game":
                    gameManager.restartCurrentGame();
                    dispose();
                    break;
                case "Main Menu":
                    gameManager.returnToMainMenu();
                    dispose();
                    break;
                case "Exit Game":
                    System.exit(0);
                    break;
            }
        }
    }
}