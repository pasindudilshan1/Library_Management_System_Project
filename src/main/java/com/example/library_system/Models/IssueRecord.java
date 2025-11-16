package com.example.library_system.Models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * IssueRecord model class representing a book borrowing transaction.
 * Demonstrates OOP principles: encapsulation, enums, date handling, and business logic.
 */
public class IssueRecord {
    
    /**
     * Enum for issue status with controlled vocabulary
     */
    public enum Status {
        ISSUED("Issued"),
        RETURNED("Returned"), 
        OVERDUE("Overdue");
        
        private final String displayName;
        
        Status(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }

    // Private fields for encapsulation
    private Integer issueId; // Auto-generated in database
    private String bookId;
    private String memberId;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private BigDecimal fineAmount;
    private Status status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constants for business rules
    private static final int DEFAULT_LOAN_PERIOD_DAYS = 14;
    private static final BigDecimal FINE_PER_DAY = new BigDecimal("5.00"); // Rs. 5 per day

    // Default constructor
    public IssueRecord() {
        this.issueDate = LocalDate.now();
        this.dueDate = this.issueDate.plusDays(DEFAULT_LOAN_PERIOD_DAYS);
        this.status = Status.ISSUED;
        this.fineAmount = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Primary constructor for new issue
    public IssueRecord(String bookId, String memberId) {
        this();
        setBookId(bookId);
        setMemberId(memberId);
    }

    // Constructor with custom due date
    public IssueRecord(String bookId, String memberId, LocalDate dueDate) {
        this(bookId, memberId);
        setDueDate(dueDate);
    }

    // Full constructor
    public IssueRecord(Integer issueId, String bookId, String memberId, 
                      LocalDate issueDate, LocalDate dueDate, LocalDate returnDate,
                      BigDecimal fineAmount, Status status, String notes) {
        this(bookId, memberId);
        this.issueId = issueId;
        setIssueDate(issueDate);
        setDueDate(dueDate);
        setReturnDate(returnDate);
        setFineAmount(fineAmount);
        setStatus(status);
        setNotes(notes);
    }

    // Getters and Setters with validation

    public Integer getIssueId() {
        return issueId;
    }

    public void setIssueId(Integer issueId) {
        this.issueId = issueId;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        if (bookId == null || bookId.trim().isEmpty()) {
            throw new IllegalArgumentException("Book ID cannot be null or empty");
        }
        this.bookId = bookId.trim().toUpperCase();
        updateTimestamp();
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        if (memberId == null || memberId.trim().isEmpty()) {
            throw new IllegalArgumentException("Member ID cannot be null or empty");
        }
        this.memberId = memberId.trim().toUpperCase();
        updateTimestamp();
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        if (issueDate != null && issueDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Issue date cannot be in the future");
        }
        this.issueDate = issueDate == null ? LocalDate.now() : issueDate;
        // Adjust due date if needed
        if (this.dueDate != null && this.dueDate.isBefore(this.issueDate)) {
            this.dueDate = this.issueDate.plusDays(DEFAULT_LOAN_PERIOD_DAYS);
        }
        updateTimestamp();
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        if (dueDate != null && issueDate != null && dueDate.isBefore(issueDate)) {
            throw new IllegalArgumentException("Due date cannot be before issue date");
        }
        this.dueDate = dueDate;
        updateTimestamp();
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        if (returnDate != null && issueDate != null && returnDate.isBefore(issueDate)) {
            throw new IllegalArgumentException("Return date cannot be before issue date");
        }
        this.returnDate = returnDate;
        
        // Update status when return date is set
        if (returnDate != null) {
            this.status = Status.RETURNED;
            calculateFine();
        }
        updateTimestamp();
    }

    public BigDecimal getFineAmount() {
        return fineAmount;
    }

    public void setFineAmount(BigDecimal fineAmount) {
        if (fineAmount != null && fineAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Fine amount cannot be negative");
        }
        this.fineAmount = fineAmount == null ? BigDecimal.ZERO : fineAmount;
        updateTimestamp();
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status == null ? Status.ISSUED : status;
        updateTimestamp();
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        if (notes != null && notes.length() > 1000) {
            throw new IllegalArgumentException("Notes cannot exceed 1000 characters");
        }
        this.notes = notes == null ? null : notes.trim();
        updateTimestamp();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Business methods demonstrating method design and control structures

    /**
     * Checks if the book is currently overdue
     */
    public boolean isOverdue() {
        return status == Status.ISSUED && 
               dueDate != null && 
               LocalDate.now().isAfter(dueDate);
    }

    /**
     * Calculates the number of days overdue (0 if not overdue)
     */
    public long getDaysOverdue() {
        if (!isOverdue()) {
            return 0;
        }
        return ChronoUnit.DAYS.between(dueDate, LocalDate.now());
    }

    /**
     * Calculates the fine amount based on overdue days
     */
    public void calculateFine() {
        if (returnDate == null || dueDate == null) {
            this.fineAmount = BigDecimal.ZERO;
            return;
        }
        
        // Calculate overdue days
        long overdueDays;
        if (returnDate.isAfter(dueDate)) {
            overdueDays = ChronoUnit.DAYS.between(dueDate, returnDate);
        } else {
            overdueDays = 0;
        }
        
        // Calculate fine (Rs. 5 per day)
        this.fineAmount = FINE_PER_DAY.multiply(BigDecimal.valueOf(overdueDays));
        updateTimestamp();
    }

    /**
     * Returns the book and calculates final fine
     */
    public void returnBook() {
        returnBook(LocalDate.now());
    }

    /**
     * Returns the book with a specific return date
     */
    public void returnBook(LocalDate returnDate) {
        setReturnDate(returnDate);
        setStatus(Status.RETURNED);
        calculateFine();
    }

    /**
     * Extends the due date by specified number of days
     */
    public boolean extendDueDate(int days) {
        if (days <= 0 || status != Status.ISSUED) {
            return false;
        }
        
        this.dueDate = this.dueDate.plusDays(days);
        
        // Update status if no longer overdue
        if (status == Status.OVERDUE && !isOverdue()) {
            status = Status.ISSUED;
        }
        
        updateTimestamp();
        return true;
    }

    /**
     * Updates status based on current date (should be called periodically)
     */
    public void updateStatus() {
        if (status == Status.ISSUED && isOverdue()) {
            status = Status.OVERDUE;
            updateTimestamp();
        }
    }

    /**
     * Gets the loan period in days
     */
    public long getLoanPeriodDays() {
        if (dueDate == null || issueDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(issueDate, dueDate);
    }

    /**
     * Checks if the record is valid
     */
    public boolean isValid() {
        return bookId != null && !bookId.trim().isEmpty() &&
               memberId != null && !memberId.trim().isEmpty() &&
               issueDate != null && dueDate != null &&
               !dueDate.isBefore(issueDate) &&
               (returnDate == null || !returnDate.isBefore(issueDate)) &&
               fineAmount != null && fineAmount.compareTo(BigDecimal.ZERO) >= 0;
    }

    // Helper method to update timestamp
    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    // Object methods for proper equality and string representation

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        IssueRecord that = (IssueRecord) obj;
        return Objects.equals(issueId, that.issueId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(issueId);
    }

    @Override
    public String toString() {
        return String.format("IssueRecord{ID=%d, BookID='%s', MemberID='%s', " +
                           "IssueDate=%s, DueDate=%s, ReturnDate=%s, " +
                           "Status=%s, Fine=%.2f, Overdue=%d days}",
                issueId, bookId, memberId, issueDate, dueDate, returnDate,
                status, fineAmount, getDaysOverdue());
    }

    /**
     * Creates a copy of this issue record
     */
    public IssueRecord copy() {
        IssueRecord copy = new IssueRecord();
        copy.issueId = this.issueId;
        copy.bookId = this.bookId;
        copy.memberId = this.memberId;
        copy.issueDate = this.issueDate;
        copy.dueDate = this.dueDate;
        copy.returnDate = this.returnDate;
        copy.fineAmount = this.fineAmount;
        copy.status = this.status;
        copy.notes = this.notes;
        copy.createdAt = this.createdAt;
        copy.updatedAt = this.updatedAt;
        return copy;
    }
}
