import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ContinuePanel extends JPanel {
    private final GameLauncher launcher;
    private final UserManager userManager;
    private final JPanel listPanel = new JPanel();

    private static final Color ENTRY_BG_COLOR = new Color(255, 255, 240, 220);
    private static final Color TEXT_COLOR = new Color(50, 50, 50); 
    private static final Color SCORE_COLOR = new Color(192, 57, 43); 

    public ContinuePanel(GameLauncher launcher, UserManager userManager) {
        this.launcher = launcher;
        this.userManager = userManager;
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());

        BackgroundPanel bg = new BackgroundPanel("gameplay_bg.png");
        bg.setLayout(new BorderLayout());
        bg.setBorder(BorderFactory.createEmptyBorder(40, 32, 32, 32));

        JLabel title = new JLabel("Continue Save", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 26f)); 
        title.setForeground(TEXT_COLOR); 
        bg.add(title, BorderLayout.NORTH);

        listPanel.setOpaque(false);
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        bg.add(scroll, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttons.setOpaque(false);
        CustomImageButton back = new CustomImageButton("assets/back.png", "assets/backh.png", "assets/backp.png");
        back.addActionListener(e -> launcher.showMainMenu());
        buttons.add(back);
        bg.add(buttons, BorderLayout.SOUTH);

        add(bg, BorderLayout.CENTER);
    }
    
    private JButton createUserEntryPanel(User user, DateTimeFormatter fmt) {
        JPanel entryPanel = new JPanel(new BorderLayout()); 
        entryPanel.setAlignmentX(Component.LEFT_ALIGNMENT); 
        entryPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60)); 
        entryPanel.setBackground(ENTRY_BG_COLOR); 
        
        entryPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(TEXT_COLOR.darker(), 1), 
            BorderFactory.createEmptyBorder(5, 16, 5, 16)
        ));
        
        JPanel leftInfoPanel = new JPanel();
        leftInfoPanel.setLayout(new BoxLayout(leftInfoPanel, BoxLayout.Y_AXIS));
        leftInfoPanel.setOpaque(false); 
        
        JLabel usernameLabel = new JLabel(user.getUsername());
        usernameLabel.setFont(usernameLabel.getFont().deriveFont(Font.BOLD, 16f)); 
        usernameLabel.setForeground(TEXT_COLOR);
        
        String details = String.format("Level %d | Sejak %s",
                user.getUnlockedLevel(),
                user.getCreatedAt().format(fmt));
        JLabel detailsLabel = new JLabel(details);
        detailsLabel.setFont(detailsLabel.getFont().deriveFont(10f)); 
        detailsLabel.setForeground(TEXT_COLOR.darker());
        
        leftInfoPanel.add(usernameLabel);
        leftInfoPanel.add(detailsLabel);
        
        JLabel scoreLabel = new JLabel("SCORE: " + user.getTotalScore());
        scoreLabel.setFont(scoreLabel.getFont().deriveFont(Font.BOLD, 18f));
        scoreLabel.setForeground(SCORE_COLOR); 
        scoreLabel.setHorizontalAlignment(SwingConstants.RIGHT); 
        scoreLabel.setOpaque(false);

        entryPanel.add(leftInfoPanel, BorderLayout.WEST);
        entryPanel.add(scoreLabel, BorderLayout.EAST);
        
        JButton wrapperButton = new JButton();
        wrapperButton.setLayout(new BorderLayout());
        wrapperButton.add(entryPanel, BorderLayout.CENTER);
        
        wrapperButton.setContentAreaFilled(false); 
        wrapperButton.setFocusPainted(false);
        wrapperButton.setBorder(BorderFactory.createEmptyBorder());
        wrapperButton.setMaximumSize(entryPanel.getMaximumSize());
        wrapperButton.setCursor(new Cursor(Cursor.HAND_CURSOR)); 
        
        wrapperButton.addActionListener(e -> launcher.showLevelBoard(user));

        wrapperButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                entryPanel.setBackground(ENTRY_BG_COLOR.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                entryPanel.setBackground(ENTRY_BG_COLOR);
            }
        });

        return wrapperButton;
    }

    public void refreshUsers() {
        new SwingWorker<List<User>, Void>() {
            @Override
            protected List<User> doInBackground() {
                return userManager.getAllUsers();
            }

            @Override
            protected void done() {
                listPanel.removeAll();
                try {
                    List<User> users = get();
                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    
                    if (users.isEmpty()) {
                        JLabel noUsers = new JLabel("Belum ada data simpanan. Mulai permainan baru!");
                        noUsers.setAlignmentX(Component.CENTER_ALIGNMENT);
                        noUsers.setForeground(TEXT_COLOR);
                        listPanel.add(Box.createVerticalStrut(50));
                        listPanel.add(noUsers);
                    } else {
                        for (User u : users) {
                            JButton entry = createUserEntryPanel(u, fmt); 
                            listPanel.add(entry);
                            listPanel.add(Box.createVerticalStrut(15));
                        }
                    }

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ContinuePanel.this, 
                        "Gagal memuat saves: " + ex.getMessage());
                }
                listPanel.revalidate();
                listPanel.repaint();
            }
        }.execute();
    }
}