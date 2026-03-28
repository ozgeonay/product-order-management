package com.demo.productordermanagement.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Error Response")
public class ErrorResponse {
    
    @Schema(description = "Timestamp when error occurred", example = "2024-01-15T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    @Schema(description = "HTTP status code", example = "404")
    private Integer status;
    
    @Schema(description = "Error type", example = "Not Found")
    private String error;
    
    @Schema(description = "Error message", example = "Product not found with id: 1")
    private String message;
    
    @Schema(description = "Request path", example = "/api/products/1")
    private String path;
    
    @Schema(description = "Validation errors", example = "[\"Product name is required\"]")
    private List<String> validationErrors;
}
