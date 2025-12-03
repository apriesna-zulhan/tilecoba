import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserManager {
    private final DatabaseManager databaseManager;
    private String lastError;

    public UserManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public String getLastError() {
        return lastError;
    }

    public User createUser(String name) {
        String insertUser = "INSERT INTO users(username) VALUES (?)";
        String insertSave = "INSERT INTO saves(user_id, name, unlocked_level, total_score) VALUES (?, ?, 1, 0)";
        lastError = null;

        try (Connection conn = databaseManager.getConnection()) {
            conn.setAutoCommit(false);

            try {
                long userId;
                try (PreparedStatement psUser = conn.prepareStatement(insertUser,
                        PreparedStatement.RETURN_GENERATED_KEYS)) {
                    psUser.setString(1, name);
                    psUser.executeUpdate();
                    try (ResultSet rs = psUser.getGeneratedKeys()) {
                        if (rs.next()) {
                            userId = rs.getLong(1);
                        } else {
                            conn.rollback();
                            return null;
                        }
                    }
                }

                long saveId;
                try (PreparedStatement psSave = conn.prepareStatement(insertSave,
                        PreparedStatement.RETURN_GENERATED_KEYS)) {
                    psSave.setLong(1, userId);
                    psSave.setString(2, name);
                    psSave.executeUpdate();
                    try (ResultSet rs = psSave.getGeneratedKeys()) {
                        if (rs.next()) {
                            saveId = rs.getLong(1);
                        } else {
                            conn.rollback();
                            return null;
                        }
                    }
                }

                conn.commit();
                return new User((int) saveId, name, 1, 0, LocalDateTime.now());
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            lastError = e.getMessage();
            return null;
        }
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT s.id, s.name, s.unlocked_level, s.total_score, s.created_at " +
                    "FROM saves s ORDER BY s.created_at DESC";
        try (Connection conn = databaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("name");
                int unlocked = rs.getInt("unlocked_level");
                long totalScore = rs.getLong("total_score");
                Timestamp created = rs.getTimestamp("created_at");
                users.add(new User(id, username, unlocked, totalScore,
                        created != null ? created.toLocalDateTime() : LocalDateTime.now()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public User getUserById(int id) {
        String sql = "SELECT s.id, s.name, s.unlocked_level, s.total_score, s.created_at " +
                    "FROM saves s WHERE s.id = ?";
        try (Connection conn = databaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp created = rs.getTimestamp("created_at");
                    return new User(
                        rs.getInt("id"), 
                        rs.getString("name"), 
                        rs.getInt("unlocked_level"),
                        rs.getLong("total_score"),
                        created != null ? created.toLocalDateTime() : LocalDateTime.now()
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateUnlockedLevel(int userId, int newLevel) {
        String sql = "UPDATE saves SET unlocked_level = ? WHERE id = ?";
        try (Connection conn = databaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newLevel);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get saved round for a specific level
     */
    public int getSavedRound(int saveId, int levelId) {
        String sql = "SELECT current_round FROM level_progress WHERE save_id = ? AND level_id = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, saveId);
            ps.setInt(2, levelId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("current_round");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }

    /**
     * Save current progress for specific level
     */
    public void saveCurrentProgress(int saveId, int levelId, int roundNumber) {
        String sql = "INSERT INTO level_progress (save_id, level_id, current_round) " +
                    "VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE current_round = VALUES(current_round)";
        try (Connection conn = databaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, saveId);
            ps.setInt(2, levelId);
            ps.setInt(3, roundNumber);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reset progress when level is completed or user wants fresh start
     */
    public void resetLevelProgress(int saveId, int levelId) {
        String sql = "DELETE FROM level_progress WHERE save_id = ? AND level_id = ?";
        try (Connection conn = databaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, saveId);
            ps.setInt(2, levelId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get all level progress for display
     */
    public String getLevelProgressSummary(int saveId) {
        StringBuilder summary = new StringBuilder();
        String sql = "SELECT level_id, current_round FROM level_progress " +
                    "WHERE save_id = ? ORDER BY level_id";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, saveId);
            try (ResultSet rs = ps.executeQuery()) {
                boolean hasProgress = false;
                while (rs.next()) {
                    if (hasProgress) summary.append(", ");
                    summary.append("L").append(rs.getInt("level_id"))
                           .append("-R").append(rs.getInt("current_round"));
                    hasProgress = true;
                }
                if (hasProgress) {
                    return "📍 Saved: " + summary.toString();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Get temp score for level in progress
     */
    public long getTempScore(int saveId, int levelId) {
        String sql = "SELECT temp_score FROM level_temp_score WHERE save_id = ? AND level_id = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, saveId);
            ps.setInt(2, levelId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("temp_score");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Update temp score during level play
     */
    public void updateTempScore(int saveId, int levelId, long score) {
        String sql = "INSERT INTO level_temp_score (save_id, level_id, temp_score) " +
                    "VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE temp_score = VALUES(temp_score)";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, saveId);
            ps.setInt(2, levelId);
            ps.setLong(3, score);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * NEW: Get highest level that user has ever attempted (completed or not)
     */
    public int getHighestAttemptedLevel(int saveId) {
        String sql = "SELECT MAX(level_id) as max_level FROM level_scores WHERE save_id = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, saveId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("max_level");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; // Belum pernah main level apapun
    }

    /**
     * UPDATED: Check if level already completed AND can be updated
     * Returns true only if it's NOT the highest attempted level
     */
    public boolean isLevelCompletedAndLocked(int saveId, int levelId) {
        int highestAttempted = getHighestAttemptedLevel(saveId);
        boolean isHighestLevel = (levelId >= highestAttempted);
        
        // Jika ini level tertinggi, return false (boleh update)
        if (isHighestLevel) {
            return false;
        }
        
        // Jika bukan level tertinggi, cek apakah sudah completed
        String sql = "SELECT is_completed FROM level_scores WHERE save_id = ? AND level_id = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, saveId);
            ps.setInt(2, levelId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("is_completed");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * UPDATED: Save final level score dengan aturan baru
     * - Level tertinggi yang pernah dicoba: SELALU update score (gagal/berhasil)
     * - Level di bawah tertinggi: TIDAK update score (prevent farming)
     */
    public void saveLevelScore(int saveId, int levelId, long levelScore) {
        int highestAttempted = getHighestAttemptedLevel(saveId);
        boolean isHighestLevel = (levelId >= highestAttempted);
        
        String insertOrUpdate;
        if (isHighestLevel) {
            // Level tertinggi: SELALU update score
            insertOrUpdate = "INSERT INTO level_scores (save_id, level_id, level_score, is_completed) " +
                            "VALUES (?, ?, ?, TRUE) " +
                            "ON DUPLICATE KEY UPDATE " +
                            "level_score = VALUES(level_score), " +
                            "is_completed = TRUE";
        } else {
            // Level lama: TIDAK update jika sudah completed
            insertOrUpdate = "INSERT INTO level_scores (save_id, level_id, level_score, is_completed) " +
                            "VALUES (?, ?, ?, TRUE) " +
                            "ON DUPLICATE KEY UPDATE " +
                            "level_score = IF(is_completed = FALSE, VALUES(level_score), level_score), " +
                            "is_completed = TRUE";
        }
        
        // Recalculate total score = SUM of all completed levels
        String updateTotalScore = 
            "UPDATE saves s SET s.total_score = (" +
            "    SELECT COALESCE(SUM(ls.level_score), 0) " +
            "    FROM level_scores ls " +
            "    WHERE ls.save_id = s.id AND ls.is_completed = TRUE" +
            ") WHERE s.id = ?";
        
        try (Connection conn = databaseManager.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // Save/update level score
                try (PreparedStatement ps = conn.prepareStatement(insertOrUpdate)) {
                    ps.setInt(1, saveId);
                    ps.setInt(2, levelId);
                    ps.setLong(3, levelScore);
                    ps.executeUpdate();
                }
                
                // Recalculate total
                try (PreparedStatement ps = conn.prepareStatement(updateTotalScore)) {
                    ps.setInt(1, saveId);
                    ps.executeUpdate();
                }
                
                // Clear temp score and progress
                clearTempScore(conn, saveId, levelId);
                
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void clearTempScore(Connection conn, int saveId, int levelId) throws SQLException {
        String sql = "DELETE FROM level_temp_score WHERE save_id = ? AND level_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, saveId);
            ps.setInt(2, levelId);
            ps.executeUpdate();
        }
    }

    public void recordRoundResult(int saveId, int level, int roundIdx, long elapsedMs, 
                                 long roundScore, int steps, int hazardTouches) {
        String insertRound = "INSERT INTO rounds(save_id, level, round_idx, round_elapsed_ms, " +
                            "round_score, steps, hazard_touches) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertRound)) {
            ps.setInt(1, saveId);
            ps.setInt(2, level);
            ps.setInt(3, roundIdx);
            ps.setLong(4, elapsedMs);
            ps.setLong(5, roundScore);
            ps.setInt(6, steps);
            ps.setInt(7, hazardTouches);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Leaderboard sorted by total_score (sum of all completed levels)
     */
    public List<User> getLeaderboard() {
        List<User> leaderboard = new ArrayList<>();
        String sql = "SELECT s.id, s.name, s.unlocked_level, s.total_score, s.created_at " +
                    "FROM saves s " +
                    "ORDER BY s.total_score DESC, s.unlocked_level DESC " +
                    "LIMIT 100";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("name");
                int unlocked = rs.getInt("unlocked_level");
                long totalScore = rs.getLong("total_score");
                Timestamp created = rs.getTimestamp("created_at");
                leaderboard.add(new User(id, username, unlocked, totalScore,
                        created != null ? created.toLocalDateTime() : LocalDateTime.now()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return leaderboard;
    }
}