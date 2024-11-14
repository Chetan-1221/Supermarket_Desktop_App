package com.example.dbms_proj1;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import java.util.Optional;

public class CashierPage extends Application {

    @FXML
    private VBox cashierPane; // Update to match the FXML

    @FXML
    private TextField searchProductField;

    @FXML
    private TableView<Product> productTableView;

    @FXML
    private TableColumn<Product, Integer> productIdColumn;

    @FXML
    private TableColumn<Product, String> productNameColumn;

    @FXML
    private TableColumn<Product, Double> priceColumn;

    @FXML
    private TableColumn<Product, Integer> stockQuantityColumn;

    @FXML
    private TableView<OrderItem> orderTableView;

    @FXML
    private TableColumn<OrderItem, Integer> orderProductIdColumn;

    @FXML
    private TableColumn<OrderItem, String> orderProductNameColumn;

    @FXML
    private TableColumn<OrderItem, Double> orderPriceColumn;

    @FXML
    private TableColumn<OrderItem, Integer> orderQuantityColumn;

    @FXML
    private TextField customerIdField;

    @FXML
    private TextField customerNameField;

    @FXML
    private TextField customerLocationField;

    @FXML
    private TextField customerPhoneField;

    @FXML
    private ComboBox<String> paymentMethodComboBox;

    @FXML
    private TextArea orderDetailsArea;

