import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Objects;

public class LevelBoardPanel extends JPanel {
    private final GameLauncher launcher;
    private final LevelManager levelManager;
    private User activeUser;
    private LevelConfig selectedLevel;
    private JPanel gridPanel;
    private JButton selectedButton;
    private JLabel progressLabel;

    public LevelBoardPanel(GameLauncher launcher, LevelManager levelManager) {
        this.launcher = launcher;
        this.levelManager = levelManager;
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());

        BackgroundPanel backgroundPanel = new BackgroundPanel("levelboard_bg.png");
        backgroundPanel.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createEmptyBorder(24, 0, 16, 0));

        JLabel title = new JLabel("LEVEL BOARD", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Progress summary for ALL levels
        progressLabel = new JLabel("", SwingConstants.CENTER);
        progressLabel.setFont(progressLabel.getFont().deriveFont(Font.PLAIN, 14f));
        progressLabel.setForeground(new Color(0xFF6B35));
        progressLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        topPanel.add(title);
        topPanel.add(Box.createVerticalStrut(8));
        topPanel.add(progressLabel);
        
        backgroundPanel.add(topPanel, BorderLayout.NORTH);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        gridPanel = new JPanel(new GridLayout(1, 5, 20, 20));
        gridPanel.setOpaque(false);
        centerWrapper.add(gridPanel);

        backgroundPanel.add(centerWrapper, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 12));
        bottomBar.setOpaque(false);
        CustomImageButton backButton = new CustomImageButton("assets/back.png", "assets/backh.png", "assets/backp.png");
        backButton.addActionListener(e -> launcher.showNewGame());
        bottomBar.add(backButton);
        backgroundPanel.add(bottomBar, BorderLayout.SOUTH);

        add(backgroundPanel, BorderLayout.CENTER);
    }

    public void setActiveUser(User activeUser) {
        this.activeUser = Objects.requireNonNull(activeUser, "User diperlukan untuk membuka level board");
        reloadLevels();
        updateProgressLabel();
    }

    /**
     * Show ALL saved progress across all levels
     */
    private void updateProgressLabel() {
        if (activeUser != null) {
            String summary = launcher.getUserManager().getLevelProgressSummary(activeUser.getId());
            progressLabel.setText(summary);
        } else {
            progressLabel.setText("");
        }
    }

    private void reloadLevels() {
        gridPanel.removeAll();
        selectedLevel = null;
        selectedButton = null;

        if (activeUser != null) {
            List<LevelConfig> levels = levelManager.getAllLevels();
            int unlocked = Math.max(activeUser.getUnlockedLevel(), 1);
            for (LevelConfig config : levels) {
                if (config.getId() <= unlocked) {
                    JButton levelButton = buildLevelButton(config);
                    
                    // Highlight levels with saved progress
                    int savedRound = launcher.getUserManager().getSavedRound(
                        activeUser.getId(), config.getId());
                    if (savedRound > 1) {
                        levelButton.setBorder(BorderFactory.createLineBorder(
                            new Color(0xFF6B35), 3));
                    }
                    
                    gridPanel.add(levelButton);
                    if (selectedLevel == null) {
                        selectedLevel = config;
                    }
                } else {
                    JButton locked = buildLockedButton();
                    gridPanel.add(locked);
                }
            }
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JButton buildLevelButton(LevelConfig config) {
        String suffix = config.getId() == 1 ? "" : "-" + (config.getId() - 1);
        CustomImageButton button = new CustomImageButton(
                "assets/default" + suffix + ".png",
                "assets/hover" + suffix + ".png",
                "assets/pressed" + suffix + ".png");
        button.addActionListener(e -> handlePlay(config));
        button.setBorder(null);
        button.setBorderPainted(false);
        return button;
    }

    private JButton buildLockedButton() {
        JButton button = new JButton(new ImageIcon("assets/level_locked.png"));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setEnabled(false);
        button.setDisabledIcon(button.getIcon());
        return button;
    }

    private void handlePlay(LevelConfig config) {
        selectedLevel = config;
        int roundCount = levelManager.getRoundCount(selectedLevel.getId());
        launcher.startGameplay(selectedLevel, roundCount);
    }
}