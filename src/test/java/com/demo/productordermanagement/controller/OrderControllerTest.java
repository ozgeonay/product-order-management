package com.demo.productordermanagement.controller;

import com.demo.productordermanagement.dto.OrderDTO;
import com.demo.productordermanagement.exception.OutOfStockException;
import com.demo.productordermanagement.exception.ResourceNotFoundException;
import com.demo.productordermanagement.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@DisplayName("OrderController Integration Tests")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Nested
    @DisplayName("GET /api/orders Tests")
    class GetAllOrdersTests {

        @Test
        @DisplayName("Should return all orders with 200 OK")
        void shouldReturnAllOrders() throws Exception {
            // Given
            OrderDTO order1 = OrderDTO.builder()
                    .id(1L)
                    .productId(1L)
                    .customerName("John Doe")
                    .quantity(2)
                    .totalPrice(1999.98)
                    .orderDate(LocalDateTime.now())
                    .build();

            OrderDTO order2 = OrderDTO.builder()
                    .id(2L)
                    .productId(2L)
                    .customerName("Jane Smith")
                    .quantity(1)
                    .totalPrice(29.99)
                    .orderDate(LocalDateTime.now())
                    .build();

            when(orderService.getAllOrders()).thenReturn(Arrays.asList(order1, order2));

            // When & Then
            mockMvc.perform(get("/api/orders"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].customerName", is("John Doe")))
                    .andExpect(jsonPath("$[1].customerName", is("Jane Smith")));

            verify(orderService, times(1)).getAllOrders();
        }
    }

    @Nested
    @DisplayName("GET /api/orders/{id} Tests")
    class GetOrderByIdTests {

        @Test
        @DisplayName("Should return order when ID exists")
        void shouldReturnOrderWhenIdExists() throws Exception {
            // Given
            OrderDTO order = OrderDTO.builder()
                    .id(1L)
                    .productId(1L)
                    .customerName("John Doe")
                    .quantity(2)
                    .totalPrice(1999.98)
                    .build();

            when(orderService.getOrderById(1L)).thenReturn(order);

            // When & Then
            mockMvc.perform(get("/api/orders/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.customerName", is("John Doe")))
                    .andExpect(jsonPath("$.quantity", is(2)));
        }

        @Test
        @DisplayName("Should return 404 when order not found")
        void shouldReturn404WhenOrderNotFound() throws Exception {
            // Given
            when(orderService.getOrderById(999L))
                    .thenThrow(new ResourceNotFoundException("Order not found with id: 999"));

            // When & Then
            mockMvc.perform(get("/api/orders/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error", is("Not Found")));
        }
    }

    @Nested
    @DisplayName("POST /api/orders Tests")
    class CreateOrderTests {

        @Test
        @DisplayName("Should create order with valid data and return 201")
        void shouldCreateOrderWithValidData() throws Exception {
            // Given
            OrderDTO inputDTO = OrderDTO.builder()
                    .productId(1L)
                    .customerName("Alice Johnson")
                    .quantity(2)
                    .build();

            OrderDTO savedDTO = OrderDTO.builder()
                    .id(5L)
                    .productId(1L)
                    .customerName("Alice Johnson")
                    .quantity(2)
                    .totalPrice(1999.98)
                    .orderDate(LocalDateTime.now())
                    .build();

            when(orderService.createOrder(any(OrderDTO.class))).thenReturn(savedDTO);

            // When & Then
            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", is(5)))
                    .andExpect(jsonPath("$.customerName", is("Alice Johnson")))
                    .andExpect(jsonPath("$.totalPrice", is(1999.98)));

            verify(orderService, times(1)).createOrder(any(OrderDTO.class));
        }

        @Test
        @DisplayName("Should return 400 when customer name is blank")
        void shouldReturn400WhenCustomerNameIsBlank() throws Exception {
            // Given
            OrderDTO invalidDTO = OrderDTO.builder()
                    .productId(1L)
                    .customerName("")
                    .quantity(2)
                    .build();

            // When & Then
            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error", is("Validation Error")));

            verify(orderService, never()).createOrder(any());
        }

        @Test
        @DisplayName("Should return 400 when quantity is less than 1")
        void shouldReturn400WhenQuantityIsInvalid() throws Exception {
            // Given
            OrderDTO invalidDTO = OrderDTO.builder()
                    .productId(1L)
                    .customerName("Bob Wilson")
                    .quantity(0)
                    .build();

            // When & Then
            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error", is("Validation Error")));
        }

        @Test
        @DisplayName("Should return 400 when product is out of stock")
        void shouldReturn400WhenOutOfStock() throws Exception {
            // Given
            OrderDTO orderDTO = OrderDTO.builder()
                    .productId(1L)
                    .customerName("Carol White")
                    .quantity(100)
                    .build();

            when(orderService.createOrder(any(OrderDTO.class)))
                    .thenThrow(new OutOfStockException("Not enough stock for product: Laptop. Available: 10, Requested: 100"));

            // When & Then
            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(orderDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error", is("Out of Stock")))
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Not enough stock")));
        }

        @Test
        @DisplayName("Should return 404 when product not found")
        void shouldReturn404WhenProductNotFound() throws Exception {
            // Given
            OrderDTO orderDTO = OrderDTO.builder()
                    .productId(999L)
                    .customerName("Dave Brown")
                    .quantity(1)
                    .build();

            when(orderService.createOrder(any(OrderDTO.class)))
                    .thenThrow(new ResourceNotFoundException("Product not found with id: 999"));

            // When & Then
            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(orderDTO)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/orders/{id} Tests")
    class CancelOrderTests {

        @Test
        @DisplayName("Should cancel order successfully")
        void shouldCancelOrderSuccessfully() throws Exception {
            // Given
            doNothing().when(orderService).cancelOrder(1L);

            // When & Then
            mockMvc.perform(delete("/api/orders/1"))
                    .andExpect(status().isNoContent());

            verify(orderService, times(1)).cancelOrder(1L);
        }

        @Test
        @DisplayName("Should return 404 when canceling non-existent order")
        void shouldReturn404WhenCancelingNonExistentOrder() throws Exception {
            // Given
            doThrow(new ResourceNotFoundException("Order not found with id: 999"))
                    .when(orderService).cancelOrder(999L);

            // When & Then
            mockMvc.perform(delete("/api/orders/999"))
                    .andExpect(status().isNotFound());
        }
    }
}
