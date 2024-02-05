package ua.com.fm.school;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

	private static DatabaseConnection instance;
	private Connection connection;
	private String url = "jdbc:postgresql://localhost:5432/SchoolDatabase";
	private String username = "db_admin";
	private String password = "1234";

	private DatabaseConnection() throws SQLException {
		try {
			Class.forName("org.postgresql.Driver");
			this.connection = DriverManager.getConnection(url, username, password);
		} catch (ClassNotFoundException ex) {
			throw new SQLException("Database Connection Creation Failed. Driver not found.", ex);
		} catch (SQLException ex) {
			throw new SQLException("Database Connection Creation Failed: " + ex.getMessage(), ex);
		}
	}

	public Connection getConnection() {
		return connection;
	}

	public static DatabaseConnection getInstance() throws SQLException {
		if (instance == null || instance.getConnection().isClosed()) {
			instance = new DatabaseConnection();
		}

		return instance;
	}
}