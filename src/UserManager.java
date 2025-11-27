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
        String insertSave = "INSERT INTO saves(user_id, name, unlocked_level) VALUES (?, ?, 1)";
        lastError = null;

        try (Connection conn = databaseManager.getConnection()) {
            conn.setAutoCommit(false);

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
            return new User((int) saveId, name, 1, LocalDateTime.now());
        } catch (SQLException e) {
            lastError = e.getMessage();
            return null;
        }
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT s.id, s.name, s.unlocked_level, s.created_at FROM saves s ORDER BY s.created_at DESC";
        try (Connection conn = databaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("name");
                int unlocked = rs.getInt("unlocked_level");
                Timestamp created = rs.getTimestamp("created_at");
                users.add(new User(id, username, unlocked,
                        created != null ? created.toLocalDateTime() : LocalDateTime.now()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public User getUserById(int id) {
        String sql = "SELECT s.id, s.name, s.unlocked_level, s.created_at FROM saves s WHERE s.id = ?";
        try (Connection conn = databaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp created = rs.getTimestamp("created_at");
                    return new User(rs.getInt("id"), rs.getString("name"), rs.getInt("unlocked_level"),
                            created != null ? created.toLocalDateTime() : LocalDateTime.now());
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
     * Persist a completed round and update save aggregates in one transaction.
     */
    public void recordRoundResult(int saveId, int level, int roundIdx, long elapsedMs, long roundScore, int steps,
            int hazardTouches) {
        String insertRound = "INSERT INTO rounds(save_id, level, round_idx, round_elapsed_ms, round_score, steps, hazard_touches) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        String selectSave = "SELECT unlocked_level, total_time_ms, total_score FROM saves WHERE id = ? FOR UPDATE";
        String updateSave = "UPDATE saves SET unlocked_level = ?, total_time_ms = ?, total_score = ? WHERE id = ?";

        try (Connection conn = databaseManager.getConnection()) {
            conn.setAutoCommit(false);

            int currentUnlocked = 1;
            long totalTime = 0;
            long totalScore = 0;
            try (PreparedStatement psSel = conn.prepareStatement(selectSave)) {
                psSel.setInt(1, saveId);
                try (ResultSet rs = psSel.executeQuery()) {
                    if (rs.next()) {
                        currentUnlocked = rs.getInt("unlocked_level");
                        totalTime = rs.getLong("total_time_ms");
                        totalScore = rs.getLong("total_score");
                    }
                }
            }

            try (PreparedStatement psRound = conn.prepareStatement(insertRound)) {
                psRound.setInt(1, saveId);
                psRound.setInt(2, level);
                psRound.setInt(3, roundIdx);
                psRound.setLong(4, elapsedMs);
                psRound.setLong(5, roundScore);
                psRound.setInt(6, steps);
                psRound.setInt(7, hazardTouches);
                psRound.executeUpdate();
            }

            int newUnlocked = currentUnlocked;
            if (roundIdx >= 6 && level >= currentUnlocked) {
                newUnlocked = level + 1;
            }

            try (PreparedStatement psUpdate = conn.prepareStatement(updateSave)) {
                psUpdate.setInt(1, newUnlocked);
                psUpdate.setLong(2, totalTime + elapsedMs);
                psUpdate.setLong(3, totalScore + roundScore);
                psUpdate.setInt(4, saveId);
                psUpdate.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
