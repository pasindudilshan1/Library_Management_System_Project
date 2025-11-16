package com.example.library_system.Database;

import com.example.library_system.Models.Book;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Book entities.
 * Demonstrates database operations, error handling, and JDBC usage.
 */
public class BookDAO implements DAO<Book, String> {
    
    private static final String TABLE_NAME = "books";
    
    // SQL Queries using prepared statements for security
    private static final String INSERT_SQL = 
        "INSERT INTO " + TABLE_NAME + 
        " (book_id, title, author, genre, total_copies, available_copies, isbn, publication_year, publisher) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String UPDATE_SQL = 
        "UPDATE " + TABLE_NAME + 
        " SET title = ?, author = ?, genre = ?, total_copies = ?, available_copies = ?, " +
        "isbn = ?, publication_year = ?, publisher = ?, updated_at = CURRENT_TIMESTAMP " +
        "WHERE book_id = ?";
    
    private static final String DELETE_SQL = 
        "DELETE FROM " + TABLE_NAME + " WHERE book_id = ?";
    
    private static final String SELECT_BY_ID_SQL = 
        "SELECT * FROM " + TABLE_NAME + " WHERE book_id = ?";
    
    private static final String SELECT_ALL_SQL = 
        "SELECT * FROM " + TABLE_NAME + " ORDER BY title";
    
    private static final String EXISTS_SQL = 
        "SELECT 1 FROM " + TABLE_NAME + " WHERE book_id = ?";
    
    private static final String COUNT_SQL = 
        "SELECT COUNT(*) FROM " + TABLE_NAME;
    
    private static final String SEARCH_SQL = 
        "SELECT * FROM " + TABLE_NAME + 
        " WHERE title LIKE ? OR author LIKE ? OR genre LIKE ? " +
        "ORDER BY title";
    
    private static final String FIND_BY_AUTHOR_SQL = 
        "SELECT * FROM " + TABLE_NAME + " WHERE author LIKE ? ORDER BY title";
    
    private static final String FIND_BY_GENRE_SQL = 
        "SELECT * FROM " + TABLE_NAME + " WHERE genre LIKE ? ORDER BY title";
    
    private static final String FIND_AVAILABLE_SQL = 
        "SELECT * FROM " + TABLE_NAME + " WHERE available_copies > 0 ORDER BY title";
    
    private static final String UPDATE_AVAILABLE_COPIES_SQL = 
        "UPDATE " + TABLE_NAME + " SET available_copies = ? WHERE book_id = ?";

