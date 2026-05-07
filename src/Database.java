import java.sql.*;

public class Database {

    private static final String URL      = "jdbc:mysql://localhost:3306/qcu_chatbot";
    private static final String USER     = "root";
    private static final String PASSWORD = ""; // XAMPP default is empty

    private static Connection connection = null;

    // ── Connect to database ──────────────────────────────────────────
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("[Database] Connected to MySQL!");
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL Driver not found! Make sure mysql-connector-java.jar is added.");
            }
        }
        return connection;
    }
    public static void clearChatHistory(int userId) throws SQLException {
    String sql = "DELETE FROM chat_history WHERE user_id = ?";
    PreparedStatement stmt = getConnection().prepareStatement(sql);
    stmt.setInt(1, userId);
    stmt.executeUpdate();
    stmt.close();
        }

    // ── Create tables if they don't exist ───────────────────────────
    public static void initialize() throws SQLException {
        Connection conn = getConnection();
        Statement stmt = conn.createStatement();

        // Users table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS users (
                id INT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(50) UNIQUE NOT NULL,
                student_number VARCHAR(20) UNIQUE NOT NULL,
                password VARCHAR(50) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);

        // Chat history table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS chat_history (
                id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                role VARCHAR(10) NOT NULL,
                message TEXT NOT NULL,
                timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY(user_id) REFERENCES users(id)
            )
        """);

        System.out.println("[Database] Tables ready!");
        stmt.close();
    }

    // ── Register new user ────────────────────────────────────────────
    public static boolean register(String username, String studentNumber, String password) throws SQLException {
        String sql = "INSERT INTO users (username, student_number, password) VALUES (?, ?, ?)";
        PreparedStatement stmt = getConnection().prepareStatement(sql);
        stmt.setString(1, username);
        stmt.setString(2, studentNumber);
        stmt.setString(3, password);
        int rows = stmt.executeUpdate();
        stmt.close();
        return rows > 0;
    }

    // ── Login user ───────────────────────────────────────────────────
    public static int login(String username, String password) throws SQLException {
        String sql = "SELECT id FROM users WHERE username = ? AND password = ?";
        PreparedStatement stmt = getConnection().prepareStatement(sql);
        stmt.setString(1, username);
        stmt.setString(2, password);
        ResultSet rs = stmt.executeQuery();
        int userId = -1;
        if (rs.next()) {
            userId = rs.getInt("id");
        }
        rs.close();
        stmt.close();
        return userId; // returns -1 if not found
    }

    // ── Check if username exists ─────────────────────────────────────
    public static boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT id FROM users WHERE username = ?";
        PreparedStatement stmt = getConnection().prepareStatement(sql);
        stmt.setString(1, username);
        ResultSet rs = stmt.executeQuery();
        boolean exists = rs.next();
        rs.close();
        stmt.close();
        return exists;
    }

    // ── Check if student number exists ───────────────────────────────
    public static boolean studentNumberExists(String studentNumber) throws SQLException {
        String sql = "SELECT id FROM users WHERE student_number = ?";
        PreparedStatement stmt = getConnection().prepareStatement(sql);
        stmt.setString(1, studentNumber);
        ResultSet rs = stmt.executeQuery();
        boolean exists = rs.next();
        rs.close();
        stmt.close();
        return exists;
    }

    // ── Save chat message ────────────────────────────────────────────
    public static void saveMessage(int userId, String role, String message) throws SQLException {
        String sql = "INSERT INTO chat_history (user_id, role, message) VALUES (?, ?, ?)";
        PreparedStatement stmt = getConnection().prepareStatement(sql);
        stmt.setInt(1, userId);
        stmt.setString(2, role);
        stmt.setString(3, message);
        stmt.executeUpdate();
        stmt.close();
    }

    // ── Load chat history for a user ─────────────────────────────────
    public static ResultSet getChatHistory(int userId) throws SQLException {
        String sql = "SELECT role, message, timestamp FROM chat_history WHERE user_id = ? ORDER BY id ASC";
        PreparedStatement stmt = getConnection().prepareStatement(sql);
        stmt.setInt(1, userId);
        return stmt.executeQuery();
    }

    // ── Get username by ID ───────────────────────────────────────────
    public static String getUsername(int userId) throws SQLException {
        String sql = "SELECT username FROM users WHERE id = ?";
        PreparedStatement stmt = getConnection().prepareStatement(sql);
        stmt.setInt(1, userId);
        ResultSet rs = stmt.executeQuery();
        String username = "";
        if (rs.next()) username = rs.getString("username");
        rs.close();
        stmt.close();
        return username;
    }
}
