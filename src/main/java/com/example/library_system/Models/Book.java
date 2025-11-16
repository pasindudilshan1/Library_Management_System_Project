package com.example.library_system.Models;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Book model class representing a book in the library system.
 * Demonstrates OOP principles: encapsulation, data validation, and proper method design.
 */
public class Book {
    // Private fields for encapsulation
    private String bookId;
    private String title;
    private String author;
    private String genre;
    private int totalCopies;
    private int availableCopies;
    private String isbn;
    private Integer publicationYear;
    private String publisher;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public Book() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Primary constructor with validation
    public Book(String bookId, String title, String author, String genre, int totalCopies) {
        this();
        setBookId(bookId);
        setTitle(title);
        setAuthor(author);
        setGenre(genre);
        setTotalCopies(totalCopies);
        this.availableCopies = totalCopies; // Initially all copies are available
    }

    // Full constructor
    public Book(String bookId, String title, String author, String genre, 
               int totalCopies, int availableCopies, String isbn, 
               Integer publicationYear, String publisher) {
        this(bookId, title, author, genre, totalCopies);
        setAvailableCopies(availableCopies);
        setIsbn(isbn);
        setPublicationYear(publicationYear);
        setPublisher(publisher);
    }

    // Getters and Setters with validation

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        if (bookId == null || bookId.trim().isEmpty()) {
            throw new IllegalArgumentException("Book ID cannot be null or empty");
        }
        if (!bookId.matches("^B\\d{3,}$")) {
            throw new IllegalArgumentException("Book ID must start with 'B' followed by at least 3 digits");
        }
        this.bookId = bookId.trim().toUpperCase();
        updateTimestamp();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        if (title.length() > 255) {
            throw new IllegalArgumentException("Title cannot exceed 255 characters");
        }
        this.title = title.trim();
        updateTimestamp();
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("Author cannot be null or empty");
        }
        if (author.length() > 255) {
            throw new IllegalArgumentException("Author name cannot exceed 255 characters");
        }
        this.author = author.trim();
        updateTimestamp();
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        if (genre != null && genre.length() > 100) {
            throw new IllegalArgumentException("Genre cannot exceed 100 characters");
        }
        this.genre = genre == null ? null : genre.trim();
        updateTimestamp();
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(int totalCopies) {
        if (totalCopies < 0) {
            throw new IllegalArgumentException("Total copies cannot be negative");
        }
        this.totalCopies = totalCopies;
        // Adjust available copies if necessary
        if (this.availableCopies > totalCopies) {
            this.availableCopies = totalCopies;
        }
        updateTimestamp();
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(int availableCopies) {
        if (availableCopies < 0) {
            throw new IllegalArgumentException("Available copies cannot be negative");
        }
        if (availableCopies > this.totalCopies) {
            throw new IllegalArgumentException("Available copies cannot exceed total copies");
        }
        this.availableCopies = availableCopies;
        updateTimestamp();
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        if (isbn != null && !isbn.trim().isEmpty()) {
            // Basic ISBN validation (10 or 13 digits with optional hyphens)
            String cleanIsbn = isbn.replaceAll("-", "");
            if (!cleanIsbn.matches("^\\d{10}$") && !cleanIsbn.matches("^\\d{13}$")) {
                throw new IllegalArgumentException("ISBN must be 10 or 13 digits");
            }
        }
        this.isbn = isbn == null ? null : isbn.trim();
        updateTimestamp();
    }

    public Integer getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(Integer publicationYear) {
        if (publicationYear != null) {
            int currentYear = java.time.Year.now().getValue();
            if (publicationYear < 1000 || publicationYear > currentYear) {
                throw new IllegalArgumentException("Publication year must be between 1000 and " + currentYear);
            }
        }
        this.publicationYear = publicationYear;
        updateTimestamp();
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        if (publisher != null && publisher.length() > 255) {
            throw new IllegalArgumentException("Publisher name cannot exceed 255 characters");
        }
        this.publisher = publisher == null ? null : publisher.trim();
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

    // Business methods demonstrating method design

    /**
     * Checks if the book is available for borrowing
     */
    public boolean isAvailable() {
        return availableCopies > 0;
    }

    /**
     * Gets the number of copies currently issued
     */
    public int getIssuedCopies() {
        return totalCopies - availableCopies;
    }

    /**
     * Issues a copy of the book if available
     * @return true if successfully issued, false if no copies available
     */
    public boolean issueCopy() {
        if (isAvailable()) {
            availableCopies--;
            updateTimestamp();
            return true;
        }
        return false;
    }

    /**
     * Returns a copy of the book
     * @return true if successfully returned, false if all copies already returned
     */
    public boolean returnCopy() {
        if (availableCopies < totalCopies) {
            availableCopies++;
            updateTimestamp();
            return true;
        }
        return false;
    }

    /**
     * Validates all required fields
     */
    public boolean isValid() {
        return bookId != null && !bookId.trim().isEmpty() &&
               title != null && !title.trim().isEmpty() &&
               author != null && !author.trim().isEmpty() &&
               totalCopies >= 0 && availableCopies >= 0 &&
               availableCopies <= totalCopies;
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
        Book book = (Book) obj;
        return Objects.equals(bookId, book.bookId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookId);
    }

    @Override
    public String toString() {
        return String.format("Book{ID='%s', Title='%s', Author='%s', Genre='%s', " +
                           "Total=%d, Available=%d, ISBN='%s', Year=%d, Publisher='%s'}",
                bookId, title, author, genre, totalCopies, availableCopies, 
                isbn, publicationYear, publisher);
    }

    /**
     * Creates a copy of this book (useful for data manipulation without affecting original)
     */
    public Book copy() {
        Book copy = new Book();
        copy.bookId = this.bookId;
        copy.title = this.title;
        copy.author = this.author;
        copy.genre = this.genre;
        copy.totalCopies = this.totalCopies;
        copy.availableCopies = this.availableCopies;
        copy.isbn = this.isbn;
        copy.publicationYear = this.publicationYear;
        copy.publisher = this.publisher;
        copy.createdAt = this.createdAt;
        copy.updatedAt = this.updatedAt;
        return copy;
    }
}

