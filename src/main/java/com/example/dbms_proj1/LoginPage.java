package com.example.dbms_proj1;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginPage extends Application {

    @FXML private VBox loginPane;
    @FXML private Label titleLabel;
    @FXML private Label userLabel;
    @FXML private TextField userText;
    @FXML private Label pwLabel;
    @FXML private PasswordField pwBox;
    @FXML private Button loginButton;
    @FXML private Button signupButton;

    private Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginPage.fxml"));
            loader.setController(this); // Set this class as the controller
            loginPane = loader.load();
            Scene scene = new Scene(loginPane, 1700, 1040);
            scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.setTitle("Supermarket Management System - Login");
            this.primaryStage = primaryStage; // Store primary stage reference
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = userText.getText();
        String password = pwBox.getText();
        authenticateUser(username, password);
    }

    @FXML
    private void handleSignup(ActionEvent event) {
        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("SignupPage.fxml"));
            VBox signupPane = loader.load();
            Scene scene = new Scene(signupPane, 600, 400);
            scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Sign Up");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Unable to load sign up page.");
        }
    }

    private void authenticateUser(String username, String password) {
        try (Connection conn = DatabaseConnector.getConnection()) {
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Successfully authenticated
                showAlert(Alert.AlertType.INFORMATION, "Login Successful", "Welcome, " + username + "!");
                // Redirect to the appropriate interface
                if (username.equals("admin") || username.equals("admin1") || username.equals("admin2")) {
                    // Load administrator interface
                    new AdministratorPage().start(new Stage());
                } else if(username.equals("cashier") || username.equals("cashier1") || username.equals("cashier2")) {
                    // Load user interface
                    new CashierPage().start(new Stage());
                }
                primaryStage.close();
            } else {
                // Authentication failed
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password.");
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
