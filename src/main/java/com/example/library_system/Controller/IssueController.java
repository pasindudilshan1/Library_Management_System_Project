package com.example.library_system.Controller;

import com.example.library_system.Database.*;
import com.example.library_system.Models.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for book issuing operations.
 * Demonstrates transaction management and business rule validation.
 */
public class IssueController implements Initializable {
    
    // Issue Form Components
    @FXML private ComboBox<Member> memberComboBox;
    @FXML private ComboBox<Book> bookComboBox;
    @FXML private DatePicker issueDatePicker;
    @FXML private DatePicker dueDatePicker;
    @FXML private TextArea notesField;
    @FXML private Button issueButton;
    
    // Available Books Table
    @FXML private TextField bookSearchField;
    @FXML private TableView<Book> availableBooksTable;
    @FXML private TableColumn<Book, String> bookIdColumn;
    @FXML private TableColumn<Book, String> bookTitleColumn;
    @FXML private TableColumn<Book, String> bookAuthorColumn;
    @FXML private TableColumn<Book, String> bookAvailableColumn;
    
    // Active Members Table
    @FXML private TextField memberSearchField;
    @FXML private TableView<Member> activeMembersTable;
    @FXML private TableColumn<Member, String> memberIdColumn;
    @FXML private TableColumn<Member, String> memberNameColumn;
    @FXML private TableColumn<Member, String> memberEmailColumn;
    @FXML private TableColumn<Member, String> memberStatusColumn;
    
    // Data Access Objects
    private BookDAO bookDAO;
    private MemberDAO memberDAO;
    private IssueRecordDAO issueRecordDAO;
    
    // Observable Lists
    private ObservableList<Book> availableBooksList;
    private ObservableList<Member> activeMembersList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize DAOs
        bookDAO = new BookDAO();
        memberDAO = new MemberDAO();
        issueRecordDAO = new IssueRecordDAO();
        
        // Setup table columns
        setupBooksTableColumns();
        setupMembersTableColumns();
        
        // Setup combo boxes
        setupComboBoxes();
        
        // Setup date pickers
        setupDatePickers();
        
        // Load initial data
        loadAvailableBooks();
        loadActiveMembers();
        
