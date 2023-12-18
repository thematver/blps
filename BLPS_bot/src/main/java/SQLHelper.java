import com.pengrad.telegrambot.model.User;

import java.sql.*;
import java.util.UUID;  // To generate a unique hash


enum  UserState {
    INITIAL,
    HAS_UUID,
    AUTHORIZED,
    BLOCKED,
}

class UserStateResult {
    private UserState userState;
    private String uuid;

    public UserStateResult(UserState userState, String uuid) {
        this.userState = userState;
        this.uuid = uuid;
    }

    public UserState getUserState() {
        return userState;
    }

    public String getUuid() {
        return uuid;
    }
}


public class SQLHelper {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/blps_bot";

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(DB_URL);
    }

    public static UserStateResult checkUser(String telegramId) {
        String sql = "SELECT spring_id, uniquehash FROM moderators WHERE telegram_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, telegramId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String springId = rs.getString("spring_id");
                String uniqueHash = rs.getString("uniquehash");

                if (springId != null && !springId.isEmpty()) {
                    return new UserStateResult(UserState.AUTHORIZED, null);
                } else if (uniqueHash != null && !uniqueHash.isEmpty()) {
                    return new UserStateResult(UserState.HAS_UUID, uniqueHash);
                } else {
                    return new UserStateResult(UserState.INITIAL, null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new UserStateResult(UserState.INITIAL, null);
    }



    // Overloaded addUser
    public static String addUser(String telegramId) {
        String sql = "INSERT INTO moderators (telegram_id, uniquehash) VALUES (?, ?)";
        String uniqueHash = UUID.randomUUID().toString();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            pstmt.setString(1, telegramId);
            pstmt.setString(2, uniqueHash);  // Generating a unique hash
            int rowsAffected = pstmt.executeUpdate();
            conn.commit();
            return uniqueHash;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String addUser(String springId, String uniqueHash) {
        String telegramId = null;

        // First, fetch the Telegram ID using uniqueHash
        String fetchSql = "SELECT telegram_id FROM moderators WHERE uniquehash = ?";
        try (Connection conn = getConnection();
             PreparedStatement fetchStmt = conn.prepareStatement(fetchSql)) {
            fetchStmt.setString(1, uniqueHash);
            ResultSet rs = fetchStmt.executeQuery();
            if (rs.next()) {
                telegramId = rs.getString("telegram_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Now, update the entry
        String updateSql = "UPDATE moderators SET spring_id = ?, uniquehash = NULL WHERE uniquehash = ?";
        try (Connection conn = getConnection();
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
            conn.setAutoCommit(false);
            updateStmt.setString(1, springId);
            updateStmt.setString(2, uniqueHash);
            int rowsAffected = updateStmt.executeUpdate();
            conn.commit();
            if (rowsAffected > 0) {
                return telegramId;  // Return the fetched Telegram ID
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;  // Return null if the operation failed or the Telegram ID wasn't found
    }


}
