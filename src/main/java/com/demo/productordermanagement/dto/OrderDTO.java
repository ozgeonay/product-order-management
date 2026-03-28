package com.demo.productordermanagement.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Order Data Transfer Object")
public class OrderDTO {
    
    @Schema(description = "Unique identifier of the order", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;
    
    @NotNull(message = "Product ID is required")
    @Schema(description = "ID of the product being ordered", example = "1", required = true)
    private Long productId;
    
    @NotBlank(message = "Customer name is required")
    @Schema(description = "Name of the customer", example = "John Doe", required = true)
    private String customerName;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Schema(description = "Quantity of products ordered", example = "2", required = true)
    private Integer quantity;
    
    @Schema(description = "Total price of the order", example = "1999.98", accessMode = Schema.AccessMode.READ_ONLY)
    private Double totalPrice;
    
    @Schema(description = "Date and time when the order was placed", example = "2024-01-15T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime orderDate;
}