        // Setup table selection listeners
        setupTableSelectionListeners();
    }

    /**
     * Setup books table columns
     */
    private void setupBooksTableColumns() {
        if (bookIdColumn != null) {
            bookIdColumn.setCellValueFactory(data -> 
                new SimpleStringProperty(String.valueOf(data.getValue().getBookId())));
        }
        if (bookTitleColumn != null) {
            bookTitleColumn.setCellValueFactory(data -> 
                new SimpleStringProperty(data.getValue().getTitle()));
        }
        if (bookAuthorColumn != null) {
            bookAuthorColumn.setCellValueFactory(data -> 
                new SimpleStringProperty(data.getValue().getAuthor()));
        }
        if (bookAvailableColumn != null) {
            bookAvailableColumn.setCellValueFactory(data -> 
                new SimpleStringProperty(String.valueOf(data.getValue().getAvailableCopies())));
        }
    }

    /**
     * Setup members table columns
     */
    private void setupMembersTableColumns() {
        if (memberIdColumn != null) {
            memberIdColumn.setCellValueFactory(data -> 
                new SimpleStringProperty(String.valueOf(data.getValue().getMemberId())));
        }
        if (memberNameColumn != null) {
            memberNameColumn.setCellValueFactory(data -> 
                new SimpleStringProperty(data.getValue().getFirstName() + " " + data.getValue().getLastName()));
        }
        if (memberEmailColumn != null) {
            memberEmailColumn.setCellValueFactory(data -> 
                new SimpleStringProperty(data.getValue().getEmail()));
        }
        if (memberStatusColumn != null) {
            memberStatusColumn.setCellValueFactory(data -> 
                new SimpleStringProperty(data.getValue().isActive() ? "Active" : "Inactive"));
        }
    }

    /**
     * Setup date pickers
     */
    private void setupDatePickers() {
        if (issueDatePicker != null) {
            issueDatePicker.setValue(LocalDate.now());
            issueDatePicker.setEditable(false);
        }
        
        if (dueDatePicker != null) {
            dueDatePicker.setValue(LocalDate.now().plusDays(14)); // Default 14 days
            dueDatePicker.setEditable(false);
        }
    }

    /**
     * Setup combo boxes for member and book selection
     */
    private void setupComboBoxes() {
        try {
            // Setup member combo box
            if (memberComboBox != null) {
                List<Member> members = memberDAO.findActiveMembers();
                ObservableList<Member> memberItems = FXCollections.observableArrayList(members);
                memberComboBox.setItems(memberItems);
                memberComboBox.setConverter(new javafx.util.StringConverter<Member>() {
                    @Override
                    public String toString(Member member) {
                        return member != null ? member.getFirstName() + " " + member.getLastName() + " (" + member.getMemberId() + ")" : "";
                    }
                    @Override
                    public Member fromString(String string) {
                        return null; // Not needed for display
                    }
                });
            }
            
            // Setup book combo box
            if (bookComboBox != null) {
                List<Book> books = bookDAO.findAvailableBooks();
                ObservableList<Book> bookItems = FXCollections.observableArrayList(books);
                bookComboBox.setItems(bookItems);
                bookComboBox.setConverter(new javafx.util.StringConverter<Book>() {
                    @Override
                    public String toString(Book book) {
                        return book != null ? book.getTitle() + " by " + book.getAuthor() + " (" + book.getBookId() + ")" : "";
                    }
                    @Override
                    public Book fromString(String string) {
                        return null; // Not needed for display
                    }
                });
            }
        } catch (SQLException e) {
            showError("Error", "Failed to load data for dropdowns: " + e.getMessage());
        }
    }

    /**
     * Setup table selection listeners
     */
    private void setupTableSelectionListeners() {
        if (availableBooksTable != null) {
            availableBooksTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null && bookComboBox != null) {
                        bookComboBox.setValue(newValue);
                    }
                }
            );
        }
        
        if (activeMembersTable != null) {
            activeMembersTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null && memberComboBox != null) {
                        memberComboBox.setValue(newValue);
                    }
                }
            );
        }
    }

    /**
     * Issue a book to a member
     */
    @FXML
    protected void issueBook() {
        try {
            // Validate input
            if (!validateIssueInput()) {
                return;
            }
            
            Member selectedMember = memberComboBox.getValue();
            Book selectedBook = bookComboBox.getValue();
            LocalDate issueDate = issueDatePicker.getValue();
            LocalDate dueDate = dueDatePicker.getValue();
            
            if (selectedMember == null) {
                showWarning("No Member Selected", "Please select a member from the dropdown.");
                return;
            }
            
            if (selectedBook == null) {
                showWarning("No Book Selected", "Please select a book from the dropdown.");
                return;
            }
            
            // Validate book availability
            if (selectedBook.getAvailableCopies() <= 0) {
                showWarning("Book Unavailable", "This book is not currently available for issuing.");
                return;
            }
            
            // Validate member
            if (!selectedMember.isActive()) {
                showWarning("Inactive Member", "This member account is inactive and cannot borrow books.");
                return;
            }
            
            // Create issue record
            IssueRecord issueRecord = new IssueRecord();
            issueRecord.setBookId(String.valueOf(selectedBook.getBookId()));
            issueRecord.setMemberId(String.valueOf(selectedMember.getMemberId()));
            issueRecord.setIssueDate(issueDate);
            issueRecord.setDueDate(dueDate);
            issueRecord.setStatus(IssueRecord.Status.ISSUED);
            
            // Save issue record and update book availability
            issueRecordDAO.save(issueRecord);
            
            showSuccess("Book Issued Successfully", 
                String.format("Book '%s' issued to %s %s\nDue Date: %s", 
                    selectedBook.getTitle(), 
                    selectedMember.getFirstName(), 
                    selectedMember.getLastName(),
                    dueDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
            
            // Clear form and refresh all data
            clearIssueForm();
            loadAvailableBooks(); // Refresh available books table
            loadActiveMembers(); // Refresh active members table to update "Books Issued" count
            setupComboBoxes(); // Refresh combo boxes to show updated availability
            
        } catch (SQLException e) {
            showError("Database Error", "Failed to issue book: " + e.getMessage());
        }
    }

    /**
     * Search available books
     */
    @FXML
    protected void searchAvailableBooks() {
        try {
            String searchTerm = bookSearchField != null ? bookSearchField.getText().trim() : "";
            
            List<Book> searchResults;
            if (searchTerm.isEmpty()) {
                searchResults = bookDAO.findAvailableBooks();
            } else {
                // Search by ID, title, or author
                searchResults = bookDAO.findAvailableBooks().stream()
                    .filter(book -> 
                        book.getBookId().toLowerCase().contains(searchTerm.toLowerCase()) ||
                        book.getTitle().toLowerCase().contains(searchTerm.toLowerCase()) ||
                        book.getAuthor().toLowerCase().contains(searchTerm.toLowerCase()))
                    .toList();
            }
            
            availableBooksList = FXCollections.observableArrayList(searchResults);
            if (availableBooksTable != null) {
                availableBooksTable.setItems(availableBooksList);
            }
            
        } catch (SQLException e) {
            showError("Search Error", "Failed to search books: " + e.getMessage());
        }
    }

    /**
     * Refresh available books table
     */
    @FXML
    protected void refreshAvailableBooks() {
        loadAvailableBooks();
        if (bookSearchField != null) {
            bookSearchField.clear();
        }
    }

    /**
     * Search active members
     */
    @FXML
    protected void searchActiveMembers() {
        try {
            String searchTerm = memberSearchField != null ? memberSearchField.getText().trim() : "";
            
            List<Member> searchResults;
            if (searchTerm.isEmpty()) {
                searchResults = memberDAO.findActiveMembers();
            } else {
                // Search by ID, name, or email
                searchResults = memberDAO.findActiveMembers().stream()
                    .filter(member -> 
                        member.getMemberId().toLowerCase().contains(searchTerm.toLowerCase()) ||
                        (member.getFirstName() + " " + member.getLastName()).toLowerCase().contains(searchTerm.toLowerCase()) ||
                        member.getEmail().toLowerCase().contains(searchTerm.toLowerCase()))
                    .toList();
            }
            
            activeMembersList = FXCollections.observableArrayList(searchResults);
            if (activeMembersTable != null) {
                activeMembersTable.setItems(activeMembersList);
            }
            
        } catch (SQLException e) {
            showError("Search Error", "Failed to search members: " + e.getMessage());
        }
    }

    /**
     * Refresh active members table
     */
    @FXML
    protected void refreshActiveMembers() {
        loadActiveMembers();
        if (memberSearchField != null) {
            memberSearchField.clear();
        }
    }

    /**
     * Load available books
     */
    private void loadAvailableBooks() {
        try {
            List<Book> availableBooks = bookDAO.findAvailableBooks();
            availableBooksList = FXCollections.observableArrayList(availableBooks);
            if (availableBooksTable != null) {
                availableBooksTable.setItems(availableBooksList);
            }
        } catch (SQLException e) {
            showError("Database Error", "Failed to load available books: " + e.getMessage());
        }
    }

    /**
     * Load active members
     */
    private void loadActiveMembers() {
        try {
            List<Member> activeMembers = memberDAO.findActiveMembers();
            activeMembersList = FXCollections.observableArrayList(activeMembers);
            if (activeMembersTable != null) {
                activeMembersTable.setItems(activeMembersList);
            }
        } catch (SQLException e) {
            showError("Database Error", "Failed to load active members: " + e.getMessage());
        }
    }

    /**
     * Clear issue form
     */
    private void clearIssueForm() {
        if (memberComboBox != null) memberComboBox.setValue(null);
        if (bookComboBox != null) bookComboBox.setValue(null);
        if (notesField != null) notesField.clear();
        if (issueDatePicker != null) issueDatePicker.setValue(LocalDate.now());
        if (dueDatePicker != null) dueDatePicker.setValue(LocalDate.now().plusDays(14));
        
        // Clear table selections
        if (availableBooksTable != null) {
            availableBooksTable.getSelectionModel().clearSelection();
        }
        if (activeMembersTable != null) {
            activeMembersTable.getSelectionModel().clearSelection();
        }
    }

    /**
     * Validate issue input
     */
    private boolean validateIssueInput() {
        if (bookComboBox == null || bookComboBox.getValue() == null) {
            showWarning("Validation Error", "Please select a book.");
            return false;
        }
        
        if (memberComboBox == null || memberComboBox.getValue() == null) {
            showWarning("Validation Error", "Please select a member.");
            return false;
        }
        
        if (issueDatePicker == null || issueDatePicker.getValue() == null) {
            showWarning("Validation Error", "Please select an issue date.");
            return false;
        }
        
        if (dueDatePicker == null || dueDatePicker.getValue() == null) {
            showWarning("Validation Error", "Please select a due date.");
            return false;
        }
        
        if (dueDatePicker.getValue().isBefore(issueDatePicker.getValue())) {
            showWarning("Validation Error", "Due date cannot be before issue date.");
            return false;
        }
        
        return true;
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
