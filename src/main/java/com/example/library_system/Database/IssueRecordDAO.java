package com.example.library_system.Database;

import com.example.library_system.Models.IssueRecord;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for IssueRecord entities.
 * Demonstrates complex database operations with transactions and business logic.
 */
public class IssueRecordDAO implements DAO<IssueRecord, Integer> {
    
    private static final String TABLE_NAME = "issue_records";
    
    // SQL Queries
    private static final String INSERT_SQL = 
        "INSERT INTO " + TABLE_NAME + 
        " (book_id, member_id, issue_date, due_date, fine_amount, status, notes) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    private static final String UPDATE_SQL = 
        "UPDATE " + TABLE_NAME + 
        " SET return_date = ?, fine_amount = ?, status = ?, notes = ?, " +
        "updated_at = CURRENT_TIMESTAMP WHERE issue_id = ?";
    
    private static final String DELETE_SQL = 
        "DELETE FROM " + TABLE_NAME + " WHERE issue_id = ?";
    
    private static final String SELECT_BY_ID_SQL = 
        "SELECT * FROM " + TABLE_NAME + " WHERE issue_id = ?";
    
    private static final String SELECT_ALL_SQL = 
        "SELECT * FROM " + TABLE_NAME + " ORDER BY issue_date DESC";
    
    private static final String EXISTS_SQL = 
        "SELECT 1 FROM " + TABLE_NAME + " WHERE issue_id = ?";
    
    private static final String COUNT_SQL = 
        "SELECT COUNT(*) FROM " + TABLE_NAME;
    
    private static final String FIND_BY_MEMBER_SQL = 
        "SELECT * FROM " + TABLE_NAME + " WHERE member_id = ? ORDER BY issue_date DESC";
    
    private static final String FIND_BY_BOOK_SQL = 
        "SELECT * FROM " + TABLE_NAME + " WHERE book_id = ? ORDER BY issue_date DESC";
    
    private static final String FIND_ACTIVE_ISSUES_SQL = 
        "SELECT * FROM " + TABLE_NAME + " WHERE status = 'ISSUED' ORDER BY due_date";
    
    private static final String FIND_OVERDUE_SQL = 
        "SELECT * FROM " + TABLE_NAME + " WHERE status IN ('ISSUED', 'OVERDUE') " +
        "AND due_date < CURDATE() ORDER BY due_date";
    
    private static final String FIND_MEMBER_ACTIVE_ISSUES_SQL = 
        "SELECT * FROM " + TABLE_NAME + " WHERE member_id = ? AND status = 'ISSUED'";
    
    private static final String UPDATE_OVERDUE_STATUS_SQL = 
        "UPDATE " + TABLE_NAME + " SET status = 'OVERDUE' " +
        "WHERE status = 'ISSUED' AND due_date < CURDATE()";
    
    private static final String ISSUE_BOOK_SQL = 
        "{ CALL issue_book_transaction(?, ?, ?, ?, ?) }";
    
    private static final String RETURN_BOOK_SQL = 
        "{ CALL return_book_transaction(?, ?, ?) }";

    private BookDAO bookDAO;
    
    public IssueRecordDAO() {
        this.bookDAO = new BookDAO();
    }

