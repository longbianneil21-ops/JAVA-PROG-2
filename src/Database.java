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

        // Chat sessions table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS chat_sessions (
                id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                title VARCHAR(100) NOT NULL DEFAULT 'New Chat',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY(user_id) REFERENCES users(id)
            )
        """);

        // Chat history table with session_id
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS chat_history (
                id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                session_id INT NOT NULL,
                role VARCHAR(10) NOT NULL,
                message TEXT NOT NULL,
                timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY(user_id) REFERENCES users(id),
                FOREIGN KEY(session_id) REFERENCES chat_sessions(id)
            )
        """);

        // Migrate old chat_history rows that have no session_id column
        // (safe to run even if already migrated)
        try {
            stmt.execute("ALTER TABLE chat_history ADD COLUMN session_id INT NOT NULL DEFAULT 0");
            System.out.println("[Database] Migrated chat_history: added session_id column.");
        } catch (SQLException ignored) {
            // Column already exists — skip
        }

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
        if (rs.next()) userId = rs.getInt("id");
        rs.close();
        stmt.close();
        return userId;
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

    // ── Create a new chat session ────────────────────────────────────
    public static int createSession(int userId, String title) throws SQLException {
        String sql = "INSERT INTO chat_sessions (user_id, title) VALUES (?, ?)";
        PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        stmt.setInt(1, userId);
        stmt.setString(2, title);
        stmt.executeUpdate();
        ResultSet keys = stmt.getGeneratedKeys();
        int sessionId = -1;
        if (keys.next()) sessionId = keys.getInt(1);
        keys.close();
        stmt.close();
        return sessionId;
    }

    // ── Update session title ─────────────────────────────────────────
    public static void updateSessionTitle(int sessionId, String title) throws SQLException {
        String sql = "UPDATE chat_sessions SET title = ? WHERE id = ?";
        PreparedStatement stmt = getConnection().prepareStatement(sql);
        stmt.setString(1, title);
        stmt.setInt(2, sessionId);
        stmt.executeUpdate();
        stmt.close();
    }

    // ── Get all sessions for a user (newest first) ───────────────────
    public static ResultSet getSessions(int userId) throws SQLException {
        String sql = "SELECT id, title, created_at FROM chat_sessions WHERE user_id = ? ORDER BY created_at DESC";
        PreparedStatement stmt = getConnection().prepareStatement(sql);
        stmt.setInt(1, userId);
        return stmt.executeQuery();
    }

    // ── Save chat message with session ───────────────────────────────
    public static void saveMessage(int userId, int sessionId, String role, String message) throws SQLException {
        String sql = "INSERT INTO chat_history (user_id, session_id, role, message) VALUES (?, ?, ?, ?)";
        PreparedStatement stmt = getConnection().prepareStatement(sql);
        stmt.setInt(1, userId);
        stmt.setInt(2, sessionId);
        stmt.setString(3, role);
        stmt.setString(4, message);
        stmt.executeUpdate();
        stmt.close();
    }

    // ── Load messages for a specific session ─────────────────────────
    public static ResultSet getSessionMessages(int sessionId) throws SQLException {
        String sql = "SELECT role, message, timestamp FROM chat_history WHERE session_id = ? ORDER BY id ASC";
        PreparedStatement stmt = getConnection().prepareStatement(sql);
        stmt.setInt(1, sessionId);
        return stmt.executeQuery();
    }

    // ── Get most recent session for a user ───────────────────────────
    public static int getLatestSessionId(int userId) throws SQLException {
        String sql = "SELECT id FROM chat_sessions WHERE user_id = ? ORDER BY created_at DESC LIMIT 1";
        PreparedStatement stmt = getConnection().prepareStatement(sql);
        stmt.setInt(1, userId);
        ResultSet rs = stmt.executeQuery();
        int sessionId = -1;
        if (rs.next()) sessionId = rs.getInt("id");
        rs.close();
        stmt.close();
        return sessionId;
    }

    // ── Delete a session and its messages ────────────────────────────
    public static void deleteSession(int sessionId) throws SQLException {
        // Delete messages first (foreign key)
        String sql1 = "DELETE FROM chat_history WHERE session_id = ?";
        PreparedStatement stmt1 = getConnection().prepareStatement(sql1);
        stmt1.setInt(1, sessionId);
        stmt1.executeUpdate();
        stmt1.close();

        String sql2 = "DELETE FROM chat_sessions WHERE id = ?";
        PreparedStatement stmt2 = getConnection().prepareStatement(sql2);
        stmt2.setInt(1, sessionId);
        stmt2.executeUpdate();
        stmt2.close();
    }

    // ── Clear all sessions for a user ────────────────────────────────
    public static void clearAllSessions(int userId) throws SQLException {
        String sql1 = "DELETE FROM chat_history WHERE user_id = ?";
        PreparedStatement stmt1 = getConnection().prepareStatement(sql1);
        stmt1.setInt(1, userId);
        stmt1.executeUpdate();
        stmt1.close();

        String sql2 = "DELETE FROM chat_sessions WHERE user_id = ?";
        PreparedStatement stmt2 = getConnection().prepareStatement(sql2);
        stmt2.setInt(1, userId);
        stmt2.executeUpdate();
        stmt2.close();
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