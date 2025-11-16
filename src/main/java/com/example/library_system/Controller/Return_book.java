package com.example.library_system.Controller;

import com.example.library_system.Database.*;
import com.example.library_system.Models.*;
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
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for book return operations.
 * Demonstrates fine calculation and transaction completion.
 */
public class Return_book implements Initializable {
    
    // Return Form Components
    @FXML private ComboBox<IssueRecord> issueRecordComboBox;
    @FXML private DatePicker returnDatePicker;
    @FXML private TextArea returnNotesField;
    @FXML private TextField fineAmountField;
    @FXML private Button returnButton;
    
    // Search Components
    @FXML private TextField searchField;
    
    // Issue Records Table
    @FXML private TableView<IssueRecord> issuedBooksTable;
    @FXML private TableColumn<IssueRecord, String> issueIdColumn;
    @FXML private TableColumn<IssueRecord, String> issuedBookIdColumn;
    @FXML private TableColumn<IssueRecord, String> issuedBookTitleColumn;
    @FXML private TableColumn<IssueRecord, String> issuedMemberIdColumn;
    @FXML private TableColumn<IssueRecord, String> issuedMemberNameColumn;
    @FXML private TableColumn<IssueRecord, String> issueDateColumn;
    @FXML private TableColumn<IssueRecord, String> dueDateColumn;
    @FXML private TableColumn<IssueRecord, String> daysOverdueColumn;
    @FXML private TableColumn<IssueRecord, String> fineColumn;
    @FXML private TableColumn<IssueRecord, String> statusColumn;
    
    // Search and filter fields
    @FXML private TextField issuedBooksSearchField;
    
    // Information labels
    @FXML private Label issueBookLabel;
    @FXML private Label issueMemberLabel;
    @FXML private Label issueDateLabel;
    @FXML private Label dueDateLabel;
    @FXML private Label daysOverdueLabel;
    @FXML private Label fineAmountLabel;
    
    // Statistics labels
    @FXML private Label totalIssuedLabel;
    @FXML private Label overdueCountLabel;
    @FXML private Label totalFinesLabel;
    @FXML private Label statusLabel;
    
    // Data Access Objects
    private IssueRecordDAO issueRecordDAO;
    private BookDAO bookDAO;
    private MemberDAO memberDAO;
    
    // Observable Lists
    private ObservableList<IssueRecord> issuedRecordsList;
    
    // Constants
    private static final BigDecimal DAILY_FINE = new BigDecimal("5.00"); // RS5 per day

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize DAOs
        issueRecordDAO = new IssueRecordDAO();
        bookDAO = new BookDAO();
        memberDAO = new MemberDAO();
        
        // Setup table columns
        setupTableColumns();
        
        // Setup combo box
        setupIssueRecordComboBox();
        
        // Setup return date picker
        if (returnDatePicker != null) {
            returnDatePicker.setValue(LocalDate.now());
        }
        
        // Load issued books
        loadIssuedBooks();
        
        // Setup table selection listener
        setupTableSelectionListener();
        
