package com.example.library_system.Controller;

import com.example.library_system.Database.BookDAO;
import com.example.library_system.Models.Book;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for Book management operations.
 * Demonstrates proper separation of concerns, error handling, and validation.
 */
public class BookController implements Initializable {
    
    // FXML Components
    @FXML private TextField bookIdField;
    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private TextField genreField;
    @FXML private TextField totalCopiesField;
    @FXML private TextField availableCopiesField;
    @FXML private TextField isbnField;
    @FXML private TextField publicationYearField;
    @FXML private TextField publisherField;
    @FXML private TextField searchField;
    
    @FXML private TableView<Book> booksTable;
    @FXML private TableColumn<Book, String> idColumn;
    @FXML private TableColumn<Book, String> titleColumn;
    @FXML private TableColumn<Book, String> authorColumn;
    @FXML private TableColumn<Book, String> genreColumn;
    @FXML private TableColumn<Book, Integer> totalCopiesColumn;
    @FXML private TableColumn<Book, Integer> availableCopiesColumn;
    
    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button clearButton;
    @FXML private Button searchButton;
    
    // DAO for database operations
    private BookDAO bookDAO;
    private ObservableList<Book> bookList;
    private Book selectedBook;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bookDAO = new BookDAO();
        bookList = FXCollections.observableArrayList();
        