    @Override
    public boolean save(Book book) throws SQLException {
        // Validate the book before saving
        if (book == null || !book.isValid()) {
            throw new IllegalArgumentException("Invalid book data");
        }
        
        // Check if book already exists
        if (exists(book.getBookId())) {
            throw new SQLException("Book with ID " + book.getBookId() + " already exists");
        }
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL)) {
            
            // Set parameters using proper null handling
            stmt.setString(1, book.getBookId());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getAuthor());
            stmt.setString(4, book.getGenre());
            stmt.setInt(5, book.getTotalCopies());
            stmt.setInt(6, book.getAvailableCopies());
            stmt.setString(7, book.getIsbn());
            
            if (book.getPublicationYear() != null) {
                stmt.setInt(8, book.getPublicationYear());
            } else {
                stmt.setNull(8, Types.INTEGER);
            }
            
            stmt.setString(9, book.getPublisher());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error saving book: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean update(Book book) throws SQLException {
        if (book == null || !book.isValid()) {
            throw new IllegalArgumentException("Invalid book data");
        }
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {
            
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getGenre());
            stmt.setInt(4, book.getTotalCopies());
            stmt.setInt(5, book.getAvailableCopies());
            stmt.setString(6, book.getIsbn());
            
            if (book.getPublicationYear() != null) {
                stmt.setInt(7, book.getPublicationYear());
            } else {
                stmt.setNull(7, Types.INTEGER);
            }
            
            stmt.setString(8, book.getPublisher());
            stmt.setString(9, book.getBookId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating book: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean delete(String bookId) throws SQLException {
        if (bookId == null || bookId.trim().isEmpty()) {
            throw new IllegalArgumentException("Book ID cannot be null or empty");
        }
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {
            
            stmt.setString(1, bookId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting book: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Optional<Book> findById(String bookId) throws SQLException {
        if (bookId == null || bookId.trim().isEmpty()) {
            return Optional.empty();
        }
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            
            stmt.setString(1, bookId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToBook(rs));
                }
                return Optional.empty();
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding book by ID: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Book> findAll() throws SQLException {
        List<Book> books = new ArrayList<>();
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                books.add(mapResultSetToBook(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding all books: " + e.getMessage());
            throw e;
        }
        
        return books;
    }

    @Override
    public boolean exists(String bookId) throws SQLException {
        if (bookId == null || bookId.trim().isEmpty()) {
            return false;
        }
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(EXISTS_SQL)) {
            
            stmt.setString(1, bookId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking book existence: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public long count() throws SQLException {
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(COUNT_SQL);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
            
        } catch (SQLException e) {
            System.err.println("Error counting books: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Search books by title, author, or genre
     */
    public List<Book> search(String query) throws SQLException {
        if (query == null || query.trim().isEmpty()) {
            return findAll();
        }
        
        List<Book> books = new ArrayList<>();
        String searchPattern = "%" + query.trim() + "%";
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(SEARCH_SQL)) {
            
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    books.add(mapResultSetToBook(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error searching books: " + e.getMessage());
            throw e;
        }
        
        return books;
    }

    /**
     * Alias method for search - searches by title, author, or genre
     */
    public List<Book> searchByTitle(String query) throws SQLException {
        return search(query);
    }

    /**
     * Find books by author
     */
    public List<Book> findByAuthor(String author) throws SQLException {
        if (author == null || author.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Book> books = new ArrayList<>();
        String searchPattern = "%" + author.trim() + "%";
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_AUTHOR_SQL)) {
            
            stmt.setString(1, searchPattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    books.add(mapResultSetToBook(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding books by author: " + e.getMessage());
            throw e;
        }
        
        return books;
    }

    /**
     * Find books by genre
     */
    public List<Book> findByGenre(String genre) throws SQLException {
        if (genre == null || genre.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Book> books = new ArrayList<>();
        String searchPattern = "%" + genre.trim() + "%";
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_GENRE_SQL)) {
            
            stmt.setString(1, searchPattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    books.add(mapResultSetToBook(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding books by genre: " + e.getMessage());
            throw e;
        }
        
        return books;
    }

    /**
     * Find all available books (with available copies > 0)
     */
    public List<Book> findAvailableBooks() throws SQLException {
        List<Book> books = new ArrayList<>();
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(FIND_AVAILABLE_SQL);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                books.add(mapResultSetToBook(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding available books: " + e.getMessage());
            throw e;
        }
        
        return books;
    }

    /**
     * Update available copies for a book
     */
    public boolean updateAvailableCopies(String bookId, int availableCopies) throws SQLException {
        if (bookId == null || bookId.trim().isEmpty()) {
            throw new IllegalArgumentException("Book ID cannot be null or empty");
        }
        
        if (availableCopies < 0) {
            throw new IllegalArgumentException("Available copies cannot be negative");
        }
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_AVAILABLE_COPIES_SQL)) {
            
            stmt.setInt(1, availableCopies);
            stmt.setString(2, bookId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating available copies: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Maps a ResultSet row to a Book object
     */
    private Book mapResultSetToBook(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setBookId(rs.getString("book_id"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setGenre(rs.getString("genre"));
        book.setTotalCopies(rs.getInt("total_copies"));
        book.setAvailableCopies(rs.getInt("available_copies"));
        book.setIsbn(rs.getString("isbn"));
        
        // Handle nullable integer
        int pubYear = rs.getInt("publication_year");
        if (!rs.wasNull()) {
            book.setPublicationYear(pubYear);
        }
        
        book.setPublisher(rs.getString("publisher"));
        
        // Handle timestamps
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            book.setCreatedAt(created.toLocalDateTime());
        }
        
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            book.setUpdatedAt(updated.toLocalDateTime());
        }
        
        return book;
    }
}