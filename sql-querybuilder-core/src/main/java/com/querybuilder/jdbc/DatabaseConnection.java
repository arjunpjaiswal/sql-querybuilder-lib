package com.querybuilder.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static final String URL  = "jdbc:mysql://localhost:3306/querybuilder_db";
    private static final String USER = "root";
    private static final String PASS = "Arjun";

    private static DatabaseConnection instance;  // no volatile needed
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
            instance = new DatabaseConnection();  // no double-checked locking needed
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