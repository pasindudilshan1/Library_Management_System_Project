package com.example.library_system;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Main application class for the Library Management System.
 * Entry point for the JavaFX application.
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        try {
            var fxml = getClass().getResource("/com/example/library_system/main.fxml");
            if (fxml == null) {
                throw new RuntimeException("Main FXML file not found!");
            }

            Parent root = FXMLLoader.load(fxml);
            Scene scene = new Scene(root);
            
            // Add CSS styling if available
            var cssResource = getClass().getResource("/com/example/library_system/styles.css");
            if (cssResource != null) {
                scene.getStylesheets().add(cssResource.toExternalForm());
            }

            // Configure the stage
            stage.setScene(scene);
            stage.setTitle("📚 Library Management System - XAMPP Edition");
            stage.setMinWidth(1200);
            stage.setMinHeight(800);
            stage.setMaximized(true);
            
            // Set application icon if available
            try {
                var iconResource = getClass().getResourceAsStream("/com/example/library_system/icon.png");
                if (iconResource != null) {
                    stage.getIcons().add(new Image(iconResource));
                }
            } catch (Exception iconException) {
                System.out.println("Application icon not found, using default.");
            }
            
            // Center on screen
            stage.centerOnScreen();
            
            stage.show();
            
            System.out.println("Library Management System started successfully!");
            System.out.println("Database: MySQL via XAMPP");
            System.out.println("Make sure XAMPP MySQL service is running and database 'library_system' exists.");
            
        } catch (Exception e) {
            System.err.println("Failed to start application: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public static void main(String[] args) {
        System.out.println("Starting Library Management System...");
        System.out.println("Checking JavaFX version: " + System.getProperty("javafx.version"));
        System.out.println("Java version: " + System.getProperty("java.version"));
        
        launch(args);
    }
}