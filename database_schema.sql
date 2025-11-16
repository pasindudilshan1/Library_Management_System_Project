-- Library Management System Database Schema
-- Run this script in phpMyAdmin or MySQL Workbench to set up the database

-- Create database
CREATE DATABASE IF NOT EXISTS library_system;
USE library_system;

-- Create Books table
CREATE TABLE IF NOT EXISTS books (
    book_id VARCHAR(10) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    genre VARCHAR(100),
    total_copies INT NOT NULL DEFAULT 1,
    available_copies INT NOT NULL DEFAULT 1,
    isbn VARCHAR(20),
    publication_year YEAR,
    publisher VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_copies CHECK (total_copies >= 0),
    CONSTRAINT chk_available CHECK (available_copies >= 0 AND available_copies <= total_copies)
);

-- Create Members table
CREATE TABLE IF NOT EXISTS members (
    member_id VARCHAR(10) PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(15),
    address TEXT,
    membership_date DATE DEFAULT CURRENT_DATE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- email regex check (MySQL uses REGEXP or RLIKE). Adjust if your MySQL version doesn't enforce CHECK.
    CONSTRAINT chk_email CHECK (email REGEXP '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$')
);

-- Create Issue Records table
CREATE TABLE IF NOT EXISTS issue_records (
    issue_id INT AUTO_INCREMENT PRIMARY KEY,
    book_id VARCHAR(10) NOT NULL,
    member_id VARCHAR(10) NOT NULL,
    issue_date DATE NOT NULL DEFAULT CURRENT_DATE,
    due_date DATE NOT NULL,
    return_date DATE NULL,
    fine_amount DECIMAL(10,2) DEFAULT 0.00,
    status ENUM('ISSUED', 'RETURNED', 'OVERDUE') DEFAULT 'ISSUED',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE,
    FOREIGN KEY (member_id) REFERENCES members(member_id) ON DELETE CASCADE,
    
    CONSTRAINT chk_dates CHECK (due_date >= issue_date),
    CONSTRAINT chk_return_date CHECK (return_date IS NULL OR return_date >= issue_date),
    CONSTRAINT chk_fine CHECK (fine_amount >= 0)
);

