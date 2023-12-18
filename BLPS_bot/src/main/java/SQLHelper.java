import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SQLHelper {
    private static final String DB_URL = System.getenv("YOUR_ENV_VARIABLE_NAME");
    private static final String POSTGRES_DRIVER = "org.postgresql.Driver";
    private static final Logger LOGGER = Logger.getLogger(SQLHelper.class.getName());

    static {
        loadPostgresDriver();
    }

    private static void loadPostgresDriver() {
        try {
            Class.forName(POSTGRES_DRIVER);
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "PostgreSQL Driver not found", e);
            throw new RuntimeException("PostgreSQL Driver not found", e);
        }
    }

    public static Connection getConnection() throws SQLException {
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

                if (!springId.isEmpty()) {
                    return new UserStateResult(UserState.AUTHORIZED, null);
                } else if (!uniqueHash.isEmpty()) {
                    return new UserStateResult(UserState.HAS_UUID, uniqueHash);
                } else {
                    return new UserStateResult(UserState.INITIAL, null);
                }
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
        return new UserStateResult(UserState.INITIAL, null);
    }

    public static List<String> getUserIds() {
        List<String> telegramIds = new ArrayList<>();
        String sql = "SELECT telegram_id FROM moderators";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                telegramIds.add(rs.getString("telegram_id"));
            }

        } catch (SQLException e) {
            handleSQLException(e);
        }
        return telegramIds;
    }

    public static String addUser(String springId, String uniqueHash) {
        String telegramId = null;
        String fetchSql = "SELECT telegram_id FROM moderators WHERE uniquehash = ?";
        try (Connection conn = getConnection();
             PreparedStatement fetchStmt = conn.prepareStatement(fetchSql)) {
            fetchStmt.setString(1, uniqueHash);
            ResultSet rs = fetchStmt.executeQuery();
            if (rs.next()) {
                telegramId = rs.getString("telegram_id");
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
        String updateSql = "UPDATE moderators SET spring_id = ?, uniquehash = NULL WHERE uniquehash = ?";
        try (Connection conn = getConnection();
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
            updateStmt.setString(1, springId);
            updateStmt.setString(2, uniqueHash);
            int rowsAffected = updateStmt.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "User updated with springId: {}", springId);
                return telegramId;
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
        return null;
    }


    public static String addUser(String telegramId) {
        String sql = "INSERT INTO moderators (telegram_id, uniquehash) VALUES (?, ?)";
        String uniqueHash = generateUniqueHash();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, telegramId);
            pstmt.setString(2, uniqueHash);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "User added with ID: {} ", telegramId);
                return uniqueHash;
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
        return "";
    }

    private static String generateUniqueHash() {
        return UUID.randomUUID().toString();
    }

    private static void handleSQLException(SQLException e) {
        LOGGER.log(Level.SEVERE, "SQL Exception occurred", e);
        throw new RuntimeException("SQL Exception occurred", e);
    }
}
