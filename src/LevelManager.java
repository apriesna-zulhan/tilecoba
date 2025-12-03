import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class LevelManager {
    private final DatabaseManager databaseManager;
    private final Random random = new Random();

    public LevelManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public LevelConfig loadLevel(int levelId) {
        String sql = "SELECT id, grid_rows, grid_cols, hazard_count FROM levels WHERE id = ?";
        try (Connection conn = databaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, levelId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new LevelConfig(
                            rs.getInt("id"),
                            rs.getInt("grid_rows"),
                            rs.getInt("grid_cols"),
                            rs.getInt("hazard_count"),
                            6);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        int size = 5;
        return new LevelConfig(levelId, size, size, 4, 6);
    }

    public Dimension getGridSize(int levelId) {
        LevelConfig config = loadLevel(levelId);
        return new Dimension(config.getGridCols(), config.getGridRows());
    }

    public int getDangerousTileCount(int levelId) {
        return loadLevel(levelId).getDangerousCount();
    }

    /**
     * IMPROVED: Generate two endpoints with MINIMUM distance requirement
     * and ensure they are far apart to force path through hazards
     */
    public List<Point> generateTwoBluePoints(LevelConfig level, Set<Point> exclusions) {
        List<Point> points = new ArrayList<>();
        int rows = level.getGridRows();
        int cols = level.getGridCols();
        
        // Calculate MINIMUM distance (at least 60% of grid diagonal)
        int gridDiagonal = (int) Math.sqrt(rows * rows + cols * cols);
        int minDistance = Math.max((int)(gridDiagonal * 0.6), Math.max(rows, cols) - 1);
        
        Point first = null;
        int attempts = 0;
        while (first == null && attempts < 100) {
            Point p = new Point(random.nextInt(cols), random.nextInt(rows));
            if (exclusions == null || !exclusions.contains(p)) {
                first = p;
            }
            attempts++;
        }
        
        if (first == null) {
            first = new Point(0, 0);
        }
        points.add(first);
        
        Point second = null;
        attempts = 0;
        
        while (second == null && attempts < 200) {
            Point p = new Point(random.nextInt(cols), random.nextInt(rows));
            
            // Manhattan distance
            int distance = Math.abs(p.x - first.x) + Math.abs(p.y - first.y);
            
            if (distance >= minDistance && !p.equals(first)) {
                if (exclusions == null || !exclusions.contains(p)) {
                    second = p;
                }
            }
            attempts++;
        }
        
        // Fallback: place at opposite corners
        if (second == null) {
            if (first.x < cols / 2 && first.y < rows / 2) {
                second = new Point(cols - 1, rows - 1);
            } else if (first.x >= cols / 2 && first.y < rows / 2) {
                second = new Point(0, rows - 1);
            } else if (first.x < cols / 2 && first.y >= rows / 2) {
                second = new Point(cols - 1, 0);
            } else {
                second = new Point(0, 0);
            }
            
            if (second.equals(first)) {
                second = new Point(cols - 1, rows - 1);
            }
        }
        
        points.add(second);
        return points;
    }

    /**
     * IMPROVED: Generate hazards with better distribution
     * Ensures hazards are spread out but some are close to force strategic paths
     */
    public Set<Point> generateDangerousTiles(LevelConfig level) {
        Set<Point> hazards = new HashSet<>();
        int rows = level.getGridRows();
        int cols = level.getGridCols();
        int targetCount = level.getDangerousCount();
        
        int maxSafeHazards = (rows * cols) / 3;
        targetCount = Math.min(targetCount, maxSafeHazards);
        
        // First phase: spread hazards evenly
        int attempts = 0;
        int spreadCount = targetCount / 2;
        while (hazards.size() < spreadCount && attempts < 200) {
            Point p = new Point(random.nextInt(cols), random.nextInt(rows));
            
            boolean tooClose = false;
            for (Point existing : hazards) {
                int distance = Math.abs(p.x - existing.x) + Math.abs(p.y - existing.y);
                if (distance < 3) {
                    tooClose = true;
                    break;
                }
            }
            
            if (!tooClose) {
                hazards.add(p);
            }
            attempts++;
        }
        
        // Second phase: add remaining hazards with less spacing
        attempts = 0;
        while (hazards.size() < targetCount && attempts < 200) {
            Point p = new Point(random.nextInt(cols), random.nextInt(rows));
            
            boolean tooClose = false;
            for (Point existing : hazards) {
                int distance = Math.abs(p.x - existing.x) + Math.abs(p.y - existing.y);
                if (distance < 2) {
                    tooClose = true;
                    break;
                }
            }
            
            if (!tooClose) {
                hazards.add(p);
            }
            attempts++;
        }
        
        return hazards;
    }

    public List<LevelConfig> getAvailableLevels(User user) {
        List<LevelConfig> levels = new ArrayList<>();
        String sql = "SELECT id, grid_rows, grid_cols, hazard_count FROM levels WHERE id <= ? ORDER BY id";
        int unlocked = Math.max(user.getUnlockedLevel(), 1);
        try (Connection conn = databaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, unlocked);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    levels.add(new LevelConfig(
                            rs.getInt("id"),
                            rs.getInt("grid_rows"),
                            rs.getInt("grid_cols"),
                            rs.getInt("hazard_count"),
                            6));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (levels.isEmpty()) {
            levels.add(loadLevel(1));
        }
        return levels;
    }

    public List<LevelConfig> getAllLevels() {
        List<LevelConfig> levels = new ArrayList<>();
        String sql = "SELECT id, grid_rows, grid_cols, hazard_count FROM levels ORDER BY id";
        try (Connection conn = databaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                levels.add(new LevelConfig(
                        rs.getInt("id"),
                        rs.getInt("grid_rows"),
                        rs.getInt("grid_cols"),
                        rs.getInt("hazard_count"),
                        6));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (levels.isEmpty()) {
            levels.add(loadLevel(1));
        }
        return levels;
    }

    public int getRoundCount(int levelId) {
        return loadLevel(levelId).getRounds();
    }
}