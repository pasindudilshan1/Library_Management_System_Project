package com.example.library_system.Models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Member model class representing a library member.
 * Demonstrates OOP principles: encapsulation, validation, and proper data handling.
 */
public class Member {
    // Private fields for encapsulation
    private String memberId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private LocalDate membershipDate;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    
    // Phone validation pattern (supports various formats: 9-15 digits)
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^[+]?[0-9]{9,15}$");

    // Default constructor
    public Member() {
        this.membershipDate = LocalDate.now();
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Primary constructor with validation
    public Member(String memberId, String firstName, String lastName, String email) {
        this();
        setMemberId(memberId);
        setFirstName(firstName);
        setLastName(lastName);
        setEmail(email);
    }

    // Full constructor
    public Member(String memberId, String firstName, String lastName, String email, 
                 String phone, String address) {
        this(memberId, firstName, lastName, email);
        setPhone(phone);
        setAddress(address);
    }

    // Complete constructor with all fields
    public Member(String memberId, String firstName, String lastName, String email, 
                 String phone, String address, LocalDate membershipDate, boolean isActive) {
        this(memberId, firstName, lastName, email, phone, address);
        setMembershipDate(membershipDate);
        setActive(isActive);
    }

    // Getters and Setters with validation

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        if (memberId == null || memberId.trim().isEmpty()) {
            throw new IllegalArgumentException("Member ID cannot be null or empty");
        }
        if (!memberId.matches("^M\\d{3,}$")) {
            throw new IllegalArgumentException("Member ID must start with 'M' followed by at least 3 digits");
        }
        this.memberId = memberId.trim().toUpperCase();
        updateTimestamp();
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be null or empty");
        }
        if (firstName.length() > 100) {
            throw new IllegalArgumentException("First name cannot exceed 100 characters");
        }
        if (!firstName.matches("^[a-zA-Z\\s'-]+$")) {
            throw new IllegalArgumentException("First name can only contain letters, spaces, hyphens, and apostrophes");
        }
        this.firstName = capitalizeWords(firstName.trim());
        updateTimestamp();
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be null or empty");
        }
        if (lastName.length() > 100) {
            throw new IllegalArgumentException("Last name cannot exceed 100 characters");
        }
        if (!lastName.matches("^[a-zA-Z\\s'-]+$")) {
            throw new IllegalArgumentException("Last name can only contain letters, spaces, hyphens, and apostrophes");
        }
        this.lastName = capitalizeWords(lastName.trim());
        updateTimestamp();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (email.length() > 255) {
            throw new IllegalArgumentException("Email cannot exceed 255 characters");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
        this.email = email.trim().toLowerCase();
        updateTimestamp();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        if (phone != null && !phone.trim().isEmpty()) {
            // Clean the phone number (remove spaces, dashes, parentheses)
            String cleanPhone = phone.replaceAll("[\\s\\-()]+", "");
            if (!PHONE_PATTERN.matcher(cleanPhone).matches()) {
                throw new IllegalArgumentException("Invalid phone number format");
            }
            this.phone = cleanPhone;
        } else {
            this.phone = phone;
        }
        updateTimestamp();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        if (address != null && address.length() > 500) {
            throw new IllegalArgumentException("Address cannot exceed 500 characters");
        }
        this.address = address == null ? null : address.trim();
        updateTimestamp();
    }

    public LocalDate getMembershipDate() {
        return membershipDate;
    }

    public void setMembershipDate(LocalDate membershipDate) {
        if (membershipDate != null && membershipDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Membership date cannot be in the future");
        }
        this.membershipDate = membershipDate == null ? LocalDate.now() : membershipDate;
        updateTimestamp();
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
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
     * Gets the full name of the member
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Calculates membership duration in years
     */
    public long getMembershipYears() {
        return java.time.Period.between(membershipDate, LocalDate.now()).getYears();
    }

    /**
     * Activates the member account
     */
    public void activate() {
        setActive(true);
    }

    /**
     * Deactivates the member account
     */
    public void deactivate() {
        setActive(false);
    }

    /**
     * Checks if the member is a senior member (5+ years)
     */
    public boolean isSeniorMember() {
        return getMembershipYears() >= 5;
    }

    /**
     * Validates all required fields
     */
    public boolean isValid() {
        return memberId != null && !memberId.trim().isEmpty() &&
               firstName != null && !firstName.trim().isEmpty() &&
               lastName != null && !lastName.trim().isEmpty() &&
               email != null && !email.trim().isEmpty() &&
               EMAIL_PATTERN.matcher(email).matches() &&
               membershipDate != null;
    }

    // Helper methods

    /**
     * Capitalizes the first letter of each word
     */
    private String capitalizeWords(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        String[] words = text.toLowerCase().split("\\s+");
        StringBuilder capitalized = new StringBuilder();
        
        for (int i = 0; i < words.length; i++) {
            if (words[i].length() > 0) {
                capitalized.append(Character.toUpperCase(words[i].charAt(0)));
                if (words[i].length() > 1) {
                    capitalized.append(words[i].substring(1));
                }
            }
            if (i < words.length - 1) {
                capitalized.append(" ");
            }
        }
        
        return capitalized.toString();
    }

    /**
     * Updates the timestamp
     */
    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    // Object methods for proper equality and string representation

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Member member = (Member) obj;
        return Objects.equals(memberId, member.memberId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberId);
    }

    @Override
    public String toString() {
        return String.format("Member{ID='%s', Name='%s %s', Email='%s', Phone='%s', " +
                           "Address='%s', MembershipDate=%s, Active=%s}",
                memberId, firstName, lastName, email, phone, address, 
                membershipDate, isActive);
    }

    /**
     * Creates a copy of this member (useful for data manipulation without affecting original)
     */
    public Member copy() {
        Member copy = new Member();
        copy.memberId = this.memberId;
        copy.firstName = this.firstName;
        copy.lastName = this.lastName;
        copy.email = this.email;
        copy.phone = this.phone;
        copy.address = this.address;
        copy.membershipDate = this.membershipDate;
        copy.isActive = this.isActive;
        copy.createdAt = this.createdAt;
        copy.updatedAt = this.updatedAt;
        return copy;
    }
}