    private Stage primaryStage;
    private ObservableList<OrderItem> orderItems;
    private ObservableList<Product> products;

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CashierPage.fxml"));
            cashierPane = loader.load();
            Scene scene = new Scene(cashierPane, 1700, 1040);
            scene.getStylesheets().add(getClass().getResource("style2.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.setTitle("Supermarket Management System - Cashier Page");
            // Set the primaryStage in the controller
            CashierPage controller = loader.getController();
            controller.setPrimaryStage(primaryStage);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    public void initialize() {
        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("productPrice"));
        stockQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("productQuantity"));

        orderProductIdColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));
        orderProductNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        orderPriceColumn.setCellValueFactory(new PropertyValueFactory<>("productPrice"));
        orderQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        orderItems = FXCollections.observableArrayList();
        orderTableView.setItems(orderItems);

        paymentMethodComboBox.setItems(FXCollections.observableArrayList("Cash", "Card", "UPI"));

        loadProducts();
    }

    @FXML
    private void handleSearchProducts() {
        String searchText = searchProductField.getText().trim();
        System.out.println("Search text: " + searchText);

        try (Connection conn = DatabaseConnector.getConnection()) {
            String sql = "SELECT * FROM products WHERE product_id = ? OR product_name LIKE ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, searchText);
            pstmt.setString(2, "%" + searchText + "%");
            ResultSet rs = pstmt.executeQuery();

            products = FXCollections.observableArrayList();

            while (rs.next()) {
                int productId = rs.getInt("product_id");
                String productName = rs.getString("product_name");
                double price = rs.getDouble("product_price");
                int stockQuantity = rs.getInt("product_quantity");

                Product product = new Product(productId, productName, price, stockQuantity);
                products.add(product);
            }
            productTableView.setItems(products);
            productTableView.refresh();

            if (products.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "No Results", "No products found matching your search.");
            } else {
                System.out.println("Products found: " + products.size());
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error searching products.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddToOrder() {
        Product selectedProduct = productTableView.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            TextInputDialog dialog = new TextInputDialog("1");
            dialog.setTitle("Enter Quantity");
            dialog.setHeaderText("Enter quantity for " + selectedProduct.getProductName());
            dialog.setContentText("Quantity:");

            dialog.showAndWait().ifPresent(quantity -> {
                try {
                    int qty = Integer.parseInt(quantity);
                    if (qty > 0) {
                        if (qty > selectedProduct.getProductQuantity()) {
                            showAlert(Alert.AlertType.WARNING, "Insufficient Stock", "Not enough stock available.");
                        } else {
                            boolean exists = false;
                            for (OrderItem item : orderItems) {
                                if (item.getProductId() == selectedProduct.getProductId()) {
                                    item.setQuantity(item.getQuantity() + qty);
                                    exists = true;
                                    break;
                                }
                            }
                            if (!exists) {
                                orderItems.add(new OrderItem(selectedProduct.getProductId(), selectedProduct.getProductName(), selectedProduct.getProductPrice(), qty));
                            }

                            selectedProduct.setProductQuantity(selectedProduct.getProductQuantity() - qty);
                            productTableView.refresh();
                            orderTableView.refresh();
                        }
                    } else {
                        showAlert(Alert.AlertType.WARNING, "Invalid Quantity", "Quantity must be greater than zero.");
                    }
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid number.");
                }
            });
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product to add.");
        }
    }

    @FXML
    private void handleRemoveFromOrder() {
        OrderItem selectedOrder = orderTableView.getSelectionModel().getSelectedItem();
        if (selectedOrder != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Remove From Order");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to remove " + selectedOrder.getProductName() + " from the order?");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    for (Product product : products) {
                        if (product.getProductId() == selectedOrder.getProductId()) {
                            product.setProductQuantity(product.getProductQuantity() + selectedOrder.getQuantity());
                            break;
                        }
                    }
                    productTableView.refresh();

                    orderItems.remove(selectedOrder);
                    orderTableView.refresh();
                }
            });
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an item to remove.");
        }
    }

    @FXML
    private void handleGenerateBill() {
        String customerId = customerIdField.getText().trim();
        String customerName = customerNameField.getText().trim();
        String customerLocation = customerLocationField.getText().trim();
        String customerPhone = customerPhoneField.getText().trim();
        String paymentMethod = paymentMethodComboBox.getValue();

        if (customerId.isEmpty() || customerName.isEmpty() || customerLocation.isEmpty() || customerPhone.isEmpty() || paymentMethod == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all customer details and select a payment method.");
            return;
        }

        double totalAmount = orderItems.stream().mapToDouble(item -> item.getProductPrice() * item.getQuantity()).sum();

        try (Connection conn = DatabaseConnector.getConnection()) {
            conn.setAutoCommit(false);

            // Insert into Customers table (if not already exists)
            int customerIdInt = Integer.parseInt(customerId);
            if (!customerExists(conn, customerIdInt)) {
                insertCustomer(conn, customerIdInt, customerName, customerPhone, customerLocation);
            }

            // Insert into Orders table
            int orderId = insertOrder(conn, customerIdInt, paymentMethod, totalAmount);

            // Insert into OrderItems table
            insertOrderItems(conn, orderId);

            // Update Product quantities
            updateProductQuantities(conn);

            conn.commit();

            // Clear order items after successful transaction
            orderItems.clear();
            orderTableView.refresh();

            // Display order details
            StringBuilder orderDetails = new StringBuilder();
            orderDetails.append("Customer ID: ").append(customerId).append("\n");
            orderDetails.append("Customer Name: ").append(customerName).append("\n");
            orderDetails.append("Customer Location: ").append(customerLocation).append("\n");
            orderDetails.append("Customer Phone: ").append(customerPhone).append("\n");
            orderDetails.append("Payment Method: ").append(paymentMethod).append("\n");
            orderDetails.append("Total Amount: $").append(totalAmount).append("\n\n");
            orderDetails.append("Items:\n");

            for (OrderItem item : orderItems) {
                orderDetails.append(item.getProductName()).append(" - ").append(item.getQuantity()).append(" x $").append(item.getProductPrice()).append("\n");
            }
            orderDetailsArea.setText(orderDetails.toString());

            showAlert(Alert.AlertType.INFORMATION, "Order Generated", "Order successfully generated and database updated.");
        } catch (SQLException e) {
            /*try {
                conn.rollback(); // Rollback changes if there's an error
            } catch (SQLException rollbackEx) {
                System.out.println("Rollback failed! " + rollbackEx.getMessage());
            }*/
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error updating database or generating order.");
            e.printStackTrace();
        }
    }

    private void updateProductQuantities(Connection conn) throws SQLException {
        String updateSql = "UPDATE Products SET product_quantity = product_quantity - ? WHERE product_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
            for (OrderItem item : orderItems) {
                pstmt.setInt(1, item.getQuantity());
                pstmt.setInt(2, item.getProductId());
                pstmt.executeUpdate();
            }
        }
    }


    private boolean customerExists(Connection conn, int customerId) throws SQLException {
        String sql = "SELECT 1 FROM Customers WHERE customer_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void insertCustomer(Connection conn, int customerId, String customerName, String customerPhone, String customerLocation) throws SQLException {
        String sql = "INSERT INTO Customers (customer_id, customer_name, customer_phone, customer_location) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            pstmt.setString(2, customerName);
            pstmt.setString(3, customerPhone);
            pstmt.setString(4, customerLocation);
            pstmt.executeUpdate();
        }
    }

    private int insertOrder(Connection conn, int customerId, String paymentMethod, double totalAmount) throws SQLException {
        String sql = "INSERT INTO Orders (customer_id, payment_method, total_amount, order_date) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, customerId);
            pstmt.setString(2, paymentMethod);
            pstmt.setDouble(3, totalAmount);
            pstmt.setDate(4, java.sql.Date.valueOf(LocalDate.now()));
            pstmt.executeUpdate();
            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                throw new SQLException("Creating order failed, no ID obtained.");
            }
        }
    }

    private void insertOrderItems(Connection conn, int orderId) throws SQLException {
        String sql = "INSERT INTO OrderItems (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (OrderItem item : orderItems) {
                pstmt.setInt(1, orderId);
                pstmt.setInt(2, item.getProductId());
                pstmt.setInt(3, item.getQuantity());
                pstmt.setDouble(4, item.getProductPrice());
                pstmt.executeUpdate();
            }
        }
    }

    @FXML
    private void handleRefresh() {
        loadProducts();
        clearFields();
    }

    private void loadProducts() {
        try (Connection conn = DatabaseConnector.getConnection()) {
            String sql = "SELECT * FROM products";
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                products = FXCollections.observableArrayList();
                while (rs.next()) {
                    int productId = rs.getInt("product_id");
                    String productName = rs.getString("product_name");
                    double price = rs.getDouble("product_price");
                    int stockQuantity = rs.getInt("product_quantity");

                    Product product = new Product(productId, productName, price, stockQuantity);
                    products.add(product);
                }
                productTableView.setItems(products);
                productTableView.refresh();
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error loading products.");
            e.printStackTrace();
        }
    }

    private void clearFields() {
        searchProductField.clear();
        customerIdField.clear();
        customerNameField.clear();
        customerLocationField.clear();
        customerPhoneField.clear();
        paymentMethodComboBox.getSelectionModel().clearSelection();
        orderItems.clear();
        orderTableView.refresh();
        orderDetailsArea.clear();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to logout?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Close the current stage (window)
                primaryStage.close();

                try {
                    new LoginPage().start(new Stage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
