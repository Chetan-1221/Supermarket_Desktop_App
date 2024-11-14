package com.example.dbms_proj1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class SignupPage {

    @FXML private TextField signupUserText;
    @FXML private PasswordField signupPwBox;
    @FXML private Button signupSubmitButton;
    @FXML private RadioButton adminRadioButton;
    @FXML private RadioButton userRadioButton;
    @FXML private ToggleGroup roleGroup;

    @FXML
    public void initialize() {
        // Initialize ToggleGroup and assign it to RadioButtons
        roleGroup = new ToggleGroup();
        adminRadioButton.setToggleGroup(roleGroup);
        userRadioButton.setToggleGroup(roleGroup);
    }
    @FXML
    private void handleSignupSubmit(ActionEvent event) {
        String username = signupUserText.getText();
        String password = signupPwBox.getText();
        String role = adminRadioButton.isSelected() ? "admin" : "cashier"; // Default to "cashier" if not selected

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "All fields must be filled.");
            return;
        }

        addUserToDatabase(username, password, role);
    }

    private void addUserToDatabase(String username, String password, String role) {
        try (Connection conn = DatabaseConnector.getConnection()) {
            String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Account created successfully.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to create account.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error connecting to the database.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