        initializeTableColumns();
        setupEventHandlers();
        loadBooks();
        generateNextBookId();
    }

    /**
     * Initialize table columns
     */
    private void initializeTableColumns() {
        if (idColumn != null) {
            idColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getBookId()));
        }
        if (titleColumn != null) {
            titleColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTitle()));
        }
        if (authorColumn != null) {
            authorColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getAuthor()));
        }
        if (genreColumn != null) {
            genreColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getGenre()));
        }
        if (totalCopiesColumn != null) {
            totalCopiesColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getTotalCopies()).asObject());
        }
        if (availableCopiesColumn != null) {
            availableCopiesColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getAvailableCopies()).asObject());
        }
        
        if (booksTable != null) {
            booksTable.setItems(bookList);
        }
    }

    /**
     * Setup event handlers
     */
    private void setupEventHandlers() {
        if (booksTable != null) {
            booksTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    selectedBook = newValue;
                    populateFields(newValue);
                    updateButtonStates();
                });
        }
    }

    /**
     * Add a new book
     */
    @FXML
    protected void addBook() {
        try {
            // Validate input fields
            if (!validateInputFields()) {
                return;
            }

            // Create book object with validation
            Book book = createBookFromFields();
            
            // Save to database
            if (bookDAO.save(book)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Book added successfully!");
                loadBooks();
                clearFields();
                generateNextBookId();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add book to database.");
            }

        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", e.getMessage());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                     "Database operation failed: " + e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Unexpected Error", 
                     "An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * Update an existing book
     */
    @FXML
    protected void updateBook() {
        if (selectedBook == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a book to update.");
            return;
        }

        try {
            // Validate input fields
            if (!validateInputFields()) {
                return;
            }

            // Create updated book object
            Book updatedBook = createBookFromFields();
            
            // Update in database
            if (bookDAO.update(updatedBook)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Book updated successfully!");
                loadBooks();
                clearFields();
                selectedBook = null;
                updateButtonStates();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update book in database.");
            }

        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", e.getMessage());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                     "Database operation failed: " + e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Unexpected Error", 
                     "An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * Delete a book
     */
    @FXML
    protected void deleteBook() {
        if (selectedBook == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a book to delete.");
            return;
        }

        // Confirmation dialog
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Deletion");
        confirmDialog.setHeaderText("Delete Book");
        confirmDialog.setContentText("Are you sure you want to delete the book: " + 
                                   selectedBook.getTitle() + "?");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (bookDAO.delete(selectedBook.getBookId())) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Book deleted successfully!");
                    loadBooks();
                    clearFields();
                    selectedBook = null;
                    updateButtonStates();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete book from database.");
                }

            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", 
                         "Database operation failed: " + e.getMessage());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Unexpected Error", 
                         "An unexpected error occurred: " + e.getMessage());
            }
        }
    }

    /**
     * Search books
     */
    @FXML
    protected void searchBooks() {
        String searchQuery = searchField != null ? searchField.getText() : "";
        
        try {
            List<Book> searchResults;
            if (searchQuery.trim().isEmpty()) {
                searchResults = bookDAO.findAll();
            } else {
                searchResults = bookDAO.search(searchQuery);
            }
            
            bookList.clear();
            bookList.addAll(searchResults);
            
            if (searchResults.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Search Results", "No books found for: " + searchQuery);
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                     "Search operation failed: " + e.getMessage());
        }
    }

    /**
     * Clear all input fields
     */
    @FXML
    protected void clearFields() {
        if (bookIdField != null) bookIdField.clear();
        if (titleField != null) titleField.clear();
        if (authorField != null) authorField.clear();
        if (genreField != null) genreField.clear();
        if (totalCopiesField != null) totalCopiesField.clear();
        if (availableCopiesField != null) availableCopiesField.clear();
        if (isbnField != null) isbnField.clear();
        if (publicationYearField != null) publicationYearField.clear();
        if (publisherField != null) publisherField.clear();
        
        selectedBook = null;
        updateButtonStates();
        
        if (booksTable != null) {
            booksTable.getSelectionModel().clearSelection();
        }
    }

    /**
     * Refresh the books list from database
     */
    @FXML
    protected void refreshBooks() {
        loadBooks();
        clearFields();
        generateNextBookId();
        
    }

    /**
     * Load all books from database
     */
    private void loadBooks() {
        try {
            List<Book> books = bookDAO.findAll();
            bookList.clear();
            bookList.addAll(books);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                     "Failed to load books: " + e.getMessage());
        }
    }

    /**
     * Generate next book ID automatically
     */
    private void generateNextBookId() {
        try {
            long count = bookDAO.count();
            String nextId = String.format("B%03d", count + 1);
            
            // Keep incrementing if ID already exists
            while (bookDAO.exists(nextId)) {
                count++;
                nextId = String.format("B%03d", count + 1);
            }
            
            if (bookIdField != null) {
                bookIdField.setText(nextId);
                bookIdField.setEditable(false);
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                     "Failed to generate book ID: " + e.getMessage());
        }
    }

    /**
     * Validate input fields
     */
    private boolean validateInputFields() {
        StringBuilder errorMessage = new StringBuilder();

        if (bookIdField == null || bookIdField.getText().trim().isEmpty()) {
            errorMessage.append("Book ID is required.\n");
        }
        
        if (titleField == null || titleField.getText().trim().isEmpty()) {
            errorMessage.append("Title is required.\n");
        }
        
        if (authorField == null || authorField.getText().trim().isEmpty()) {
            errorMessage.append("Author is required.\n");
        }
        
        if (totalCopiesField == null || totalCopiesField.getText().trim().isEmpty()) {
            errorMessage.append("Total copies is required.\n");
        } else {
            try {
                int totalCopies = Integer.parseInt(totalCopiesField.getText().trim());
                if (totalCopies <= 0) {
                    errorMessage.append("Total copies must be greater than 0.\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("Total copies must be a valid number.\n");
            }
        }
        
        if (availableCopiesField != null && !availableCopiesField.getText().trim().isEmpty()) {
            try {
                int availableCopies = Integer.parseInt(availableCopiesField.getText().trim());
                if (availableCopies < 0) {
                    errorMessage.append("Available copies cannot be negative.\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("Available copies must be a valid number.\n");
            }
        }
        
        if (publicationYearField != null && !publicationYearField.getText().trim().isEmpty()) {
            try {
                Integer.parseInt(publicationYearField.getText().trim());
            } catch (NumberFormatException e) {
                errorMessage.append("Publication year must be a valid number.\n");
            }
        }

        if (errorMessage.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", errorMessage.toString());
            return false;
        }

        return true;
    }

    /**
     * Create Book object from form fields
     */
    private Book createBookFromFields() {
        String bookId = bookIdField.getText().trim();
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String genre = genreField != null ? genreField.getText().trim() : "";
        
        int totalCopies = Integer.parseInt(totalCopiesField.getText().trim());
        
        int availableCopies = totalCopies; // Default to total copies
        if (availableCopiesField != null && !availableCopiesField.getText().trim().isEmpty()) {
            availableCopies = Integer.parseInt(availableCopiesField.getText().trim());
        }
        
        String isbn = isbnField != null ? isbnField.getText().trim() : null;
        Integer publicationYear = null;
        if (publicationYearField != null && !publicationYearField.getText().trim().isEmpty()) {
            publicationYear = Integer.parseInt(publicationYearField.getText().trim());
        }
        String publisher = publisherField != null ? publisherField.getText().trim() : null;

        return new Book(bookId, title, author, genre, totalCopies, availableCopies, 
                       isbn, publicationYear, publisher);
    }

    /**
     * Populate form fields with book data
     */
    private void populateFields(Book book) {
        if (book == null) {
            clearFields();
            return;
        }

        if (bookIdField != null) {
            bookIdField.setText(book.getBookId());
            bookIdField.setEditable(false);
        }
        if (titleField != null) titleField.setText(book.getTitle());
        if (authorField != null) authorField.setText(book.getAuthor());
        if (genreField != null) genreField.setText(book.getGenre() != null ? book.getGenre() : "");
        if (totalCopiesField != null) totalCopiesField.setText(String.valueOf(book.getTotalCopies()));
        if (availableCopiesField != null) availableCopiesField.setText(String.valueOf(book.getAvailableCopies()));
        if (isbnField != null) isbnField.setText(book.getIsbn() != null ? book.getIsbn() : "");
        if (publicationYearField != null) {
            publicationYearField.setText(book.getPublicationYear() != null ? 
                                       book.getPublicationYear().toString() : "");
        }
        if (publisherField != null) publisherField.setText(book.getPublisher() != null ? book.getPublisher() : "");
    }

    /**
     * Update button states based on selection
     */
    private void updateButtonStates() {
        boolean hasSelection = selectedBook != null;
        
        if (updateButton != null) updateButton.setDisable(!hasSelection);
        if (deleteButton != null) deleteButton.setDisable(!hasSelection);
        
        if (addButton != null) addButton.setDisable(hasSelection);
    }

    /**
     * Show alert dialog
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}