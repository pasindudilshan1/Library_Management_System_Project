package com.example.library_system.Database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    
    private static String URL;
    private static String USER;
    private static String PASSWORD;
    
    static {
        loadProperties();
    }
    
    private static void loadProperties() {
        try (InputStream input = DBConnection.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            
            if (input == null) {
                System.err.println("Unable to find application.properties");
                return;
            }
            
            Properties prop = new Properties();
            prop.load(input);
            
            URL = prop.getProperty("db.url");
            USER = prop.getProperty("db.username");
            PASSWORD = prop.getProperty("db.password");
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static Connection connect() {
        try {
            // Load driver (optional for JDBC 4.0+, but safe to include)
            if (URL.contains("mysql")) {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } else if (URL.contains("sqlite")) {
                Class.forName("org.sqlite.JDBC");
            }
            
            // For SQLite (no credentials)
            if (USER == null || USER.isEmpty()) {
                return DriverManager.getConnection(URL);
            }
            
            // For MySQL/PostgreSQL (with credentials)
            return DriverManager.getConnection(URL, USER, PASSWORD);
            
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}