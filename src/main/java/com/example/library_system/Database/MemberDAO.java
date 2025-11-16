package com.example.library_system.Database;

import com.example.library_system.Models.Member;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Member entities.
 * Demonstrates database operations with proper validation and error handling.
 */
public class MemberDAO implements DAO<Member, String> {
    
    private static final String TABLE_NAME = "members";
    
    // SQL Queries
    private static final String INSERT_SQL = 
        "INSERT INTO " + TABLE_NAME + 
        " (member_id, first_name, last_name, email, phone, address) " +
        "VALUES (?, ?, ?, ?, ?, ?)";
    
    private static final String UPDATE_SQL = 
        "UPDATE " + TABLE_NAME + 
        " SET first_name = ?, last_name = ?, email = ?, phone = ?, address = ?, " +
        "updated_at = CURRENT_TIMESTAMP WHERE member_id = ?";
    
    private static final String DELETE_SQL = 
        "UPDATE " + TABLE_NAME + " SET is_active = FALSE WHERE member_id = ?";
    
    private static final String HARD_DELETE_SQL = 
        "DELETE FROM " + TABLE_NAME + " WHERE member_id = ?";
    
    private static final String SELECT_BY_ID_SQL = 
        "SELECT * FROM " + TABLE_NAME + " WHERE member_id = ?";
    
    private static final String SELECT_ALL_SQL = 
        "SELECT * FROM " + TABLE_NAME + " ORDER BY first_name, last_name";
    
    private static final String SELECT_ACTIVE_SQL = 
        "SELECT * FROM " + TABLE_NAME + " WHERE is_active = TRUE ORDER BY first_name, last_name";
    
    private static final String EXISTS_SQL = 
        "SELECT 1 FROM " + TABLE_NAME + " WHERE member_id = ?";
    
    private static final String COUNT_SQL = 
        "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE is_active = TRUE";
    
    private static final String SEARCH_SQL = 
        "SELECT * FROM " + TABLE_NAME + 
        " WHERE (first_name LIKE ? OR last_name LIKE ? OR email LIKE ?) AND is_active = TRUE " +
        "ORDER BY first_name, last_name";
    
    private static final String FIND_BY_EMAIL_SQL = 
        "SELECT * FROM " + TABLE_NAME + " WHERE email = ?";
    
    private static final String UPDATE_STATUS_SQL = 
        "UPDATE " + TABLE_NAME + " SET is_active = ? WHERE member_id = ?";

