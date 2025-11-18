# 📚 Library Management System

A comprehensive library management system built with JavaFX and MySQL, demonstrating core programming concepts including object-oriented programming, database integration, and proper software architecture patterns.

## 🔧 Features

### Core Functionality
- **Book Management**: Add, update, delete, and search books with comprehensive validation
- **Member Management**: Manage library members with email validation and status tracking
- **Issue/Return System**: Track book borrowing with automatic due dates and fine calculation
- **Real-time Availability**: Track available vs. issued book copies with automatic updates
- **Overdue Tracking**: Automatic fine calculation for overdue books (Rs. 5 per day)
- **Advanced Search**: Search across books by title, author, genre with filtering

### Technical Highlights
- **Object-Oriented Design**: Proper encapsulation, inheritance, and polymorphism
- **Data Validation**: Comprehensive input validation with custom exceptions
- **DAO Pattern**: Clean separation between data access and business logic
- **Database Transactions**: ACID compliance for critical operations like book issue/return
- **Modern Java Features**: LocalDate/LocalDateTime, Optional, BigDecimal for precision
- **JavaFX GUI**: User-friendly graphical interface with proper event handling

## 🛠️ Prerequisites

### Required Software
1. **Java 17 or higher** - Download from [Oracle JDK](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html) or [OpenJDK](https://openjdk.java.net/)
2. **XAMPP** (for MySQL database) - Download from [Apache Friends](https://www.apachefriends.org/)
3. **IntelliJ IDEA** (Community Edition is free) - Download from [JetBrains](https://www.jetbrains.com/idea/download/)

### XAMPP Setup
1. Download and install XAMPP
2. Start Apache and MySQL services from XAMPP Control Panel
3. Open phpMyAdmin (usually at http://localhost/phpmyadmin)

## 📥 Downloading and Installing IntelliJ IDEA

1. Go to the [IntelliJ IDEA download page](https://www.jetbrains.com/idea/download/)
2. Choose the Community Edition (free and open-source)
3. Download the installer for your operating system (Windows, macOS, or Linux)
4. Run the installer and follow the setup wizard
5. Launch IntelliJ IDEA after installation

## 🚀 Setup and Run via IntelliJ IDEA


```

### 2. Open the Project in IntelliJ IDEA
1. Launch IntelliJ IDEA
2. Click on "Open" from the welcome screen
3. Navigate to the cloned `Library_System` folder and select it
4. IntelliJ will automatically detect it as a Maven project and import it

### 3. Configure JDK
1. In IntelliJ, go to `File` > `Project Structure`
2. Under `Project SDK`, ensure Java 17 is selected
3. If not available, click `Add SDK` > `Download JDK` and download Java 17

### 4. Database Setup
1. Open phpMyAdmin in your web browser
2. Click "New" to create a new database
3. Name it `library_system`
4. Set collation to `utf8mb4_general_ci`
5. Click "Create"

### 5. Import Database Schema
1. Select the `library_system` database
2. Click on the "Import" tab
3. Choose the `database_schema.sql` file from the project root
4. Click "Go" to execute the script

This creates all necessary tables with sample data and triggers for automatic inventory management.

### 6. Run the Application
1. In IntelliJ, expand the project structure in the left pane
2. Navigate to `src/main/java/com/example/library_system/Main.java`
3. Right-click on `Main.java` and select `Run 'Main.main()'`


The application will start and open the JavaFX GUI window.

