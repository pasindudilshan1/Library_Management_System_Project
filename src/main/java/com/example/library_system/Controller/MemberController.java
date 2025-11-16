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
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for member management operations.
 * Handles member registration, search, and management with proper validation.
 */
public class MemberController implements Initializable {
    
    // Search Components
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button clearButton;
    @FXML private Button addButton;
    
    // Form Fields
    @FXML private TextField memberIdField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextArea addressField;
    
    // Table Components
    @FXML private TableView<Member> membersTable;
    @FXML private TableColumn<Member, String> idColumn;
    @FXML private TableColumn<Member, String> nameColumn;
    @FXML private TableColumn<Member, String> emailColumn;
    @FXML private TableColumn<Member, String> phoneColumn;
    @FXML private TableColumn<Member, String> statusColumn;
    
    // Action Buttons
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    
    // Status Labels
    @FXML private Label memberCountLabel;
    @FXML private Label totalMembersLabel;
    @FXML private Label memberStatusLabel;
    @FXML private Label memberSinceLabel;
    @FXML private Label activeBooksLabel;
    
    // Data Access Objects
    private MemberDAO memberDAO;
    private ObservableList<Member> membersList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        memberDAO = new MemberDAO();
        setupTableColumns();
        loadAllMembers();
        setupTableSelectionListener();
    }

    /**
     * Set up table columns with proper cell value factories
     */
    private void setupTableColumns() {
        if (idColumn != null) {
            idColumn.setCellValueFactory(data -> 
                new SimpleStringProperty(String.valueOf(data.getValue().getMemberId())));
        }
        if (nameColumn != null) {
            nameColumn.setCellValueFactory(data -> 
                new SimpleStringProperty(data.getValue().getFirstName() + " " + data.getValue().getLastName()));
        }
        if (emailColumn != null) {
            emailColumn.setCellValueFactory(data -> 
                new SimpleStringProperty(data.getValue().getEmail()));
        }
        if (phoneColumn != null) {
            phoneColumn.setCellValueFactory(data -> 
                new SimpleStringProperty(data.getValue().getPhone()));
        }
        if (statusColumn != null) {
            statusColumn.setCellValueFactory(data -> 
                new SimpleStringProperty(data.getValue().isActive() ? "Active" : "Inactive"));
        }
    }

    /**
     * Set up table selection listener to populate form fields
     */
    private void setupTableSelectionListener() {
        if (membersTable != null) {
            membersTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        populateFormFields(newValue);
                    }
                }
            );
        }
    }

    /**
     * Populate form fields with selected member data
     */
    private void populateFormFields(Member member) {
        if (memberIdField != null) memberIdField.setText(String.valueOf(member.getMemberId()));
        if (firstNameField != null) firstNameField.setText(member.getFirstName());
        if (lastNameField != null) lastNameField.setText(member.getLastName());
        if (emailField != null) emailField.setText(member.getEmail());
        if (phoneField != null) phoneField.setText(member.getPhone());
        if (addressField != null) addressField.setText(member.getAddress());
    }

    /**
     * Search members by name or email
     */
    @FXML
    protected void searchMembers() {
        try {
            String searchTerm = searchField != null ? searchField.getText().trim() : "";
            
            List<Member> searchResults;
            if (searchTerm.isEmpty()) {
                searchResults = memberDAO.findAll();
            } else {
                searchResults = memberDAO.searchByName(searchTerm);
            }
            
            membersList = FXCollections.observableArrayList(searchResults);
            if (membersTable != null) {
                membersTable.setItems(membersList);
            }
            
        } catch (SQLException e) {
            showError("Search Error", "Failed to search members: " + e.getMessage());
        }
    }

    /**
     * Clear all form fields
     */
    @FXML
    protected void clearFields() {
        if (memberIdField != null) memberIdField.clear();
        if (firstNameField != null) firstNameField.clear();
        if (lastNameField != null) lastNameField.clear();
        if (emailField != null) emailField.clear();
        if (phoneField != null) phoneField.clear();
        if (addressField != null) addressField.clear();
        if (searchField != null) searchField.clear();
        
        // Clear table selection
        if (membersTable != null) {
            membersTable.getSelectionModel().clearSelection();
        }
        
        loadAllMembers(); // Refresh the table
    }

    /**
     * Add a new member
     */
    @FXML
    protected void addMember() {
        try {
            // Validate input
            if (!validateInput()) {
                return;
            }
            
            // Create new member
            Member newMember = new Member();
            // Generate and set member ID
            String newMemberId = memberDAO.generateNextMemberId();
            newMember.setMemberId(newMemberId);
            newMember.setFirstName(firstNameField.getText().trim());
            newMember.setLastName(lastNameField.getText().trim());
            newMember.setEmail(emailField.getText().trim());
            newMember.setPhone(phoneField.getText().trim());
            newMember.setAddress(addressField.getText().trim());
            newMember.setMembershipDate(LocalDate.now());
            newMember.setActive(true);
            
            // Save to database
            memberDAO.save(newMember);
            
            // Show success message
            showSuccess("Success", "Member added successfully!");
            
            // Clear fields and refresh table
            clearFields();
            
        } catch (SQLException e) {
            showError("Database Error", "Failed to add member: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            showError("Validation Error", e.getMessage());
        }
    }

    /**
     * Update selected member
     */
    @FXML
    protected void updateMember() {
        try {
            Member selectedMember = membersTable != null ? membersTable.getSelectionModel().getSelectedItem() : null;
            if (selectedMember == null) {
                showWarning("No Selection", "Please select a member to update.");
                return;
            }
            
            // Validate input
            if (!validateInput()) {
                return;
            }
            
            // Update member details
            selectedMember.setFirstName(firstNameField.getText().trim());
            selectedMember.setLastName(lastNameField.getText().trim());
            selectedMember.setEmail(emailField.getText().trim());
            selectedMember.setPhone(phoneField.getText().trim());
            selectedMember.setAddress(addressField.getText().trim());
            
            // Save changes
            memberDAO.update(selectedMember);
            
            showSuccess("Success", "Member updated successfully!");
            
            // Refresh table
            loadAllMembers();
            
        } catch (SQLException e) {
            showError("Database Error", "Failed to update member: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            showError("Validation Error", e.getMessage());
        }
    }

    /**
     * Delete selected member
     */
    @FXML
    protected void deleteMember() {
        try {
            Member selectedMember = membersTable != null ? membersTable.getSelectionModel().getSelectedItem() : null;
            if (selectedMember == null) {
                showWarning("No Selection", "Please select a member to delete.");
                return;
            }
            
            // Confirm deletion
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Deletion");
            confirmAlert.setHeaderText("Delete Member");
            confirmAlert.setContentText("Are you sure you want to delete " + 
                selectedMember.getFirstName() + " " + selectedMember.getLastName() + "?");
            
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                memberDAO.delete(selectedMember.getMemberId());
                showSuccess("Success", "Member deleted successfully!");
                
                clearFields();
                loadAllMembers();
            }
            
        } catch (SQLException e) {
            showError("Database Error", "Failed to delete member: " + e.getMessage());
        }
    }

    /**
     * Load all members into the table
     */
    private void loadAllMembers() {
        try {
            List<Member> allMembers = memberDAO.findAll();
            membersList = FXCollections.observableArrayList(allMembers);
            if (membersTable != null) {
                membersTable.setItems(membersList);
            }
            updateMemberCount();
        } catch (SQLException e) {
            showError("Database Error", "Failed to load members: " + e.getMessage());
        }
    }

    /**
     * Update member count labels
     */
    private void updateMemberCount() {
        if (membersList != null) {
            int totalCount = membersList.size();
            int activeCount = (int) membersList.stream().filter(Member::isActive).count();
            
            if (memberCountLabel != null) {
                memberCountLabel.setText("(" + totalCount + " members)");
            }
            if (totalMembersLabel != null) {
                totalMembersLabel.setText(String.valueOf(activeCount));
            }
        }
    }

    /**
     * Validate form input
     */
    private boolean validateInput() {
        if (firstNameField == null || firstNameField.getText().trim().isEmpty()) {
            showWarning("Validation Error", "First name is required.");
            return false;
        }
        
        if (lastNameField == null || lastNameField.getText().trim().isEmpty()) {
            showWarning("Validation Error", "Last name is required.");
            return false;
        }
        
        if (emailField == null || emailField.getText().trim().isEmpty()) {
            showWarning("Validation Error", "Email is required.");
            return false;
        }
        
        if (phoneField == null || phoneField.getText().trim().isEmpty()) {
            showWarning("Validation Error", "Phone number is required.");
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

    /**
     * Deactivate selected member
     */
    @FXML
    protected void deactivateMember() {
        try {
            Member selectedMember = membersTable != null ? membersTable.getSelectionModel().getSelectedItem() : null;
            if (selectedMember == null) {
                showWarning("No Selection", "Please select a member to deactivate.");
                return;
            }
            
            // Confirm deactivation
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Deactivation");
            confirmAlert.setHeaderText("Deactivate Member");
            confirmAlert.setContentText("Are you sure you want to deactivate " + 
                selectedMember.getFirstName() + " " + selectedMember.getLastName() + "?");
            
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                selectedMember.setActive(false);
                memberDAO.update(selectedMember);
                showSuccess("Success", "Member deactivated successfully!");
                
                clearFields();
                loadAllMembers();
            }
            
        } catch (SQLException e) {
            showError("Database Error", "Failed to deactivate member: " + e.getMessage());
        }
    }

    /**
     * Toggle showing inactive members
     */
    @FXML
    protected void toggleInactiveMembers() {
        // This will be called when checkbox state changes
        loadAllMembers();
    }
}
