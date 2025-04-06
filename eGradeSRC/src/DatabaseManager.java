import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.*;
import io.github.cdimascio.dotenv.Dotenv;

public class DatabaseManager {

    private static final Dotenv dotenv = Dotenv.load();

    private static final String URL = dotenv.get("DB_URL");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASSWORD = dotenv.get("DB_PASSWORD");

    private static Connection connection;

    public static void connect() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Database connected successfully!");
        } catch (SQLException e) {
            System.err.println("❌ Failed to connect to the database.");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        return connection;
    }

    public static int loggedInUserId = -1;

    public static String validateLogin(String email, String password) {
        String query = "SELECT * FROM validate_login(?, ?)";

        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                loggedInUserId = rs.getInt("user_id");
                return rs.getString("user_type");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
