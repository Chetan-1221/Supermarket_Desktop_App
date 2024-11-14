package com.example.dbms_proj1;

import java.time.LocalDate;

public class Order {
    private int orderId;
    private String customerName;
    private double totalAmount;
    private LocalDate orderDate;

    public Order(int orderId, String customerName, double totalAmount, LocalDate orderDate) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.totalAmount = totalAmount;
        this.orderDate = orderDate;
    }

    public int getOrderId() {
        return orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    // Optionally, you can override toString() to customize TableView display
    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", customerName='" + customerName + '\'' +
                ", totalAmount=" + totalAmount +
                ", orderDate=" + orderDate +
                '}';
    }
}
