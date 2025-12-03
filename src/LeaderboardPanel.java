import javax.swing.*;
import java.awt.*;
import java.util.List;

public class LeaderboardPanel extends JPanel {
    private final GameLauncher launcher;
    private final UserManager userManager;
    private final JPanel listPanel = new JPanel();

    public LeaderboardPanel(GameLauncher launcher, UserManager userManager) {
        this.launcher = launcher;
        this.userManager = userManager;
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());

        BackgroundPanel bg = new BackgroundPanel("gameplay_bg.png");
        bg.setLayout(new BorderLayout());
        bg.setBorder(BorderFactory.createEmptyBorder(24, 32, 32, 32));

        // Title
        JLabel title = new JLabel("🏆 LEADERBOARD 🏆", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 26f));
        title.setForeground(new Color(0xFFD700)); // Gold color
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        bg.add(title, BorderLayout.NORTH);

        // Leaderboard list
        listPanel.setOpaque(false);
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        bg.add(scroll, BorderLayout.CENTER);

        // Back button
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttons.setOpaque(false);
        CustomImageButton back = new CustomImageButton("assets/back.png", "assets/backh.png", "assets/backp.png");
        back.addActionListener(e -> launcher.showMainMenu());
        buttons.add(back);
        bg.add(buttons, BorderLayout.SOUTH);

        add(bg, BorderLayout.CENTER);
    }

    public void refreshLeaderboard() {
        new SwingWorker<List<User>, Void>() {
            @Override
            protected List<User> doInBackground() {
                return userManager.getLeaderboard();
            }

            @Override
            protected void done() {
                listPanel.removeAll();
                try {
                    List<User> users = get();
                    
                    if (users.isEmpty()) {
                        JLabel empty = new JLabel("Belum ada pemain di leaderboard");
                        empty.setForeground(Color.GRAY);
                        empty.setAlignmentX(Component.CENTER_ALIGNMENT);
                        listPanel.add(empty);
                    } else {
                        // Header
                        JPanel header = createHeaderPanel();
                        listPanel.add(header);
                        listPanel.add(Box.createVerticalStrut(10));
                        
                        // Entries
                        int rank = 1;
                        for (User u : users) {
                            JPanel entry = createLeaderboardEntry(rank, u);
                            listPanel.add(entry);
                            listPanel.add(Box.createVerticalStrut(5));
                            rank++;
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LeaderboardPanel.this, 
                        "Gagal memuat leaderboard: " + ex.getMessage());
                }
                listPanel.revalidate();
                listPanel.repaint();
            }
        }.execute();
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new GridLayout(1, 4, 10, 0));
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        header.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));

        JLabel rankLabel = new JLabel("Rank");
        JLabel nameLabel = new JLabel("Username");
        JLabel scoreLabel = new JLabel("Total Score");
        JLabel levelLabel = new JLabel("Level");

        Font headerFont = new Font("Arial", Font.BOLD, 14);
        Color headerColor = new Color(0x1F6D8C);
        
        rankLabel.setFont(headerFont);
        nameLabel.setFont(headerFont);
        scoreLabel.setFont(headerFont);
        levelLabel.setFont(headerFont);
        
        rankLabel.setForeground(headerColor);
        nameLabel.setForeground(headerColor);
        scoreLabel.setForeground(headerColor);
        levelLabel.setForeground(headerColor);

        header.add(rankLabel);
        header.add(nameLabel);
        header.add(scoreLabel);
        header.add(levelLabel);

        return header;
    }

    private JPanel createLeaderboardEntry(int rank, User user) {
        JPanel entry = new JPanel(new GridLayout(1, 4, 10, 0));
        entry.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        entry.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        // Medal for top 3
        String rankText;
        Color bgColor;
        Color textColor = Color.BLACK;
        
        if (rank == 1) {
            rankText = "🥇 " + rank;
            bgColor = new Color(255, 215, 0, 80); // Gold with transparency
        } else if (rank == 2) {
            rankText = "🥈 " + rank;
            bgColor = new Color(192, 192, 192, 80); // Silver
        } else if (rank == 3) {
            rankText = "🥉 " + rank;
            bgColor = new Color(205, 127, 50, 80); // Bronze
        } else {
            rankText = String.valueOf(rank);
            bgColor = new Color(255, 255, 255, 40); // White transparent
        }
        
        entry.setOpaque(true);
        entry.setBackground(bgColor);

        JLabel rankLabel = new JLabel(rankText);
        JLabel nameLabel = new JLabel(user.getUsername());
        JLabel scoreLabel = new JLabel(String.valueOf(user.getTotalScore()));
        JLabel levelLabel = new JLabel("Level " + user.getUnlockedLevel());

        Font entryFont = new Font("Arial", Font.PLAIN, 13);
        rankLabel.setFont(entryFont);
        nameLabel.setFont(entryFont);
        scoreLabel.setFont(entryFont);
        levelLabel.setFont(entryFont);

        rankLabel.setForeground(textColor);
        nameLabel.setForeground(textColor);
        scoreLabel.setForeground(textColor);
        levelLabel.setForeground(textColor);

        entry.add(rankLabel);
        entry.add(nameLabel);
        entry.add(scoreLabel);
        entry.add(levelLabel);

        return entry;
    }
}