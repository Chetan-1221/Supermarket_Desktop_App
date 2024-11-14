package com.example.dbms_proj1;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class AdministratorPage extends Application {

    @FXML
    private VBox adminPane;

    @FXML
    private TextField productIdField;

    @FXML
    private TextField productNameField;

    @FXML
    private TextField priceField;

    @FXML
    private TextField stockQuantityField;

    @FXML
    private TextField searchProductField;

    @FXML
    private TableView<Product> productTableView;

    @FXML
    private TableColumn<Product, String> productIdColumn;

    @FXML
    private TableColumn<Product, String> productNameColumn;

    @FXML
    private TableColumn<Product, Double> priceColumn;

    @FXML
    private TableColumn<Product, Integer> stockQuantityColumn;

    @FXML
    private TableView<Order> orderHistoryTableView;

    @FXML
    private TableColumn<Order, Integer> orderIdColumn;

    @FXML
    private TableColumn<Order, String> customerNameColumn;

    @FXML
    private TableColumn<Order, Double> totalAmountColumn;

    @FXML
    private TableColumn<Order, String> orderDateColumn;

    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AdministratorPage.fxml"));
            loader.setController(this);
            adminPane = loader.load();
            Scene scene = new Scene(adminPane, 1700, 1040);
            scene.getStylesheets().add(getClass().getResource("style1.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.setTitle("Supermarket Management System - Administrator Page");
            this.primaryStage = primaryStage;
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @FXML
    public void initialize() {
        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("productPrice"));
        stockQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("productQuantity"));

        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        totalAmountColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
    }

    @FXML
    private void handleAddProduct() {
        int productId = Integer.parseInt(productIdField.getText());
        String productName = productNameField.getText();
        double price = Double.parseDouble(priceField.getText());
        int stockQuantity = Integer.parseInt(stockQuantityField.getText());

        /*/ Validate input
        /if (productId || productName.isEmpty() || priceStr || stockQuantityStr) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all fields.");
            return;
        }*/

        /*try {
            double price = Double.parseDouble(priceStr);
            int stockQuantity = Integer.parseInt(stockQuantityStr);*/

            // Insert into database
            try (Connection conn = DatabaseConnector.getConnection()) {
                String sql = "INSERT INTO products (product_id, product_name, product_price, product_quantity) VALUES (?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, productId);
                pstmt.setString(2, productName);
                pstmt.setDouble(3, price);
                pstmt.setInt(4, stockQuantity);
                pstmt.executeUpdate();

                showAlert(Alert.AlertType.INFORMATION, "Success", "Product added successfully.");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Error adding product to database.");
                e.printStackTrace();
            }
        /*} catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter valid price and stock quantity.");
        }*/
    }//

    @FXML
    private void handleSearchProducts() {
        String searchText = searchProductField.getText().trim();
        System.out.println("Search text: " + searchText);

        // Perform search in database
        try (Connection conn = DatabaseConnector.getConnection()) {
            String sql = "SELECT * FROM products WHERE product_id = ? OR product_name LIKE ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, searchText);
            pstmt.setString(2, "%" + searchText + "%");
            ResultSet rs = pstmt.executeQuery();

            // Clear previous data
            productTableView.getItems().clear();

            // Populate table with search results
            while (rs.next()) {
                int productId = rs.getInt("product_id");
                String productName = rs.getString("product_name");
                double price = rs.getDouble("product_price");
                int stockQuantity = rs.getInt("product_quantity");

                Product product = new Product(productId, productName, price, stockQuantity);
                System.out.println("Products found "+ product.getProductName());
                productTableView.getItems().add(product);
                System.out.println("Products added to instance");
            }
            productTableView.refresh(); // Refresh the table view to display the updated data

            if (productTableView.getItems().isEmpty()) {
                System.out.println("No products found.");
                showAlert(Alert.AlertType.INFORMATION, "No Results", "No products found matching your search.");
            } else {
                System.out.println("Products found: " + productTableView.getItems().size());
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error searching products.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewOrderHistory() {
        System.out.println("::");
        try (Connection conn = DatabaseConnector.getConnection()) {
            String sql = "SELECT * FROM orders";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            System.out.println("::");

            // Clear previous data
            orderHistoryTableView.getItems().clear();

            // Fill table with orders
            while (rs.next()) {
                int orderId = rs.getInt("order_id");
                String customerName = getCustomerName(rs.getInt("customer_id")); //method to get customer name from different table
                double totalAmount = rs.getDouble("total_amount");
                LocalDate orderDate = rs.getDate("order_date").toLocalDate();

                Order order = new Order(orderId, customerName, totalAmount, orderDate);
                orderHistoryTableView.getItems().add(order);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error fetching order history.");
            e.printStackTrace();
        }
    }

    // Example method to fetch customer name based on customer_id
    private String getCustomerName(int customerId) throws SQLException {
        System.out.println("In getcustomer name method");
        String customerName = "";
        try (Connection conn = DatabaseConnector.getConnection()) {
            String sql = "SELECT customer_name FROM customers WHERE customer_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                customerName = rs.getString("customer_name");
            }
        }
        return customerName;
    }


    @FXML
    private void handleLogout() {
        primaryStage.close();
        try {
            new LoginPage().start(new Stage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
