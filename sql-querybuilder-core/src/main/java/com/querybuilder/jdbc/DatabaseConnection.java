package com.querybuilder.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Configurable via system properties, so consumers can point this at
    // their own database without editing library source. Falls back to
    // sane local-dev defaults if not set.
    //   -Ddb.url=jdbc:mysql://localhost:3306/your_db
    //   -Ddb.user=your_user
    //   -Ddb.password=your_password
    private static final String URL  = System.getProperty("db.url",      "jdbc:mysql://localhost:3306/querybuilder_db");
    private static final String USER = System.getProperty("db.user",     "root");
    private static final String PASS = System.getProperty("db.password", "");

    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        try {
            this.connection = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("[DB] Connected!");
        } catch (SQLException e) {
            throw new RuntimeException("Connection failed: " + e.getMessage(), e);
        }
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        try {
            if (connection != null) connection.close();
        } catch (SQLException e) {
            System.err.println("Close error: " + e.getMessage());
        } finally {
            instance = null;
        }
    }
}