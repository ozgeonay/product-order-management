package com.demo.productordermanagement.constants;

public final class ErrorMessages {
    
    private ErrorMessages() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    public static final String PRODUCT_NOT_FOUND = "Product not found with id: %d";
    public static final String ORDER_NOT_FOUND = "Order not found with id: %d";
    public static final String OUT_OF_STOCK = "Not enough stock for product: %s. Available: %d, Requested: %d";
}