    @Override
    public boolean save(Member member) throws SQLException {
        if (member == null || !member.isValid()) {
            throw new IllegalArgumentException("Invalid member data");
        }
        
        // Check if member already exists
        if (exists(member.getMemberId())) {
            throw new SQLException("Member with ID " + member.getMemberId() + " already exists");
        }
        
        // Check if email already exists
        if (findByEmail(member.getEmail()).isPresent()) {
            throw new SQLException("Member with email " + member.getEmail() + " already exists");
        }
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL)) {
            
            stmt.setString(1, member.getMemberId());
            stmt.setString(2, member.getFirstName());
            stmt.setString(3, member.getLastName());
            stmt.setString(4, member.getEmail());
            stmt.setString(5, member.getPhone());
            stmt.setString(6, member.getAddress());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error saving member: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean update(Member member) throws SQLException {
        if (member == null || !member.isValid()) {
            throw new IllegalArgumentException("Invalid member data");
        }
        
        // Check if another member already has this email
        Optional<Member> existingMember = findByEmail(member.getEmail());
        if (existingMember.isPresent() && 
            !existingMember.get().getMemberId().equals(member.getMemberId())) {
            throw new SQLException("Another member with email " + member.getEmail() + " already exists");
        }
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {
            
            stmt.setString(1, member.getFirstName());
            stmt.setString(2, member.getLastName());
            stmt.setString(3, member.getEmail());
            stmt.setString(4, member.getPhone());
            stmt.setString(5, member.getAddress());
            stmt.setString(6, member.getMemberId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating member: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean delete(String memberId) throws SQLException {
        return deactivate(memberId);
    }
    
    /**
     * Soft delete - deactivates member instead of removing from database
     */
    public boolean deactivate(String memberId) throws SQLException {
        if (memberId == null || memberId.trim().isEmpty()) {
            throw new IllegalArgumentException("Member ID cannot be null or empty");
        }
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {
            
            stmt.setString(1, memberId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deactivating member: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Hard delete - permanently removes member from database
     */
    public boolean hardDelete(String memberId) throws SQLException {
        if (memberId == null || memberId.trim().isEmpty()) {
            throw new IllegalArgumentException("Member ID cannot be null or empty");
        }
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(HARD_DELETE_SQL)) {
            
            stmt.setString(1, memberId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error hard deleting member: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Optional<Member> findById(String memberId) throws SQLException {
        if (memberId == null || memberId.trim().isEmpty()) {
            return Optional.empty();
        }
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            
            stmt.setString(1, memberId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToMember(rs));
                }
                return Optional.empty();
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding member by ID: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Member> findAll() throws SQLException {
        List<Member> members = new ArrayList<>();
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                members.add(mapResultSetToMember(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding all members: " + e.getMessage());
            throw e;
        }
        
        return members;
    }
    
    /**
     * Find only active members
     */
    public List<Member> findActiveMembers() throws SQLException {
        List<Member> members = new ArrayList<>();
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ACTIVE_SQL);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                members.add(mapResultSetToMember(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding active members: " + e.getMessage());
            throw e;
        }
        
        return members;
    }

    @Override
    public boolean exists(String memberId) throws SQLException {
        if (memberId == null || memberId.trim().isEmpty()) {
            return false;
        }
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(EXISTS_SQL)) {
            
            stmt.setString(1, memberId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking member existence: " + e.getMessage());
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
            System.err.println("Error counting members: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Search members by name or email
     */
    public List<Member> search(String query) throws SQLException {
        if (query == null || query.trim().isEmpty()) {
            return findActiveMembers();
        }
        
        List<Member> members = new ArrayList<>();
        String searchPattern = "%" + query.trim() + "%";
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(SEARCH_SQL)) {
            
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    members.add(mapResultSetToMember(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error searching members: " + e.getMessage());
            throw e;
        }
        
        return members;
    }

    /**
     * Alias method for search - searches by name or email
     */
    public List<Member> searchByName(String query) throws SQLException {
        return search(query);
    }

    /**
     * Find member by email address
     */
    public Optional<Member> findByEmail(String email) throws SQLException {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_EMAIL_SQL)) {
            
            stmt.setString(1, email.toLowerCase());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToMember(rs));
                }
                return Optional.empty();
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding member by email: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Activate or deactivate a member
     */
    public boolean updateStatus(String memberId, boolean isActive) throws SQLException {
        if (memberId == null || memberId.trim().isEmpty()) {
            throw new IllegalArgumentException("Member ID cannot be null or empty");
        }
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_STATUS_SQL)) {
            
            stmt.setBoolean(1, isActive);
            stmt.setString(2, memberId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating member status: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Activate a member
     */
    public boolean activate(String memberId) throws SQLException {
        return updateStatus(memberId, true);
    }
    
    /**
     * Generate the next member ID based on existing members
     * Format: M### (e.g., M001, M002, M003)
     */
    public String generateNextMemberId() throws SQLException {
        String sql = "SELECT member_id FROM " + TABLE_NAME + " ORDER BY member_id DESC LIMIT 1";
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                String lastId = rs.getString("member_id");
                // Extract the numeric part (e.g., "M004" -> 4)
                int lastNumber = Integer.parseInt(lastId.substring(1));
                // Increment and format with leading zeros
                return String.format("M%03d", lastNumber + 1);
            } else {
                // No members exist yet, start with M001
                return "M001";
            }
            
        } catch (SQLException e) {
            System.err.println("Error generating member ID: " + e.getMessage());
            throw e;
        } catch (NumberFormatException e) {
            // If parsing fails, default to M001
            System.err.println("Error parsing member ID: " + e.getMessage());
            return "M001";
        }
    }

    /**
     * Maps a ResultSet row to a Member object
     */
    private Member mapResultSetToMember(ResultSet rs) throws SQLException {
        Member member = new Member();
        member.setMemberId(rs.getString("member_id"));
        member.setFirstName(rs.getString("first_name"));
        member.setLastName(rs.getString("last_name"));
        member.setEmail(rs.getString("email"));
        member.setPhone(rs.getString("phone"));
        member.setAddress(rs.getString("address"));
        
        // Handle dates
        Date membershipDate = rs.getDate("membership_date");
        if (membershipDate != null) {
            member.setMembershipDate(membershipDate.toLocalDate());
        }
        
        member.setActive(rs.getBoolean("is_active"));
        
        // Handle timestamps
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            member.setCreatedAt(created.toLocalDateTime());
        }
        
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            member.setUpdatedAt(updated.toLocalDateTime());
        }
        
        return member;
    }
}