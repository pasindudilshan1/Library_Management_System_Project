package com.example.library_system.Controller;

import com.example.library_system.Database.*;
import com.example.library_system.Models.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.ResourceBundle;

/**
 * Main controller for the dashboard and overall application coordination.
 * Demonstrates centralized application state management and navigation.
 */
public class MainController implements Initializable {
    
    // Dashboard Statistics Labels
    @FXML private Label totalBooksLabel;
    @FXML private Label totalMembersLabel;
    @FXML private Label issuedBooksLabel;
    @FXML private Label overdueBooksLabel;
    @FXML private Label statusLabel;
    
    // Recent Activity Table
    @FXML private TableView<ActivityRecord> recentActivityTable;
    @FXML private TableColumn<ActivityRecord, String> activityDateColumn;
    @FXML private TableColumn<ActivityRecord, String> activityTypeColumn;
    @FXML private TableColumn<ActivityRecord, String> activityBookColumn;
    @FXML private TableColumn<ActivityRecord, String> activityMemberColumn;
    
    // Available Books Tab
    @FXML private TextField availableBooksSearchField;
    @FXML private TableView<Book> availableBooksTable;
    @FXML private TableColumn<Book, String> availableBookIdColumn;
    @FXML private TableColumn<Book, String> availableBookTitleColumn;
    @FXML private TableColumn<Book, String> availableBookAuthorColumn;
    @FXML private TableColumn<Book, String> availableBookGenreColumn;
    @FXML private TableColumn<Book, String> availableBookCopiesColumn;
    @FXML private TableColumn<Book, String> availableBookISBNColumn;
    
    // Issued Books Tab
    @FXML private TextField issuedBooksSearchField;
    @FXML private TableView<IssueRecord> issuedBooksTable;
    @FXML private TableColumn<IssueRecord, String> issuedBookIdColumn;
    @FXML private TableColumn<IssueRecord, String> issuedBookTitleColumn;
    @FXML private TableColumn<IssueRecord, String> issuedMemberIdColumn;
    @FXML private TableColumn<IssueRecord, String> issuedMemberNameColumn;
    @FXML private TableColumn<IssueRecord, String> issuedDateColumn;
    @FXML private TableColumn<IssueRecord, String> dueDateColumn;
    @FXML private TableColumn<IssueRecord, String> daysOverdueColumn;
    @FXML private TableColumn<IssueRecord, String> fineAmountColumn;
    @FXML private TableColumn<IssueRecord, String> statusColumn;
    
    // Members View Tab
    @FXML private TextField membersSearchField;
    @FXML private CheckBox showInactiveMembersCheckbox;
    @FXML private TableView<Member> membersViewTable;
    @FXML private TableColumn<Member, String> memberIdViewColumn;
    @FXML private TableColumn<Member, String> memberNameViewColumn;
    @FXML private TableColumn<Member, String> memberEmailViewColumn;
    @FXML private TableColumn<Member, String> memberPhoneViewColumn;
    @FXML private TableColumn<Member, String> memberJoinDateColumn;
    @FXML private TableColumn<Member, String> memberActiveIssuesColumn;
    @FXML private TableColumn<Member, String> memberStatusViewColumn;
    
    // DAOs
    private BookDAO bookDAO;
    private MemberDAO memberDAO;
    private IssueRecordDAO issueRecordDAO;
    
    // Observable Lists
    private ObservableList<Book> availableBooksList;
    private ObservableList<IssueRecord> issuedBooksList;
    private ObservableList<Member> membersList;
    private ObservableList<ActivityRecord> recentActivityList;
    
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize DAOs
        bookDAO = new BookDAO();
        memberDAO = new MemberDAO();
        issueRecordDAO = new IssueRecordDAO();
        
        // Initialize observable lists
        availableBooksList = FXCollections.observableArrayList();
        issuedBooksList = FXCollections.observableArrayList();
        membersList = FXCollections.observableArrayList();
        recentActivityList = FXCollections.observableArrayList();
        
        // Initialize tables
        initializeTables();
        
        // Load initial data
        loadDashboardData();
        
        // Set up periodic refresh (every 30 seconds)
        setupPeriodicRefresh();
        
