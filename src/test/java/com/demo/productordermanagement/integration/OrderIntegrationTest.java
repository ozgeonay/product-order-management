package com.demo.productordermanagement.integration;

import com.demo.productordermanagement.dto.OrderDTO;
import com.demo.productordermanagement.entity.Product;
import com.demo.productordermanagement.repository.OrderRepository;
import com.demo.productordermanagement.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Order Integration Tests - Full Stack")
class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        productRepository.deleteAll();

        testProduct = new Product();
        testProduct.setName("Laptop");
        testProduct.setPrice(999.99);
        testProduct.setStock(10);
        testProduct = productRepository.save(testProduct);
    }

    @Nested
    @DisplayName("Order CRUD Flow")
    class OrderCRUDFlowTests {

        @Test
        @DisplayName("Should create order, check stock decrease, then cancel and restore stock")
        void shouldPerformCompleteOrderFlow() throws Exception {
            // 1. Check initial product stock
            int initialStock = testProduct.getStock();
            assertThat(initialStock).isEqualTo(10);

            // 2. CREATE ORDER - POST /api/orders
            OrderDTO newOrder = OrderDTO.builder()
                    .productId(testProduct.getId())
                    .customerName("Integration Test Customer")
                    .quantity(3)
                    .build();

            String createResponse = mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newOrder)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.customerName", is("Integration Test Customer")))
                    .andExpect(jsonPath("$.quantity", is(3)))
                    .andExpect(jsonPath("$.totalPrice", closeTo(2999.97, 0.01)))
                    .andExpect(jsonPath("$.orderDate").exists())
                    .andReturn().getResponse().getContentAsString();

            OrderDTO createdOrder = objectMapper.readValue(createResponse, OrderDTO.class);
            Long orderId = createdOrder.getId();

            // 3. VERIFY STOCK DECREASED
            Product updatedProduct = productRepository.findById(testProduct.getId()).orElseThrow();
            assertThat(updatedProduct.getStock()).isEqualTo(7); // 10 - 3 = 7

            // 4. READ ORDER - GET /api/orders/{id}
            mockMvc.perform(get("/api/orders/" + orderId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(orderId.intValue())))
                    .andExpect(jsonPath("$.customerName", is("Integration Test Customer")));

            // 5. CANCEL ORDER - DELETE /api/orders/{id}
            mockMvc.perform(delete("/api/orders/" + orderId))
                    .andExpect(status().isNoContent());

            // 6. VERIFY STOCK RESTORED
            Product restoredProduct = productRepository.findById(testProduct.getId()).orElseThrow();
            assertThat(restoredProduct.getStock()).isEqualTo(10); // Back to 10

            // 7. VERIFY ORDER DELETED
            mockMvc.perform(get("/api/orders/" + orderId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Stock Management Integration")
    class StockManagementTests {

        @Test
        @DisplayName("Should handle multiple orders and track stock correctly")
        void shouldHandleMultipleOrdersAndTrackStock() throws Exception {
            // Initial stock: 10
            
            // Order 1: Buy 3 units
            OrderDTO order1 = OrderDTO.builder()
                    .productId(testProduct.getId())
                    .customerName("Customer 1")
                    .quantity(3)
                    .build();

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(order1)))
                    .andExpect(status().isCreated());

            Product afterOrder1 = productRepository.findById(testProduct.getId()).orElseThrow();
            assertThat(afterOrder1.getStock()).isEqualTo(7); // 10 - 3

            // Order 2: Buy 2 units
            OrderDTO order2 = OrderDTO.builder()
                    .productId(testProduct.getId())
                    .customerName("Customer 2")
                    .quantity(2)
                    .build();

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(order2)))
                    .andExpect(status().isCreated());

            Product afterOrder2 = productRepository.findById(testProduct.getId()).orElseThrow();
            assertThat(afterOrder2.getStock()).isEqualTo(5); // 7 - 2

            // Order 3: Buy 4 units
            OrderDTO order3 = OrderDTO.builder()
                    .productId(testProduct.getId())
                    .customerName("Customer 3")
                    .quantity(4)
                    .build();

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(order3)))
                    .andExpect(status().isCreated());

            Product afterOrder3 = productRepository.findById(testProduct.getId()).orElseThrow();
            assertThat(afterOrder3.getStock()).isEqualTo(1); // 5 - 4

            // Verify all orders exist
            mockMvc.perform(get("/api/orders"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)));
        }

        @Test
        @DisplayName("Should reject order when stock is insufficient")
        void shouldRejectOrderWhenStockInsufficient() throws Exception {
            // Try to order more than available
            OrderDTO largeOrder = OrderDTO.builder()
                    .productId(testProduct.getId())
                    .customerName("Greedy Customer")
                    .quantity(100)
                    .build();

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(largeOrder)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error", is("Out of Stock")))
                    .andExpect(jsonPath("$.message", containsString("Not enough stock")))
                    .andExpect(jsonPath("$.message", containsString("Available: 10")))
                    .andExpect(jsonPath("$.message", containsString("Requested: 100")));

            // Verify stock unchanged
            Product unchangedProduct = productRepository.findById(testProduct.getId()).orElseThrow();
            assertThat(unchangedProduct.getStock()).isEqualTo(10);
        }

        @Test
        @DisplayName("Should reject order when trying to buy exactly one more than stock")
        void shouldRejectOrderWhenExceedingStockByOne() throws Exception {
            OrderDTO order = OrderDTO.builder()
                    .productId(testProduct.getId())
                    .customerName("Edge Case Customer")
                    .quantity(11) // Stock is 10
                    .build();

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(order)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error", is("Out of Stock")));
        }

        @Test
        @DisplayName("Should allow order when quantity equals stock")
        void shouldAllowOrderWhenQuantityEqualsStock() throws Exception {
            OrderDTO order = OrderDTO.builder()
                    .productId(testProduct.getId())
                    .customerName("Max Stock Customer")
                    .quantity(10) // Exactly the stock amount
                    .build();

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(order)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.quantity", is(10)));

            // Verify stock is now 0
            Product depletedProduct = productRepository.findById(testProduct.getId()).orElseThrow();
            assertThat(depletedProduct.getStock()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Order Cancellation Flow")
    class OrderCancellationTests {

        @Test
        @DisplayName("Should restore stock correctly when order is cancelled")
        void shouldRestoreStockWhenOrderCancelled() throws Exception {
            // Create order
            OrderDTO newOrder = OrderDTO.builder()
                    .productId(testProduct.getId())
                    .customerName("Cancel Test Customer")
                    .quantity(5)
                    .build();

            String response = mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newOrder)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            OrderDTO createdOrder = objectMapper.readValue(response, OrderDTO.class);

            // Verify stock decreased
            Product afterOrder = productRepository.findById(testProduct.getId()).orElseThrow();
            assertThat(afterOrder.getStock()).isEqualTo(5); // 10 - 5

            // Cancel order
            mockMvc.perform(delete("/api/orders/" + createdOrder.getId()))
                    .andExpect(status().isNoContent());

            // Verify stock restored
            Product afterCancel = productRepository.findById(testProduct.getId()).orElseThrow();
            assertThat(afterCancel.getStock()).isEqualTo(10); // Back to 10
        }

        @Test
        @DisplayName("Should handle multiple order cancellations correctly")
        void shouldHandleMultipleCancellations() throws Exception {
            // Create 3 orders
            Long orderId1 = createOrderAndGetId(2);
            Long orderId2 = createOrderAndGetId(3);
            Long orderId3 = createOrderAndGetId(1);

            // Stock should be: 10 - 2 - 3 - 1 = 4
            Product afterOrders = productRepository.findById(testProduct.getId()).orElseThrow();
            assertThat(afterOrders.getStock()).isEqualTo(4);

            // Cancel order 2 (quantity: 3)
            mockMvc.perform(delete("/api/orders/" + orderId2))
                    .andExpect(status().isNoContent());

            Product after1Cancel = productRepository.findById(testProduct.getId()).orElseThrow();
            assertThat(after1Cancel.getStock()).isEqualTo(7); // 4 + 3

            // Cancel order 1 (quantity: 2)
            mockMvc.perform(delete("/api/orders/" + orderId1))
                    .andExpect(status().isNoContent());

            Product after2Cancels = productRepository.findById(testProduct.getId()).orElseThrow();
            assertThat(after2Cancels.getStock()).isEqualTo(9); // 7 + 2

            // Cancel order 3 (quantity: 1)
            mockMvc.perform(delete("/api/orders/" + orderId3))
                    .andExpect(status().isNoContent());

            Product finalStock = productRepository.findById(testProduct.getId()).orElseThrow();
            assertThat(finalStock.getStock()).isEqualTo(10); // Back to original: 9 + 1
        }

        private Long createOrderAndGetId(int quantity) throws Exception {
            OrderDTO order = OrderDTO.builder()
                    .productId(testProduct.getId())
                    .customerName("Test Customer")
                    .quantity(quantity)
                    .build();

            String response = mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(order)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            return objectMapper.readValue(response, OrderDTO.class).getId();
        }
    }

    @Nested
    @DisplayName("Order Validation Tests")
    class OrderValidationTests {

        @Test
        @DisplayName("Should reject order with blank customer name")
        void shouldRejectOrderWithBlankCustomerName() throws Exception {
            OrderDTO invalidOrder = OrderDTO.builder()
                    .productId(testProduct.getId())
                    .customerName("")
                    .quantity(1)
                    .build();

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidOrder)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error", is("Validation Error")));
        }

        @Test
        @DisplayName("Should reject order with zero quantity")
        void shouldRejectOrderWithZeroQuantity() throws Exception {
            OrderDTO invalidOrder = OrderDTO.builder()
                    .productId(testProduct.getId())
                    .customerName("Test Customer")
                    .quantity(0)
                    .build();

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidOrder)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error", is("Validation Error")));
        }

        @Test
        @DisplayName("Should reject order with null product ID")
        void shouldRejectOrderWithNullProductId() throws Exception {
            OrderDTO invalidOrder = OrderDTO.builder()
                    .productId(null)
                    .customerName("Test Customer")
                    .quantity(1)
                    .build();

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidOrder)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Order Business Logic Tests")
    class OrderBusinessLogicTests {

        @Test
        @DisplayName("Should calculate total price correctly")
        void shouldCalculateTotalPriceCorrectly() throws Exception {
            // Product price: 999.99, Quantity: 4
            OrderDTO order = OrderDTO.builder()
                    .productId(testProduct.getId())
                    .customerName("Math Test Customer")
                    .quantity(4)
                    .build();

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(order)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.totalPrice", is(3999.96))); // 999.99 * 4
        }

        @Test
        @DisplayName("Should set order date automatically")
        void shouldSetOrderDateAutomatically() throws Exception {
            OrderDTO order = OrderDTO.builder()
                    .productId(testProduct.getId())
                    .customerName("Date Test Customer")
                    .quantity(1)
                    .build();

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(order)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.orderDate").exists())
                    .andExpect(jsonPath("$.orderDate").isNotEmpty());
        }

        @Test
        @DisplayName("Should reject order for non-existent product")
        void shouldRejectOrderForNonExistentProduct() throws Exception {
            OrderDTO order = OrderDTO.builder()
                    .productId(999L)
                    .customerName("Non-Existent Product Customer")
                    .quantity(1)
                    .build();

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(order)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error", is("Not Found")))
                    .andExpect(jsonPath("$.message", containsString("Product not found")));
        }
    }

    @Nested
    @DisplayName("Order List Operations")
    class OrderListTests {

        @Test
        @DisplayName("Should return all orders with correct data")
        void shouldReturnAllOrders() throws Exception {
            // Create multiple orders
            createOrder("Customer A", 2);
            createOrder("Customer B", 1);
            createOrder("Customer C", 3);

            mockMvc.perform(get("/api/orders"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)))
                    .andExpect(jsonPath("$[*].customerName", 
                            containsInAnyOrder("Customer A", "Customer B", "Customer C")))
                    .andExpect(jsonPath("$[*].quantity", 
                            containsInAnyOrder(2, 1, 3)));
        }

        private void createOrder(String customerName, int quantity) throws Exception {
            OrderDTO order = OrderDTO.builder()
                    .productId(testProduct.getId())
                    .customerName(customerName)
                    .quantity(quantity)
                    .build();

            mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(order)));
        }
    }

    @Nested
    @DisplayName("Concurrent Order Scenarios")
    class ConcurrentOrderTests {

        @Test
        @DisplayName("Should handle edge case: last item in stock")
        void shouldHandleLastItemInStock() throws Exception {
            // Reduce stock to 1
            testProduct.setStock(1);
            productRepository.save(testProduct);

            // Order the last item
            OrderDTO order = OrderDTO.builder()
                    .productId(testProduct.getId())
                    .customerName("Last Item Customer")
                    .quantity(1)
                    .build();

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(order)))
                    .andExpect(status().isCreated());

            // Stock should be 0
            Product depletedProduct = productRepository.findById(testProduct.getId()).orElseThrow();
            assertThat(depletedProduct.getStock()).isEqualTo(0);

            // Next order should fail
            OrderDTO nextOrder = OrderDTO.builder()
                    .productId(testProduct.getId())
                    .customerName("Too Late Customer")
                    .quantity(1)
                    .build();

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(nextOrder)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error", is("Out of Stock")));
        }
    }

    @Nested
    @DisplayName("Order Error Handling")
    class OrderErrorHandlingTests {

        @Test
        @DisplayName("Should return proper error when canceling non-existent order")
        void shouldReturnProperErrorWhenCancelingNonExistent() throws Exception {
            mockMvc.perform(delete("/api/orders/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status", is(404)))
                    .andExpect(jsonPath("$.error", is("Not Found")))
                    .andExpect(jsonPath("$.message", containsString("Order not found")))
                    .andExpect(jsonPath("$.path", is("/api/orders/999")));
        }

        @Test
        @DisplayName("Should return proper error when getting non-existent order")
        void shouldReturnProperErrorWhenGettingNonExistent() throws Exception {
            mockMvc.perform(get("/api/orders/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error", is("Not Found")))
                    .andExpect(jsonPath("$.path", is("/api/orders/999")));
        }
    }
}
