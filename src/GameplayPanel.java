import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameplayPanel extends JPanel {
    private final GameLauncher launcher;
    private final LevelManager levelManager;
    private LevelConfig levelConfig;
    private int currentRound = 1;
    private int totalRounds = 6;
    private StopwatchTimer stopwatchTimer = new StopwatchTimer();

    private final JPanel hudPanel = new JPanel(new BorderLayout());
    private final JPanel hudContainer = new JPanel(new GridBagLayout());
    private final JLabel timerLabel = new JLabel("00:00");
    private final JLabel roundLabel = new JLabel("Round 1");
    private final JLabel scoreLabel = new JLabel("Score: 0");
    private final JButton pauseButton = new JButton(new ImageIcon("assets/pause.png"));
    private boolean paused = false;

    private JPanel boardWrapper;
    private JPanel gridPanel;
    private JButton[][] tileButtons;
    private ImageIcon defaultIcon;
    private ImageIcon pressedIcon;
    private ImageIcon hazardIcon;
    private ImageIcon pointDefaultIcon;
    private ImageIcon pointPressedIcon;

    private final List<Point> pathPoints = new ArrayList<>();
    private Set<Point> hazards = new HashSet<>();
    private List<Point> endpoints = new ArrayList<>();
    private boolean showHazards = false;
    private boolean showEndpoints = false;

    private Timer hazardPreviewTimer;
    private Timer endpointRevealTimer;

    public GameplayPanel(GameLauncher launcher, LevelManager levelManager) {
        this.launcher = launcher;
        this.levelManager = levelManager;
        setLayout(new BorderLayout());

        BackgroundPanel backgroundPanel = new BackgroundPanel("gameplay_bg.png");
        backgroundPanel.setLayout(new BorderLayout());

        hudPanel.setOpaque(false);
        hudContainer.setOpaque(false);

        timerLabel.setForeground(new Color(0x1F6D8C));
        timerLabel.setFont(timerLabel.getFont().deriveFont(Font.BOLD, 18f));
        roundLabel.setForeground(new Color(0x1F6D8C));
        roundLabel.setFont(roundLabel.getFont().deriveFont(Font.BOLD, 18f));
        scoreLabel.setForeground(new Color(0x1F6D8C));
        scoreLabel.setFont(scoreLabel.getFont().deriveFont(Font.BOLD, 18f));

        pauseButton.setBorderPainted(false);
        pauseButton.setContentAreaFilled(false);
        pauseButton.setFocusPainted(false);
        pauseButton.setOpaque(false);
        pauseButton.addActionListener(e -> showPauseOverlay());

        JPanel leftBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        leftBox.setOpaque(false);
        leftBox.add(timerLabel);

        JPanel centerBox = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));
        centerBox.setOpaque(false);
        centerBox.add(roundLabel);

        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        rightBox.setOpaque(false);
        rightBox.add(scoreLabel);

        hudPanel.add(leftBox, BorderLayout.WEST);
        hudPanel.add(centerBox, BorderLayout.CENTER);
        hudPanel.add(rightBox, BorderLayout.EAST);

        hudPanel.setPreferredSize(new Dimension(620, 40));

        GridBagConstraints gbcHud = new GridBagConstraints();
        gbcHud.gridx = 0;
        gbcHud.gridy = 0;
        gbcHud.insets = new Insets(0, 100, 0, 0);
        hudContainer.add(hudPanel, gbcHud);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.add(hudContainer, BorderLayout.CENTER);
        topBar.add(pauseButton, BorderLayout.EAST);

        backgroundPanel.add(topBar, BorderLayout.NORTH);

        boardWrapper = new JPanel(new GridBagLayout());
        boardWrapper.setOpaque(false);
        backgroundPanel.add(boardWrapper, BorderLayout.CENTER);

        add(backgroundPanel, BorderLayout.CENTER);

        // Update timer label periodically.
        new Timer(500, e -> timerLabel.setText(formatElapsed())).start();
    }

    public void loadLevel(LevelConfig config, int roundCount) {
        this.levelConfig = config;
        this.totalRounds = roundCount;
        this.currentRound = 1;
        stopwatchTimer.reset();
        stopwatchTimer.start();
        roundLabel.setText("Round " + currentRound);
        buildGrid();
        prepareRound();
    }

    private void buildGrid() {
        boardWrapper.removeAll();
        int rows = levelConfig.getGridRows();
        int cols = levelConfig.getGridCols();
        gridPanel = new JPanel(new GridLayout(rows, cols, 2, 2));
        gridPanel.setOpaque(false);
        gridPanel.setPreferredSize(new Dimension(620, 620));

        defaultIcon = new ImageIcon("assets/tile_default.png");
        pressedIcon = new ImageIcon("assets/tile_pressed.png");
        hazardIcon = new ImageIcon("assets/tile_hazard.png");
        pointDefaultIcon = new ImageIcon("assets/tilepoint_default.png");
        pointPressedIcon = new ImageIcon("assets/tilepoint_pressed.png");

        tileButtons = new JButton[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                JButton btn = createTileButton(r, c);
                tileButtons[r][c] = btn;
                gridPanel.add(btn);
            }
        }

        boardWrapper.add(gridPanel, new GridBagConstraints());
        boardWrapper.revalidate();
        boardWrapper.repaint();
    }

    private JButton createTileButton(int row, int col) {
        JButton button = new JButton(defaultIcon);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.addActionListener(tileClickHandler(row, col));
        return button;
    }

    private ActionListener tileClickHandler(int row, int col) {
        return e -> handleTileClick(row, col);
    }

    private void prepareRound() {
        hazards = levelManager.generateDangerousTiles(levelConfig);
        endpoints = levelManager.generateTwoBluePoints(levelConfig, hazards);
        pathPoints.clear();
        showHazards = true;
        showEndpoints = false;
        refreshTiles();

        if (hazardPreviewTimer != null) {
            hazardPreviewTimer.stop();
        }
        hazardPreviewTimer = new Timer(1200, e -> {
            showHazards = false;
            ((Timer) e.getSource()).stop();
            startEndpointReveal();
            refreshTiles();
        });
        hazardPreviewTimer.setRepeats(false);
        hazardPreviewTimer.start();
    }

    private void startEndpointReveal() {
        if (endpointRevealTimer != null) {
            endpointRevealTimer.stop();
        }
        endpointRevealTimer = new Timer(200, e -> {
            showEndpoints = true;
            ((Timer) e.getSource()).stop();
            refreshTiles();
        });
        endpointRevealTimer.setRepeats(false);
        endpointRevealTimer.start();
    }

    private void refreshTiles() {
        if (tileButtons == null || levelConfig == null) {
            return;
        }
        int rows = levelConfig.getGridRows();
        int cols = levelConfig.getGridCols();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Point p = new Point(c, r);
                JButton btn = tileButtons[r][c];

                if (showHazards && hazards.contains(p)) {
                    btn.setIcon(hazardIcon);
                } else if (showEndpoints && endpoints.contains(p)) {
                    boolean pressedEndpoint = pathPoints.contains(p);
                    btn.setIcon(pressedEndpoint ? pointPressedIcon : pointDefaultIcon);
                } else if (pathPoints.contains(p)) {
                    btn.setIcon(pressedIcon);
                } else {
                    btn.setIcon(defaultIcon);
                }
            }
        }
    }

    private String formatElapsed() {
        int seconds = stopwatchTimer.getElapsedSeconds();
        int m = seconds / 60;
        int s = seconds % 60;
        return String.format("%02d:%02d", m, s);
    }

    private void pauseGame() {
        if (!paused) {
            stopwatchTimer.stop();
            paused = true;
        }
    }

    private void resumeGame() {
        if (paused) {
            stopwatchTimer.start();
            paused = false;
        }
    }

    public void handleTileClick(int row, int col) {
        if (levelConfig == null || !showEndpoints) {
            return;
        }
        Point gridPoint = new Point(col, row);
        if (hazards.contains(gridPoint)) {
            JOptionPane.showMessageDialog(this, "Kena hazard! Ulangi ronde.");
            prepareRound();
            return;
        }
        if (!pathPoints.contains(gridPoint)) {
            pathPoints.add(gridPoint);
        }
        refreshTiles();

        if (checkRoundComplete()) {
            completeCurrentRound();
        }
    }

    public boolean checkConnected() {
        if (endpoints == null || endpoints.size() < 2) {
            return false;
        }
        return pathPoints.containsAll(endpoints);
    }

    public boolean checkHazardCollision() {
        if (hazards == null) {
            return false;
        }
        for (Point p : pathPoints) {
            if (hazards.contains(p)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkRoundComplete() {
        return checkConnected() && !checkHazardCollision();
    }

    private void completeCurrentRound() {
        if (currentRound >= totalRounds) {
            stopwatchTimer.stop();
            JOptionPane.showMessageDialog(this, "Level clear! Time: " + stopwatchTimer.getElapsedSeconds() + "s");
            launcher.showMainMenu();
            return;
        }
        currentRound++;
        roundLabel.setText("Round " + currentRound);
        prepareRound();
    }

    public void advanceRound() {
        completeCurrentRound();
    }

    private void showPauseOverlay() {
        pauseGame();

        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner instanceof Frame ? (Frame) owner : null, "Paused", true);
        dialog.setUndecorated(true);

        Image rawImg = new ImageIcon("assets/pause_panel.png").getImage();
        JPanel overlay = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(rawImg, 0, 0, getWidth(), getHeight(), this);
            }
        };
        overlay.setOpaque(false);

        CustomImageButton resumeBtn = new CustomImageButton("assets/rs.png", "assets/rsh.png", "assets/rsp.png");
        resumeBtn.addActionListener(e -> {
            dialog.dispose();
            resumeGame();
        });

        CustomImageButton menuBtn = new CustomImageButton("assets/mainmenu.png", "assets/mainmenuh.png",
                "assets/mainmenup.png");
        menuBtn.addActionListener(e -> {
            dialog.dispose();
            launcher.showLevelBoardCurrent();
        });

        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        buttons.add(resumeBtn);
        buttons.add(Box.createVerticalStrut(12));
        buttons.add(menuBtn);

        overlay.add(buttons, new GridBagConstraints());

        dialog.setContentPane(overlay);
        if (owner != null) {
            dialog.setSize(owner.getSize());
            dialog.setLocationRelativeTo(owner);
        } else {
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            dialog.setSize(screen);
            dialog.setLocationRelativeTo(null);
        }
        dialog.setVisible(true);
    }
}
