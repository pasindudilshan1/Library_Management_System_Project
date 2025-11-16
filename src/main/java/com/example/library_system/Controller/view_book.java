package com.example.library_system.Controller;

import com.example.library_system.Models.Book;
import com.example.library_system.Models.Member;
import com.example.library_system.Database.BookDAO;
import com.example.library_system.Database.MemberDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class view_book {
    @FXML
    private TableView <Book> tableView;
    @FXML
    private TableColumn<Book, String> book_IdField;
    @FXML
    private TableColumn<Book,String>titleField;
    @FXML
    private TableColumn<Book,String>authorField;
    @FXML
    private TableColumn<Book,String>genreField;
    @FXML
    private TableColumn<Book, Integer>copiesField;

    @FXML
    private TableView<Member> membersTable;
    @FXML
    private TableColumn<Member, String> memberIdColumn;
    @FXML
    private TableColumn<Member, String> firstNameColumn;
    @FXML
    private TableColumn<Member, String> lastNameColumn;
    @FXML
    private TableColumn<Member, String> emailColumn;

    private ObservableList<Book> Booklist= FXCollections.observableArrayList();
    private ObservableList<Member> Memberlist = FXCollections.observableArrayList();

    private BookDAO bookDAO = new BookDAO();
    private MemberDAO memberDAO = new MemberDAO();

    @FXML
    private void initialize(){
        // Set up books table
        book_IdField.setCellValueFactory(new PropertyValueFactory<>("bookId"));
        titleField.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorField.setCellValueFactory(new PropertyValueFactory<>("author"));
        genreField.setCellValueFactory(new PropertyValueFactory<>("genre"));
        copiesField.setCellValueFactory(new PropertyValueFactory<>("availableCopies"));
        tableView.setItems(Booklist);

        // Set up members table
        memberIdColumn.setCellValueFactory(new PropertyValueFactory<>("memberId"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        membersTable.setItems(Memberlist);

        loadData();
    }

    private void loadData() {
        try {
            // Load available books
            Booklist.clear();
            Booklist.addAll(bookDAO.findAll().stream()
                .filter(book -> book.getAvailableCopies() > 0)
                .toList());

            // Load active members
            Memberlist.clear();
            Memberlist.addAll(memberDAO.findAll().stream()
                .filter(Member::isActive)
                .toList());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