    @Override
    public boolean save(IssueRecord issueRecord) throws SQLException {
        if (issueRecord == null || !issueRecord.isValid()) {
            throw new IllegalArgumentException("Invalid issue record data");
        }
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, issueRecord.getBookId());
            stmt.setString(2, issueRecord.getMemberId());
            stmt.setDate(3, Date.valueOf(issueRecord.getIssueDate()));
            stmt.setDate(4, Date.valueOf(issueRecord.getDueDate()));
            stmt.setBigDecimal(5, issueRecord.getFineAmount());
            stmt.setString(6, issueRecord.getStatus().name());
            stmt.setString(7, issueRecord.getNotes());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // Get the generated ID
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        issueRecord.setIssueId(rs.getInt(1));
                    }
                }
                return true;
            }
            
            return false;
            
        } catch (SQLException e) {
            System.err.println("Error saving issue record: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean update(IssueRecord issueRecord) throws SQLException {
        if (issueRecord == null || !issueRecord.isValid()) {
            throw new IllegalArgumentException("Invalid issue record data");
        }
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {
            
            if (issueRecord.getReturnDate() != null) {
                stmt.setDate(1, Date.valueOf(issueRecord.getReturnDate()));
            } else {
                stmt.setNull(1, Types.DATE);
            }
            
            stmt.setBigDecimal(2, issueRecord.getFineAmount());
            stmt.setString(3, issueRecord.getStatus().name());
            stmt.setString(4, issueRecord.getNotes());
            stmt.setInt(5, issueRecord.getIssueId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating issue record: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean delete(Integer issueId) throws SQLException {
        if (issueId == null) {
            throw new IllegalArgumentException("Issue ID cannot be null");
        }
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {
            
            stmt.setInt(1, issueId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting issue record: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Optional<IssueRecord> findById(Integer issueId) throws SQLException {
        if (issueId == null) {
            return Optional.empty();
        }
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            
            stmt.setInt(1, issueId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToIssueRecord(rs));
                }
                return Optional.empty();
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding issue record by ID: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<IssueRecord> findAll() throws SQLException {
        List<IssueRecord> issueRecords = new ArrayList<>();
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                issueRecords.add(mapResultSetToIssueRecord(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding all issue records: " + e.getMessage());
            throw e;
        }
        
        return issueRecords;
    }

    @Override
    public boolean exists(Integer issueId) throws SQLException {
        if (issueId == null) {
            return false;
        }
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(EXISTS_SQL)) {
            
            stmt.setInt(1, issueId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking issue record existence: " + e.getMessage());
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
            System.err.println("Error counting issue records: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Find all issue records for a specific member
     */
    public List<IssueRecord> findByMember(String memberId) throws SQLException {
        if (memberId == null || memberId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<IssueRecord> issueRecords = new ArrayList<>();
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_MEMBER_SQL)) {
            
            stmt.setString(1, memberId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    issueRecords.add(mapResultSetToIssueRecord(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding issue records by member: " + e.getMessage());
            throw e;
        }
        
        return issueRecords;
    }

    /**
     * Find all issue records for a specific book
     */
    public List<IssueRecord> findByBook(String bookId) throws SQLException {
        if (bookId == null || bookId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<IssueRecord> issueRecords = new ArrayList<>();
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_BOOK_SQL)) {
            
            stmt.setString(1, bookId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    issueRecords.add(mapResultSetToIssueRecord(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding issue records by book: " + e.getMessage());
            throw e;
        }
        
        return issueRecords;
    }

    /**
     * Find all currently issued books
     */
    public List<IssueRecord> findActiveIssues() throws SQLException {
        List<IssueRecord> issueRecords = new ArrayList<>();
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(FIND_ACTIVE_ISSUES_SQL);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                issueRecords.add(mapResultSetToIssueRecord(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding active issues: " + e.getMessage());
            throw e;
        }
        
        return issueRecords;
    }

    /**
     * Find all overdue books
     */
    public List<IssueRecord> findOverdueBooks() throws SQLException {
        List<IssueRecord> issueRecords = new ArrayList<>();
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(FIND_OVERDUE_SQL);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                issueRecords.add(mapResultSetToIssueRecord(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding overdue books: " + e.getMessage());
            throw e;
        }
        
        return issueRecords;
    }

    /**
     * Alias method for findOverdueBooks
     */
    public List<IssueRecord> findOverdueIssues() throws SQLException {
        return findOverdueBooks();
    }

    /**
     * Find active issues for a specific member
     */
    public List<IssueRecord> findMemberActiveIssues(String memberId) throws SQLException {
        if (memberId == null || memberId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<IssueRecord> issueRecords = new ArrayList<>();
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(FIND_MEMBER_ACTIVE_ISSUES_SQL)) {
            
            stmt.setString(1, memberId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    issueRecords.add(mapResultSetToIssueRecord(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding member active issues: " + e.getMessage());
            throw e;
        }
        
        return issueRecords;
    }

    /**
     * Issue a book to a member (with transaction)
     */
    public boolean issueBook(String bookId, String memberId, LocalDate dueDate, String notes) throws SQLException {
        if (bookId == null || memberId == null) {
            throw new IllegalArgumentException("Book ID and Member ID cannot be null");
        }
        
        Connection conn = null;
        try {
            conn = DBConnection.connect();
            conn.setAutoCommit(false);
            
            // Check if book is available
            Optional<com.example.library_system.Models.Book> book = bookDAO.findById(bookId);
            if (!book.isPresent() || !book.get().isAvailable()) {
                throw new SQLException("Book is not available for issue");
            }
            
            // Create issue record
            IssueRecord issueRecord = new IssueRecord(bookId, memberId, dueDate);
            issueRecord.setNotes(notes);
            
            // Save issue record
            if (!save(issueRecord)) {
                throw new SQLException("Failed to create issue record");
            }
            
            // Update book available copies
            if (!bookDAO.updateAvailableCopies(bookId, book.get().getAvailableCopies() - 1)) {
                throw new SQLException("Failed to update book availability");
            }
            
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Error rolling back transaction: " + rollbackEx.getMessage());
                }
            }
            throw e;
            
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    System.err.println("Error closing connection: " + closeEx.getMessage());
                }
            }
        }
    }

    /**
     * Return a book (with transaction)
     */
    public boolean returnBook(Integer issueId, LocalDate returnDate) throws SQLException {
        if (issueId == null) {
            throw new IllegalArgumentException("Issue ID cannot be null");
        }
        
        Connection conn = null;
        try {
            conn = DBConnection.connect();
            conn.setAutoCommit(false);
            
            // Find the issue record
            Optional<IssueRecord> issueOpt = findById(issueId);
            if (!issueOpt.isPresent()) {
                throw new SQLException("Issue record not found");
            }
            
            IssueRecord issueRecord = issueOpt.get();
            if (issueRecord.getStatus() == IssueRecord.Status.RETURNED) {
                throw new SQLException("Book already returned");
            }
            
            // Update issue record
            issueRecord.returnBook(returnDate);
            if (!update(issueRecord)) {
                throw new SQLException("Failed to update issue record");
            }
            
            // Update book available copies
            Optional<com.example.library_system.Models.Book> book = bookDAO.findById(issueRecord.getBookId());
            if (book.isPresent()) {
                if (!bookDAO.updateAvailableCopies(issueRecord.getBookId(), 
                        book.get().getAvailableCopies() + 1)) {
                    throw new SQLException("Failed to update book availability");
                }
            }
            
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Error rolling back transaction: " + rollbackEx.getMessage());
                }
            }
            throw e;
            
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    System.err.println("Error closing connection: " + closeEx.getMessage());
                }
            }
        }
    }

    /**
     * Update overdue status for all applicable records
     */
    public int updateOverdueStatus() throws SQLException {
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_OVERDUE_STATUS_SQL)) {
            
            return stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error updating overdue status: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Maps a ResultSet row to an IssueRecord object
     */
    private IssueRecord mapResultSetToIssueRecord(ResultSet rs) throws SQLException {
        IssueRecord issueRecord = new IssueRecord();
        issueRecord.setIssueId(rs.getInt("issue_id"));
        issueRecord.setBookId(rs.getString("book_id"));
        issueRecord.setMemberId(rs.getString("member_id"));
        
        // Handle dates
        Date issueDate = rs.getDate("issue_date");
        if (issueDate != null) {
            issueRecord.setIssueDate(issueDate.toLocalDate());
        }
        
        Date dueDate = rs.getDate("due_date");
        if (dueDate != null) {
            issueRecord.setDueDate(dueDate.toLocalDate());
        }
        
        Date returnDate = rs.getDate("return_date");
        if (returnDate != null) {
            issueRecord.setReturnDate(returnDate.toLocalDate());
        }
        
        issueRecord.setFineAmount(rs.getBigDecimal("fine_amount"));
        
        // Handle enum
        String statusStr = rs.getString("status");
        if (statusStr != null) {
            issueRecord.setStatus(IssueRecord.Status.valueOf(statusStr));
        }
        
        issueRecord.setNotes(rs.getString("notes"));
        
        // Handle timestamps
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            issueRecord.setCreatedAt(created.toLocalDateTime());
        }
        
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            issueRecord.setUpdatedAt(updated.toLocalDateTime());
        }
        
        return issueRecord;
    }
}