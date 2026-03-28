package com.demo.productordermanagement.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Product entity representing items in the catalog")
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the product", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;
    
    @Column(nullable = false)
    @Schema(description = "Name of the product", example = "Laptop", required = true)
    private String name;
    
    @Column(nullable = false)
    @Schema(description = "Price of the product", example = "999.99", required = true)
    private Double price;
    
    @Column(nullable = false)
    @Schema(description = "Available stock quantity", example = "50", required = true)
    private Integer stock;
}
