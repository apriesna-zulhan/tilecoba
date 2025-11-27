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
        // fallback default
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
     * Generates two free endpoints. They are not ordered (no start-end concept).
     * Optional exclusions prevent overlap with hazards or other reserved tiles.
     */
    public List<Point> generateTwoBluePoints(LevelConfig level, Set<Point> exclusions) {
        List<Point> points = new ArrayList<>();
        int rows = level.getGridRows();
        int cols = level.getGridCols();
        while (points.size() < 2) {
            Point p = new Point(random.nextInt(cols), random.nextInt(rows));
            if (points.contains(p)) {
                continue;
            }
            if (exclusions != null && exclusions.contains(p)) {
                continue;
            }
            points.add(p);
        }
        return points;
    }

    public Set<Point> generateDangerousTiles(LevelConfig level) {
        Set<Point> hazards = new HashSet<>();
        int rows = level.getGridRows();
        int cols = level.getGridCols();
        while (hazards.size() < level.getDangerousCount()) {
            hazards.add(new Point(random.nextInt(cols), random.nextInt(rows)));
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

    /**
     * Fetch all levels defined in DB (ordered by id).
     */
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