        updateStatusLabel("Application loaded successfully");
    }

    /**
     * Initialize all table columns
     */
    private void initializeTables() {
        setupRecentActivityTable();
        setupAvailableBooksTable();
        setupIssuedBooksTable();
        setupMembersViewTable();
    }

    private void setupRecentActivityTable() {
        if (recentActivityTable != null) {
            activityDateColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getDate()));
            activityTypeColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getType()));
            activityBookColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getBookInfo()));
            activityMemberColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getMemberInfo()));
            
            recentActivityTable.setItems(recentActivityList);
        }
    }

    private void setupAvailableBooksTable() {
        if (availableBooksTable != null) {
            availableBookIdColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getBookId()));
            availableBookTitleColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getTitle()));
            availableBookAuthorColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getAuthor()));
            availableBookGenreColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getGenre()));
            availableBookCopiesColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getAvailableCopies() + "/" + cellData.getValue().getTotalCopies()));
            availableBookISBNColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getIsbn() != null ? cellData.getValue().getIsbn() : ""));
            
            availableBooksTable.setItems(availableBooksList);
        }
    }

    private void setupIssuedBooksTable() {
        if (issuedBooksTable != null) {
            issuedBookIdColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getBookId()));
            issuedBookTitleColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(getBookTitle(cellData.getValue().getBookId())));
            issuedMemberIdColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getMemberId()));
            issuedMemberNameColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(getMemberName(cellData.getValue().getMemberId())));
            issuedDateColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getIssueDate().format(dateFormatter)));
            dueDateColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getDueDate().format(dateFormatter)));
            daysOverdueColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(String.valueOf(cellData.getValue().getDaysOverdue())));
            fineAmountColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(String.format("%.2f", cellData.getValue().getFineAmount())));
            statusColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getStatus().getDisplayName()));
            
            issuedBooksTable.setItems(issuedBooksList);
        }
    }

    private void setupMembersViewTable() {
        if (membersViewTable != null) {
            memberIdViewColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getMemberId()));
            memberNameViewColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getFullName()));
            memberEmailViewColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getEmail()));
            memberPhoneViewColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getPhone() != null ? cellData.getValue().getPhone() : ""));
            memberJoinDateColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getMembershipDate().format(dateFormatter)));
            memberActiveIssuesColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(String.valueOf(getActiveBooksCount(cellData.getValue().getMemberId()))));
            memberStatusViewColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().isActive() ? "Active" : "Inactive"));
            
            membersViewTable.setItems(membersList);
        }
    }

    /**
     * Load dashboard statistics and data
     */
    @FXML
    public void loadDashboardData() {
        try {
            // Update statistics
            updateDashboardStatistics();
            
            // Load table data
            loadAvailableBooks();
            loadIssuedBooks();
            loadMembers();
            loadRecentActivity();
            
            updateStatusLabel("Data refreshed successfully");
            
        } catch (SQLException e) {
            updateStatusLabel("Error loading data: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load dashboard data: " + e.getMessage());
        }
    }

    private void updateDashboardStatistics() throws SQLException {
        long totalBooks = bookDAO.count();
        long totalMembers = memberDAO.count();
        long issued = issueRecordDAO.findActiveIssues().size();
        long overdue = issueRecordDAO.findOverdueBooks().size();
        
        if (totalBooksLabel != null) totalBooksLabel.setText(String.valueOf(totalBooks));
        if (totalMembersLabel != null) totalMembersLabel.setText(String.valueOf(totalMembers));
        if (issuedBooksLabel != null) issuedBooksLabel.setText(String.valueOf(issued));
        if (overdueBooksLabel != null) overdueBooksLabel.setText(String.valueOf(overdue));
    }

    private void loadAvailableBooks() throws SQLException {
        List<Book> books = bookDAO.findAvailableBooks();
        availableBooksList.clear();
        availableBooksList.addAll(books);
    }

    private void loadIssuedBooks() throws SQLException {
        List<IssueRecord> issues = issueRecordDAO.findActiveIssues();
        // Update overdue status before displaying
        issueRecordDAO.updateOverdueStatus();
        
        issuedBooksList.clear();
        issuedBooksList.addAll(issues);
    }

    private void loadMembers() throws SQLException {
        boolean showInactive = showInactiveMembersCheckbox != null && showInactiveMembersCheckbox.isSelected();
        List<Member> members = showInactive ? memberDAO.findAll() : memberDAO.findActiveMembers();
        membersList.clear();
        membersList.addAll(members);
    }

    private void loadRecentActivity() {
        // Simulate recent activity - in a real system, this would come from an audit log
        recentActivityList.clear();
        try {
            List<IssueRecord> recentIssues = issueRecordDAO.findActiveIssues();
            for (int i = 0; i < Math.min(5, recentIssues.size()); i++) {
                IssueRecord record = recentIssues.get(i);
                ActivityRecord activity = new ActivityRecord(
                    record.getIssueDate().format(dateFormatter),
                    "Book Issued",
                    getBookTitle(record.getBookId()),
                    getMemberName(record.getMemberId())
                );
                recentActivityList.add(activity);
            }
        } catch (SQLException e) {
            updateStatusLabel("Error loading recent activity: " + e.getMessage());
        }
    }

    /**
     * Search functions for each tab
     */
    @FXML
    public void searchAvailableBooks() {
        String query = availableBooksSearchField != null ? availableBooksSearchField.getText() : "";
        try {
            List<Book> books = query.trim().isEmpty() ? 
                bookDAO.findAvailableBooks() : 
                bookDAO.search(query).stream()
                    .filter(Book::isAvailable)
                    .toList();
            
            availableBooksList.clear();
            availableBooksList.addAll(books);
            updateStatusLabel("Found " + books.size() + " available books");
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Search Error", "Failed to search books: " + e.getMessage());
        }
    }

    @FXML
    public void searchIssuedBooks() {
        String query = issuedBooksSearchField != null ? issuedBooksSearchField.getText() : "";
        try {
            List<IssueRecord> issues = issueRecordDAO.findActiveIssues();
            
            if (!query.trim().isEmpty()) {
                String searchTerm = query.toLowerCase();
                issues = issues.stream()
                    .filter(issue -> 
                        issue.getBookId().toLowerCase().contains(searchTerm) ||
                        issue.getMemberId().toLowerCase().contains(searchTerm) ||
                        getBookTitle(issue.getBookId()).toLowerCase().contains(searchTerm) ||
                        getMemberName(issue.getMemberId()).toLowerCase().contains(searchTerm))
                    .toList();
            }
            
            issuedBooksList.clear();
            issuedBooksList.addAll(issues);
            updateStatusLabel("Found " + issues.size() + " issued books");
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Search Error", "Failed to search issued books: " + e.getMessage());
        }
    }

    @FXML
    public void searchMembers() {
        String query = membersSearchField != null ? membersSearchField.getText() : "";
        try {
            List<Member> members = memberDAO.search(query);
            membersList.clear();
            membersList.addAll(members);
            updateStatusLabel("Found " + members.size() + " members");
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Search Error", "Failed to search members: " + e.getMessage());
        }
    }

    /**
     * Refresh functions
     */
    @FXML
    public void refreshAvailableBooks() {
        try {
            loadAvailableBooks();
            if (availableBooksSearchField != null) {
                availableBooksSearchField.clear();
            }
            updateStatusLabel("Available books refreshed");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Refresh Error", "Failed to refresh available books: " + e.getMessage());
        }
    }

    @FXML
    public void refreshIssuedBooks() {
        try {
            loadIssuedBooks();
            if (issuedBooksSearchField != null) {
                issuedBooksSearchField.clear();
            }
            updateStatusLabel("Issued books refreshed");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Refresh Error", "Failed to refresh issued books: " + e.getMessage());
        }
    }

    @FXML
    public void refreshMembers() {
        try {
            loadMembers();
            if (membersSearchField != null) {
                membersSearchField.clear();
            }
            updateStatusLabel("Members list refreshed");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Refresh Error", "Failed to refresh members: " + e.getMessage());
        }
    }

    @FXML
    public void showOverdueOnly() {
        try {
            List<IssueRecord> overdueBooks = issueRecordDAO.findOverdueBooks();
            issuedBooksList.clear();
            issuedBooksList.addAll(overdueBooks);
            updateStatusLabel("Showing " + overdueBooks.size() + " overdue books");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load overdue books: " + e.getMessage());
        }
    }

    @FXML
    public void toggleInactiveMembers() {
        try {
            loadMembers();
            updateStatusLabel("Members list updated");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to toggle member view: " + e.getMessage());
        }
    }



    /**
     * Helper methods
     */
    private String getBookTitle(String bookId) {
        try {
            return bookDAO.findById(bookId)
                .map(Book::getTitle)
                .orElse("Unknown Book");
        } catch (SQLException e) {
            return "Error loading book";
        }
    }

    private String getMemberName(String memberId) {
        try {
            return memberDAO.findById(memberId)
                .map(Member::getFullName)
                .orElse("Unknown Member");
        } catch (SQLException e) {
            return "Error loading member";
        }
    }

    private int getActiveBooksCount(String memberId) {
        try {
            return issueRecordDAO.findMemberActiveIssues(memberId).size();
        } catch (SQLException e) {
            return 0;
        }
    }

    private void updateStatusLabel(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    private void setupPeriodicRefresh() {
        // Refresh dashboard every 30 seconds
        Thread refreshThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(30000); // 30 seconds
                    Platform.runLater(() -> {
                        try {
                            updateDashboardStatistics();
                        } catch (SQLException e) {
                            updateStatusLabel("Auto-refresh failed: " + e.getMessage());
                        }
                    });
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        refreshThread.setDaemon(true);
        refreshThread.start();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show error alert (alias for consistency with other controllers)
     */
    private void showError(String title, String message) {
        showAlert(Alert.AlertType.ERROR, title, message);
    }

    /**
     * Menu Action: Exit Application
     */
    @FXML
    protected void exitApplication() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Exit Application");
        confirmAlert.setHeaderText("Confirm Exit");
        confirmAlert.setContentText("Are you sure you want to exit the Library Management System?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Platform.exit();
            System.exit(0);
        }
    }

    /**
     * Menu Action: Show Overdue Report
     */
    @FXML
    protected void showOverdueReport() {
        try {
            List<IssueRecord> overdueRecords = issueRecordDAO.findOverdueIssues();
            
            StringBuilder report = new StringBuilder();
            report.append("📋 OVERDUE BOOKS REPORT\n");
            report.append("=".repeat(50)).append("\n\n");
            
            if (overdueRecords.isEmpty()) {
                report.append("✅ No overdue books found!");
            } else {
                report.append(String.format("Total Overdue Books: %d\n\n", overdueRecords.size()));
                
                for (IssueRecord record : overdueRecords) {
                    try {
                        Optional<Book> book = bookDAO.findById(record.getBookId());
                        Optional<Member> member = memberDAO.findById(record.getMemberId());
                        
                        if (book.isPresent() && member.isPresent()) {
                            long overdueDays = ChronoUnit.DAYS.between(record.getDueDate(), LocalDate.now());
                            BigDecimal fine = new BigDecimal("5.00").multiply(new BigDecimal(overdueDays));
                            
                            report.append(String.format(
                                "📖 %s\n" +
                                "👤 %s %s\n" +
                                "📅 Due: %s (%d days overdue)\n" +
                                "💰 Fine: RS%s\n" +
                                "-".repeat(30) + "\n",
                                book.get().getTitle(),
                                member.get().getFirstName(),
                                member.get().getLastName(),
                                record.getDueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                                overdueDays,
                                fine.toString()
                            ));
                        }
                    } catch (SQLException e) {
                        // Continue with next record
                    }
                }
            }
            
            Alert reportAlert = new Alert(Alert.AlertType.INFORMATION);
            reportAlert.setTitle("Overdue Books Report");
            reportAlert.setHeaderText(null);
            reportAlert.getDialogPane().setContent(new ScrollPane(new TextArea(report.toString())));
            reportAlert.getDialogPane().setPrefSize(600, 400);
            reportAlert.showAndWait();
            
        } catch (SQLException e) {
            showError("Database Error", "Failed to generate overdue report: " + e.getMessage());
        }
    }

    /**
     * Menu Action: Show Statistics
     */
    @FXML
    protected void showStatistics() {
        try {
            List<Book> allBooks = bookDAO.findAll();
            List<Member> allMembers = memberDAO.findAll();
            List<IssueRecord> activeIssues = issueRecordDAO.findActiveIssues();
            List<IssueRecord> overdueIssues = issueRecordDAO.findOverdueIssues();
            
            // Calculate statistics
            int totalBooks = allBooks.size();
            int totalCopies = allBooks.stream().mapToInt(Book::getTotalCopies).sum();
            int availableCopies = allBooks.stream().mapToInt(Book::getAvailableCopies).sum();
            int issuedCopies = totalCopies - availableCopies;
            
            int totalMembers = allMembers.size();
            int activeMembers = (int) allMembers.stream().filter(Member::isActive).count();
            
            int totalActiveIssues = activeIssues.size();
            int totalOverdueIssues = overdueIssues.size();
            
            // Calculate total fines
            BigDecimal totalFines = BigDecimal.ZERO;
            for (IssueRecord record : overdueIssues) {
                long overdueDays = ChronoUnit.DAYS.between(record.getDueDate(), LocalDate.now());
                totalFines = totalFines.add(new BigDecimal("5.00").multiply(new BigDecimal(overdueDays)));
            }
            
            // Popular categories
            Map<String, Integer> categoryStats = allBooks.stream()
                .collect(Collectors.groupingBy(Book::getGenre, Collectors.summingInt(Book::getTotalCopies)));
            String mostPopularCategory = categoryStats.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
            
            StringBuilder stats = new StringBuilder();
            stats.append("📊 LIBRARY STATISTICS\n");
            stats.append("=".repeat(50)).append("\n\n");
            
            stats.append("📚 BOOK STATISTICS\n");
            stats.append(String.format("Total Books: %d\n", totalBooks));
            stats.append(String.format("Total Copies: %d\n", totalCopies));
            stats.append(String.format("Available Copies: %d\n", availableCopies));
            stats.append(String.format("Issued Copies: %d\n", issuedCopies));
            stats.append(String.format("Most Popular Category: %s\n\n", mostPopularCategory));
            
            stats.append("👥 MEMBER STATISTICS\n");
            stats.append(String.format("Total Members: %d\n", totalMembers));
            stats.append(String.format("Active Members: %d\n", activeMembers));
            stats.append(String.format("Inactive Members: %d\n\n", totalMembers - activeMembers));
            
            stats.append("📖 ISSUE STATISTICS\n");
            stats.append(String.format("Active Issues: %d\n", totalActiveIssues));
            stats.append(String.format("Overdue Issues: %d\n", totalOverdueIssues));
            stats.append(String.format("On-time Issues: %d\n", totalActiveIssues - totalOverdueIssues));
            stats.append(String.format("Total Outstanding Fines: RS%s\n", totalFines.toString()));
            
            Alert statsAlert = new Alert(Alert.AlertType.INFORMATION);
            statsAlert.setTitle("Library Statistics");
            statsAlert.setHeaderText(null);
            statsAlert.getDialogPane().setContent(new ScrollPane(new TextArea(stats.toString())));
            statsAlert.getDialogPane().setPrefSize(600, 400);
            statsAlert.showAndWait();
            
        } catch (SQLException e) {
            showError("Database Error", "Failed to generate statistics: " + e.getMessage());
        }
    }

    /**
     * Menu Action: Show About
     */
    @FXML
    protected void showAbout() {
        Alert aboutAlert = new Alert(Alert.AlertType.INFORMATION);
        aboutAlert.setTitle("About Library Management System");
        aboutAlert.setHeaderText("📚 Library Management System v1.0");
        aboutAlert.setContentText(
            "A comprehensive library management system built with:\n\n" +
            "🔧 Technology Stack:\n" +
            "• JavaFX for modern UI\n" +
            "• MySQL database via XAMPP\n" +
            "• Maven for build management\n" +
            "• MVC architecture with DAO pattern\n\n" +
            "✨ Features:\n" +
            "• Book inventory management\n" +
            "• Member registration and tracking\n" +
            "• Book issuing and returning\n" +
            "• Fine calculation system\n" +
            "• Real-time dashboard\n" +
            "• Comprehensive reporting\n\n" +
            "🎯 Programming Concepts Demonstrated:\n" +
            "• Object-Oriented Programming\n" +
            "• Database Integration\n" +
            "• Error Handling & Validation\n" +
            "• Transaction Management\n" +
            "• UI/UX Best Practices\n\n" +
            "🚀 Built for educational and practical use!"
        );
        aboutAlert.showAndWait();
    }

    /**
     * Inner class for recent activity records
     */
    public static class ActivityRecord {
        private String date;
        private String type;
        private String bookInfo;
        private String memberInfo;

        public ActivityRecord(String date, String type, String bookInfo, String memberInfo) {
            this.date = date;
            this.type = type;
            this.bookInfo = bookInfo;
            this.memberInfo = memberInfo;
        }

        public String getDate() { return date; }
        public String getType() { return type; }
        public String getBookInfo() { return bookInfo; }
        public String getMemberInfo() { return memberInfo; }
    }
}