        // Make fine field read-only
        if (fineAmountField != null) {
            fineAmountField.setEditable(false);
        }
    }

    /**
     * Setup table columns
     */
    private void setupTableColumns() {
        if (issueIdColumn != null) {
            issueIdColumn.setCellValueFactory(data -> 
                new SimpleStringProperty(String.valueOf(data.getValue().getIssueId())));
        }
        if (issuedBookIdColumn != null) {
            issuedBookIdColumn.setCellValueFactory(data -> 
                new SimpleStringProperty(data.getValue().getBookId()));
        }
        if (issuedBookTitleColumn != null) {
            issuedBookTitleColumn.setCellValueFactory(data -> {
                try {
                    Optional<Book> book = bookDAO.findById(data.getValue().getBookId());
                    if (book.isPresent()) {
                        return new SimpleStringProperty(book.get().getTitle());
                    }
                    return new SimpleStringProperty("Unknown Book");
                } catch (SQLException e) {
                    return new SimpleStringProperty("Error loading");
                }
            });
        }
        if (issuedMemberIdColumn != null) {
            issuedMemberIdColumn.setCellValueFactory(data -> 
                new SimpleStringProperty(data.getValue().getMemberId()));
        }
        if (issuedMemberNameColumn != null) {
            issuedMemberNameColumn.setCellValueFactory(data -> {
                try {
                    Optional<Member> member = memberDAO.findById(data.getValue().getMemberId());
                    if (member.isPresent()) {
                        return new SimpleStringProperty(member.get().getFirstName() + " " + member.get().getLastName());
                    }
                    return new SimpleStringProperty("Unknown Member");
                } catch (SQLException e) {
                    return new SimpleStringProperty("Error loading");
                }
            });
        }
        if (issueDateColumn != null) {
            issueDateColumn.setCellValueFactory(data -> 
                new SimpleStringProperty(data.getValue().getIssueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
        }
        if (dueDateColumn != null) {
            dueDateColumn.setCellValueFactory(data -> 
                new SimpleStringProperty(data.getValue().getDueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
        }
        if (daysOverdueColumn != null) {
            daysOverdueColumn.setCellValueFactory(data -> {
                LocalDate dueDate = data.getValue().getDueDate();
                LocalDate today = LocalDate.now();
                if (today.isAfter(dueDate)) {
                    long overdueDays = ChronoUnit.DAYS.between(dueDate, today);
                    return new SimpleStringProperty(String.valueOf(overdueDays));
                } else {
                    return new SimpleStringProperty("0");
                }
            });
        }
        if (fineColumn != null) {
            fineColumn.setCellValueFactory(data -> {
                LocalDate dueDate = data.getValue().getDueDate();
                LocalDate today = LocalDate.now();
                if (today.isAfter(dueDate)) {
                    long overdueDays = ChronoUnit.DAYS.between(dueDate, today);
                    BigDecimal fine = DAILY_FINE.multiply(new BigDecimal(overdueDays));
                    return new SimpleStringProperty(String.format("%.2f", fine));
                } else {
                    return new SimpleStringProperty("0.00");
                }
            });
        }
        if (statusColumn != null) {
            statusColumn.setCellValueFactory(data -> {
                LocalDate dueDate = data.getValue().getDueDate();
                LocalDate today = LocalDate.now();
                if (today.isAfter(dueDate)) {
                    return new SimpleStringProperty("Overdue");
                } else {
                    return new SimpleStringProperty("On Time");
                }
            });
        }
    }

    /**
     * Setup issue record combo box
     */
    private void setupIssueRecordComboBox() {
        try {
            if (issueRecordComboBox != null) {
                List<IssueRecord> activeIssues = issueRecordDAO.findActiveIssues();
                ObservableList<IssueRecord> issueItems = FXCollections.observableArrayList(activeIssues);
                issueRecordComboBox.setItems(issueItems);
                issueRecordComboBox.setConverter(new javafx.util.StringConverter<IssueRecord>() {
                    @Override
                    public String toString(IssueRecord issueRecord) {
                        if (issueRecord == null) return "";
                        try {
                            Optional<Member> member = memberDAO.findById(issueRecord.getMemberId());
                            Optional<Book> book = bookDAO.findById(issueRecord.getBookId());
                            String memberName = member.isPresent() ? member.get().getFirstName() + " " + member.get().getLastName() : "Unknown";
                            String bookTitle = book.isPresent() ? book.get().getTitle() : "Unknown";
                            return String.format("ID:%d - %s - %s (Due: %s)", 
                                issueRecord.getIssueId(), 
                                memberName, 
                                bookTitle,
                                issueRecord.getDueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        } catch (SQLException e) {
                            return "Error loading details";
                        }
                    }
                    @Override
                    public IssueRecord fromString(String string) {
                        return null; // Not needed for display
                    }
                });
                
                // Add listener for selection changes
                issueRecordComboBox.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> {
                        if (newValue != null) {
                            populateReturnForm(newValue);
                        }
                    }
                );
            }
        } catch (SQLException e) {
            showError("Database Error", "Failed to load issue records for dropdown: " + e.getMessage());
        }
    }

    /**
     * Setup table selection listener
     */
    private void setupTableSelectionListener() {
        if (issuedBooksTable != null) {
            issuedBooksTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        populateReturnForm(newValue);
                    }
                }
            );
        }
    }

    /**
     * Populate return form with selected issue record
     */
    private void populateReturnForm(IssueRecord issueRecord) {
        try {
            // Get book and member details
            Optional<Book> book = bookDAO.findById(issueRecord.getBookId());
            Optional<Member> member = memberDAO.findById(issueRecord.getMemberId());
            
            // Update information labels
            if (issueBookLabel != null) {
                String bookInfo = book.isPresent() ? 
                    book.get().getTitle() + " (" + book.get().getBookId() + ")" : 
                    "Unknown Book";
                issueBookLabel.setText("Book: " + bookInfo);
            }
            
            if (issueMemberLabel != null) {
                String memberInfo = member.isPresent() ? 
                    member.get().getFirstName() + " " + member.get().getLastName() + " (" + member.get().getMemberId() + ")" : 
                    "Unknown Member";
                issueMemberLabel.setText("Member: " + memberInfo);
            }
            
            if (issueDateLabel != null) {
                issueDateLabel.setText("Issue Date: " + issueRecord.getIssueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
            
            if (dueDateLabel != null) {
                dueDateLabel.setText("Due Date: " + issueRecord.getDueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
            
            // Calculate overdue days
            LocalDate today = LocalDate.now();
            long overdueDays = 0;
            if (today.isAfter(issueRecord.getDueDate())) {
                overdueDays = ChronoUnit.DAYS.between(issueRecord.getDueDate(), today);
            }
            
            if (daysOverdueLabel != null) {
                daysOverdueLabel.setText("Days Overdue: " + overdueDays);
            }
            
            // Calculate fine
            BigDecimal fine = DAILY_FINE.multiply(new BigDecimal(overdueDays));
            if (fineAmountLabel != null) {
                fineAmountLabel.setText("Fine Amount: Rs. " + String.format("%.2f", fine));
            }
            
            // Set combo box value
            if (issueRecordComboBox != null) {
                issueRecordComboBox.setValue(issueRecord);
            }
            
            // Calculate and display fine in the field
            calculateFine(issueRecord.getDueDate());
            
            // Enable return button
            if (returnButton != null) {
                returnButton.setDisable(false);
            }
            
        } catch (SQLException e) {
            showError("Database Error", "Failed to load details: " + e.getMessage());
        }
    }

    /**
     * Calculate fine amount based on overdue days
     */
    private void calculateFine(LocalDate dueDate) {
        LocalDate returnDate = returnDatePicker != null ? returnDatePicker.getValue() : LocalDate.now();
        
        if (returnDate.isAfter(dueDate)) {
            long overdueDays = ChronoUnit.DAYS.between(dueDate, returnDate);
            BigDecimal fine = DAILY_FINE.multiply(new BigDecimal(overdueDays));
            
            if (fineAmountField != null) {
                fineAmountField.setText("RS" + fine.toString());
            }
        } else {
            if (fineAmountField != null) {
                fineAmountField.setText("RS0.00");
            }
        }
    }

    /**
     * Search issue records
     */
    @FXML
    protected void searchIssueRecord() {
        try {
            String searchTerm = searchField != null ? searchField.getText().trim() : "";
            
            List<IssueRecord> searchResults;
            if (searchTerm.isEmpty()) {
                searchResults = issueRecordDAO.findActiveIssues();
            } else {
                // Search by member ID or book ID
                searchResults = issueRecordDAO.findActiveIssues().stream()
                    .filter(record -> 
                        record.getMemberId().contains(searchTerm) || 
                        record.getBookId().contains(searchTerm))
                    .toList();
            }
            
            issuedRecordsList = FXCollections.observableArrayList(searchResults);
            if (issuedBooksTable != null) {
                issuedBooksTable.setItems(issuedRecordsList);
            }
            
        } catch (SQLException e) {
            showError("Search Error", "Failed to search issue records: " + e.getMessage());
        }
    }

    /**
     * Return a book
     */
    @FXML
    protected void returnBook() {
        try {
            IssueRecord selectedRecord = null;
            
            // Check ComboBox first, then table selection
            if (issueRecordComboBox != null && issueRecordComboBox.getValue() != null) {
                selectedRecord = issueRecordComboBox.getValue();
            } else if (issuedBooksTable != null) {
                selectedRecord = issuedBooksTable.getSelectionModel().getSelectedItem();
            }
            
            if (selectedRecord == null) {
                showWarning("No Selection", "Please select an issue record from the dropdown or table to process return.");
                return;
            }
            
            LocalDate returnDate = returnDatePicker.getValue();
            if (returnDate == null) {
                showWarning("Invalid Date", "Please select a valid return date.");
                return;
            }
            
            // Calculate fine
            BigDecimal fineAmount = BigDecimal.ZERO;
            if (returnDate.isAfter(selectedRecord.getDueDate())) {
                long overdueDays = ChronoUnit.DAYS.between(selectedRecord.getDueDate(), returnDate);
                fineAmount = DAILY_FINE.multiply(new BigDecimal(overdueDays));
            }
            
            // Confirm return with fine if applicable
            String confirmMessage = String.format(
                "Confirm book return?\n\nIssue ID: %d\nReturn Date: %s\nFine Amount: RS%s",
                selectedRecord.getIssueId(),
                returnDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                fineAmount.toString()
            );
            
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Return");
            confirmAlert.setHeaderText("Process Book Return");
            confirmAlert.setContentText(confirmMessage);
            
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Update issue record
                selectedRecord.setStatus(IssueRecord.Status.RETURNED);
                selectedRecord.setReturnDate(returnDate);
                selectedRecord.setFineAmount(fineAmount);
                
                // Save changes
                issueRecordDAO.update(selectedRecord);
                
                // Get book and member info for success message
                Optional<Book> book = bookDAO.findById(selectedRecord.getBookId());
                Optional<Member> member = memberDAO.findById(selectedRecord.getMemberId());
                
                String successMessage = "Book returned successfully!";
                if (book.isPresent() && member.isPresent()) {
                    successMessage = String.format(
                        "Book '%s' returned by %s %s\nReturn Date: %s\nFine: RS%s",
                        book.get().getTitle(),
                        member.get().getFirstName(),
                        member.get().getLastName(),
                        returnDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        fineAmount.toString()
                    );
                }
                
                showSuccess("Return Successful", successMessage);
                
                // Clear form and refresh table and combo box
                clearReturnForm();
                loadIssuedBooks();
                setupIssueRecordComboBox();
            }
            
        } catch (SQLException e) {
            showError("Database Error", "Failed to process book return: " + e.getMessage());
        }
    }

    /**
     * Search issued books
     */
    @FXML
    protected void searchIssuedBooks() {
        searchIssueRecord(); // Same functionality
    }

    /**
     * Refresh issued books table
     */
    @FXML
    protected void refreshIssuedBooks() {
        loadIssuedBooks();
        if (searchField != null) {
            searchField.clear();
        }
    }

    /**
     * Show only overdue books
     */
    @FXML
    protected void showOverdueOnly() {
        try {
            List<IssueRecord> overdueRecords = issueRecordDAO.findOverdueIssues();
            issuedRecordsList = FXCollections.observableArrayList(overdueRecords);
            if (issuedBooksTable != null) {
                issuedBooksTable.setItems(issuedRecordsList);
            }
        } catch (SQLException e) {
            showError("Database Error", "Failed to load overdue records: " + e.getMessage());
        }
    }

    /**
     * Load all issued books
     */
    private void loadIssuedBooks() {
        try {
            List<IssueRecord> activeIssues = issueRecordDAO.findActiveIssues();
            issuedRecordsList = FXCollections.observableArrayList(activeIssues);
            if (issuedBooksTable != null) {
                issuedBooksTable.setItems(issuedRecordsList);
            }
            updateStatistics();
        } catch (SQLException e) {
            showError("Database Error", "Failed to load issued books: " + e.getMessage());
        }
    }
    
    /**
     * Update statistics labels
     */
    private void updateStatistics() {
        if (issuedRecordsList == null) return;
        
        int totalIssued = issuedRecordsList.size();
        int overdueCount = 0;
        BigDecimal totalFines = BigDecimal.ZERO;
        LocalDate today = LocalDate.now();
        
        for (IssueRecord record : issuedRecordsList) {
            if (today.isAfter(record.getDueDate())) {
                overdueCount++;
                long overdueDays = ChronoUnit.DAYS.between(record.getDueDate(), today);
                totalFines = totalFines.add(DAILY_FINE.multiply(new BigDecimal(overdueDays)));
            }
        }
        
        if (totalIssuedLabel != null) {
            totalIssuedLabel.setText(String.valueOf(totalIssued));
        }
        if (overdueCountLabel != null) {
            overdueCountLabel.setText(String.valueOf(overdueCount));
        }
        if (totalFinesLabel != null) {
            totalFinesLabel.setText(String.format("Rs. %.2f", totalFines));
        }
    }

    /**
     * Clear return form
     */
    private void clearReturnForm() {
        if (issueRecordComboBox != null) issueRecordComboBox.setValue(null);
        if (returnNotesField != null) returnNotesField.clear();
        if (fineAmountField != null) fineAmountField.setText("RS0.00");
        if (returnDatePicker != null) returnDatePicker.setValue(LocalDate.now());
        
        // Clear information labels
        if (issueBookLabel != null) issueBookLabel.setText("Book: Not selected");
        if (issueMemberLabel != null) issueMemberLabel.setText("Member: Not selected");
        if (issueDateLabel != null) issueDateLabel.setText("Issue Date: -");
        if (dueDateLabel != null) dueDateLabel.setText("Due Date: -");
        if (daysOverdueLabel != null) daysOverdueLabel.setText("Days Overdue: -");
        if (fineAmountLabel != null) fineAmountLabel.setText("Fine Amount: Rs. 0.00");
        
        // Disable return button
        if (returnButton != null) {
            returnButton.setDisable(true);
        }
        
        // Clear table selection
        if (issuedBooksTable != null) {
            issuedBooksTable.getSelectionModel().clearSelection();
        }
    }

    /**
     * Show error alert
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show success alert
     */
    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show warning alert
     */
    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
