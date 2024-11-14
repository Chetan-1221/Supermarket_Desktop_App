module com.example.dbms_proj1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.dbms_proj1 to javafx.fxml;
    exports com.example.dbms_proj1;
}