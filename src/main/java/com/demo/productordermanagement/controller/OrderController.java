package com.demo.productordermanagement.controller;

import com.demo.productordermanagement.dto.OrderDTO;
import com.demo.productordermanagement.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "Order management APIs")
@Slf4j
public class OrderController {
    
    private final OrderService orderService;
    
    @Operation(
            summary = "Get all orders",
            description = "Retrieve a list of all orders"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of orders",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderDTO.class)))
    })
    @GetMapping
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        log.info("GET request received for all orders");
        List<OrderDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }
    
    @Operation(
            summary = "Get order by ID",
            description = "Retrieve a specific order by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved order",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderDTO.class))),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(
            @Parameter(
                    description = "Order ID to retrieve",
                    required = true,
                    example = "1"
            )
            @PathVariable(name = "id") Long id) {
        log.info("GET request received for order with id: {}", id);
        OrderDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }
    
    @Operation(
            summary = "Create a new order",
            description = "Place a new order for a product"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or product out of stock",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Order object to be created",
                    required = true,
                    content = @Content(schema = @Schema(implementation = OrderDTO.class)))
            @Valid @RequestBody OrderDTO orderDTO) {
        log.info("POST request received to create order for customer: {}", orderDTO.getCustomerName());
        OrderDTO createdOrder = orderService.createOrder(orderDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }
    
    @Operation(
            summary = "Cancel order",
            description = "Cancel an existing order and restore product stock"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Order cancelled successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelOrder(
            @Parameter(
                    description = "Order ID to cancel",
                    required = true,
                    example = "1"
            )
            @PathVariable(name = "id") Long id) {
        log.info("DELETE request received for order with id: {}", id);
        orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }
}