-- Create Library Stats table for reporting
CREATE TABLE IF NOT EXISTS library_stats (
    stat_id INT AUTO_INCREMENT PRIMARY KEY,
    total_books INT DEFAULT 0,
    total_members INT DEFAULT 0,
    books_issued INT DEFAULT 0,
    overdue_books INT DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert initial stats record (one row to maintain counters)
INSERT INTO library_stats (total_books, total_members, books_issued, overdue_books) 
VALUES (0, 0, 0, 0);

-- Create indexes for better performance
CREATE INDEX idx_books_author ON books(author);
CREATE INDEX idx_books_genre ON books(genre);
-- email already has UNIQUE index because of the UNIQUE constraint; no separate index needed
CREATE INDEX idx_issue_status ON issue_records(status);
CREATE INDEX idx_issue_dates ON issue_records(issue_date, due_date);

-- Create triggers to maintain data integrity and update stats
DELIMITER //

-- Trigger to update available copies and stats when a book is issued
CREATE TRIGGER tr_book_issued
AFTER INSERT ON issue_records
FOR EACH ROW
BEGIN
    -- Only decrement when status is ISSUED and copies are available
    IF NEW.status = 'ISSUED' THEN
        UPDATE books 
        SET available_copies = CASE 
            WHEN available_copies > 0 THEN available_copies - 1 
            ELSE 0
        END
        WHERE book_id = NEW.book_id;

        -- increment books_issued
        UPDATE library_stats 
        SET books_issued = books_issued + 1;
    END IF;
END//
 
-- Trigger to update available copies and stats when a book is returned
CREATE TRIGGER tr_book_returned
AFTER UPDATE ON issue_records
FOR EACH ROW
BEGIN
    -- If status changed from ISSUED to RETURNED, increment available_copies and decrement books_issued
    IF OLD.status = 'ISSUED' AND NEW.status = 'RETURNED' THEN
        UPDATE books 
        SET available_copies = available_copies + 1 
        WHERE book_id = NEW.book_id;
        
        UPDATE library_stats 
        SET books_issued = GREATEST(books_issued - 1, 0);
    END IF;
END//

-- Trigger to update total books count after insert
CREATE TRIGGER tr_update_book_count_insert
AFTER INSERT ON books
FOR EACH ROW
BEGIN
    UPDATE library_stats 
    SET total_books = (SELECT COUNT(*) FROM books);
END//

-- Trigger to update total books count after delete
CREATE TRIGGER tr_update_book_count_delete
AFTER DELETE ON books
FOR EACH ROW
BEGIN
    UPDATE library_stats 
    SET total_books = (SELECT COUNT(*) FROM books);
END//

-- Trigger to update total members count after insert
CREATE TRIGGER tr_update_member_count_insert
AFTER INSERT ON members
FOR EACH ROW
BEGIN
    UPDATE library_stats 
    SET total_members = (SELECT COUNT(*) FROM members WHERE is_active = TRUE);
END//

-- Trigger to update total members count after update (e.g., is_active toggled)
CREATE TRIGGER tr_update_member_count_update
AFTER UPDATE ON members
FOR EACH ROW
BEGIN
    UPDATE library_stats 
    SET total_members = (SELECT COUNT(*) FROM members WHERE is_active = TRUE);
END//

DELIMITER ;

-- Insert some sample data for testing
INSERT INTO books (book_id, title, author, genre, total_copies, available_copies, isbn, publication_year, publisher) VALUES
('B001', 'The Great Gatsby', 'F. Scott Fitzgerald', 'Fiction', 3, 3, '9780743273565', 1925, 'Scribner'),
('B002', 'To Kill a Mockingbird', 'Harper Lee', 'Fiction', 2, 2, '9780061120084', 1960, 'J.B. Lippincott & Co.'),
('B003', 'Introduction to Java Programming', 'Y. Daniel Liang', 'Programming', 5, 5, '9780132130806', 2018, 'Pearson'),
('B004', 'Data Structures and Algorithms', 'Robert Sedgewick', 'Computer Science', 3, 3, '9780321573513', 2011, 'Addison-Wesley'),
('B005', '1984', 'George Orwell', 'Dystopian Fiction', 4, 4, '9780451524935', 1949, 'Secker & Warburg');

INSERT INTO members (member_id, first_name, last_name, email, phone, address) VALUES
('M001', 'John', 'Doe', 'john.doe@email.com', '0771234567', '123 Main St, Colombo'),
('M002', 'Jane', 'Smith', 'jane.smith@email.com', '0779876543', '456 Oak Ave, Kandy'),
('M003', 'Mike', 'Johnson', 'mike.johnson@email.com', '0771122334', '789 Pine Rd, Galle'),
('M004', 'Sarah', 'Williams', 'sarah.williams@email.com', '0775566778', '321 Elm St, Negombo');

-- View to get current book availability
CREATE OR REPLACE VIEW book_availability AS
SELECT 
    b.book_id,
    b.title,
    b.author,
    b.genre,
    b.total_copies,
    b.available_copies,
    (b.total_copies - b.available_copies) AS issued_copies
FROM books b;

-- View to get overdue books
CREATE OR REPLACE VIEW overdue_books AS
SELECT 
    ir.issue_id,
    ir.book_id,
    b.title,
    ir.member_id,
    CONCAT(m.first_name, ' ', m.last_name) AS member_name,
    ir.issue_date,
    ir.due_date,
    DATEDIFF(CURDATE(), ir.due_date) AS days_overdue,
    ir.fine_amount
FROM issue_records ir
JOIN books b ON ir.book_id = b.book_id
JOIN members m ON ir.member_id = m.member_id
WHERE ir.status IN ('ISSUED', 'OVERDUE') 
  AND ir.due_date < CURDATE();
