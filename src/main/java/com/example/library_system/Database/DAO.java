package com.example.library_system.Database;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Generic Data Access Object interface defining common CRUD operations.
 * Demonstrates interface design and generics in Java.
 * 
 * @param <T> The entity type
 * @param <ID> The identifier type
 */
public interface DAO<T, ID> {
    
    /**
     * Saves a new entity to the database
     * @param entity the entity to save
     * @return true if saved successfully, false otherwise
     * @throws SQLException if database operation fails
     */
    boolean save(T entity) throws SQLException;
    
    /**
     * Updates an existing entity in the database
     * @param entity the entity to update
     * @return true if updated successfully, false otherwise
     * @throws SQLException if database operation fails
     */
    boolean update(T entity) throws SQLException;
    
    /**
     * Deletes an entity from the database by ID
     * @param id the identifier of the entity to delete
     * @return true if deleted successfully, false otherwise
     * @throws SQLException if database operation fails
     */
    boolean delete(ID id) throws SQLException;
    
    /**
     * Finds an entity by its ID
     * @param id the identifier to search for
     * @return Optional containing the entity if found, empty otherwise
     * @throws SQLException if database operation fails
     */
    Optional<T> findById(ID id) throws SQLException;
    
    /**
     * Retrieves all entities from the database
     * @return List of all entities
     * @throws SQLException if database operation fails
     */
    List<T> findAll() throws SQLException;
    
    /**
     * Checks if an entity exists in the database
     * @param id the identifier to check
     * @return true if entity exists, false otherwise
     * @throws SQLException if database operation fails
     */
    boolean exists(ID id) throws SQLException;
    
    /**
     * Counts the total number of entities
     * @return the total count
     * @throws SQLException if database operation fails
     */
    long count() throws SQLException;
}