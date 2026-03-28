package com.demo.productordermanagement.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Order entity representing customer orders")
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the order", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;
    
    @Column(nullable = false)
    @Schema(description = "ID of the product being ordered", example = "1", required = true)
    private Long productId;
    
    @Column(nullable = false)
    @Schema(description = "Name of the customer", example = "John Doe", required = true)
    private String customerName;
    
    @Column(nullable = false)
    @Schema(description = "Quantity of products ordered", example = "2", required = true)
    private Integer quantity;
    
    @Column(nullable = false)
    @Schema(description = "Total price of the order", example = "1999.98", accessMode = Schema.AccessMode.READ_ONLY)
    private Double totalPrice;
    
    @Column(nullable = false)
    @Schema(description = "Date and time when the order was placed", example = "2024-01-15T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime orderDate;
}
