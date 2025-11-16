module com.example.library_system {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires java.base;
    requires java.desktop;

    opens com.example.library_system to javafx.fxml;
    opens com.example.library_system.Controller to javafx.fxml;
    opens com.example.library_system.Models to javafx.fxml;
    opens com.example.library_system.Database to javafx.fxml;
    
    exports com.example.library_system;
    exports com.example.library_system.Controller;
    exports com.example.library_system.Models;
    exports com.example.library_system.Database;
}