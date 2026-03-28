package com.demo.productordermanagement.service;

import com.demo.productordermanagement.dto.OrderDTO;
import com.demo.productordermanagement.entity.Order;
import com.demo.productordermanagement.entity.Product;
import com.demo.productordermanagement.exception.OutOfStockException;
import com.demo.productordermanagement.exception.ResourceNotFoundException;
import com.demo.productordermanagement.mapper.OrderMapper;
import com.demo.productordermanagement.repository.OrderRepository;
import com.demo.productordermanagement.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;
    private OrderDTO testOrderDTO;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Laptop");
        testProduct.setPrice(999.99);
        testProduct.setStock(10);

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setProductId(1L);
        testOrder.setCustomerName("John Doe");
        testOrder.setQuantity(2);
        testOrder.setTotalPrice(1999.98);
        testOrder.setOrderDate(LocalDateTime.now());

        testOrderDTO = OrderDTO.builder()
                .id(1L)
                .productId(1L)
                .customerName("John Doe")
                .quantity(2)
                .totalPrice(1999.98)
                .orderDate(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("getAllOrders() Tests")
    class GetAllOrdersTests {

        @Test
        @DisplayName("Should return all orders successfully")
        void shouldReturnAllOrders() {
            // Given
            Order order2 = new Order();
            order2.setId(2L);
            order2.setProductId(2L);
            order2.setCustomerName("Jane Smith");
            order2.setQuantity(1);

            OrderDTO orderDTO2 = OrderDTO.builder()
                    .id(2L)
                    .productId(2L)
                    .customerName("Jane Smith")
                    .quantity(1)
                    .build();

            List<Order> orders = Arrays.asList(testOrder, order2);

            when(orderRepository.findAll()).thenReturn(orders);
            when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);
            when(orderMapper.toDTO(order2)).thenReturn(orderDTO2);

            // When
            List<OrderDTO> result = orderService.getAllOrders();

            // Then
            assertAll(
                    () -> assertThat(result).hasSize(2),
                    () -> assertThat(result).extracting(OrderDTO::getCustomerName)
                            .containsExactly("John Doe", "Jane Smith")
            );
            verify(orderRepository, times(1)).findAll();
            verify(orderMapper, times(2)).toDTO(any(Order.class));
        }
    }

    @Nested
    @DisplayName("getOrderById() Tests")
    class GetOrderByIdTests {

        @Test
        @DisplayName("Should return order when ID exists")
        void shouldReturnOrderWhenIdExists() {
            // Given
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

            // When
            OrderDTO result = orderService.getOrderById(1L);

            // Then
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.getId()).isEqualTo(1L),
                    () -> assertThat(result.getCustomerName()).isEqualTo("John Doe")
            );
            verify(orderRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Should throw exception when order not found")
        void shouldThrowExceptionWhenOrderNotFound() {
            // Given
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> orderService.getOrderById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Order not found with id: 999");
        }
    }

    @Nested
    @DisplayName("createOrder() Tests")
    class CreateOrderTests {

        @Test
        @DisplayName("Should create order successfully when stock is sufficient")
        void shouldCreateOrderSuccessfully() {
            // Given
            OrderDTO newOrderDTO = OrderDTO.builder()
                    .productId(1L)
                    .customerName("Alice Johnson")
                    .quantity(2)
                    .build();

            Order newOrder = new Order();
            newOrder.setProductId(1L);
            newOrder.setCustomerName("Alice Johnson");
            newOrder.setQuantity(2);

            Order savedOrder = new Order();
            savedOrder.setId(5L);
            savedOrder.setProductId(1L);
            savedOrder.setCustomerName("Alice Johnson");
            savedOrder.setQuantity(2);
            savedOrder.setTotalPrice(1999.98);
            savedOrder.setOrderDate(LocalDateTime.now());

            OrderDTO savedOrderDTO = OrderDTO.builder()
                    .id(5L)
                    .productId(1L)
                    .customerName("Alice Johnson")
                    .quantity(2)
                    .totalPrice(1999.98)
                    .build();

            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(orderMapper.toEntity(newOrderDTO)).thenReturn(newOrder);
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
            when(orderMapper.toDTO(savedOrder)).thenReturn(savedOrderDTO);

            // When
            OrderDTO result = orderService.createOrder(newOrderDTO);

            // Then
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.getId()).isEqualTo(5L),
                    () -> assertThat(result.getCustomerName()).isEqualTo("Alice Johnson"),
                    () -> assertThat(testProduct.getStock()).isEqualTo(8) // 10 - 2
            );
            verify(productRepository, times(1)).save(testProduct);
            verify(orderRepository, times(1)).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw OutOfStockException when insufficient stock")
        void shouldThrowExceptionWhenInsufficientStock() {
            // Given
            OrderDTO newOrderDTO = OrderDTO.builder()
                    .productId(1L)
                    .customerName("Bob Wilson")
                    .quantity(100) // More than available stock
                    .build();

            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

            // When & Then
            assertThatThrownBy(() -> orderService.createOrder(newOrderDTO))
                    .isInstanceOf(OutOfStockException.class)
                    .hasMessageContaining("Not enough stock")
                    .hasMessageContaining("Laptop")
                    .hasMessageContaining("Available: 10")
                    .hasMessageContaining("Requested: 100");

            verify(productRepository, never()).save(any());
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void shouldThrowExceptionWhenProductNotFound() {
            // Given
            OrderDTO newOrderDTO = OrderDTO.builder()
                    .productId(999L)
                    .customerName("Carol White")
                    .quantity(1)
                    .build();

            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> orderService.createOrder(newOrderDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Product not found with id: 999");

            verify(orderRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("cancelOrder() Tests")
    class CancelOrderTests {

        @Test
        @DisplayName("Should cancel order and restore stock successfully")
        void shouldCancelOrderAndRestoreStock() {
            // Given
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            doNothing().when(orderRepository).delete(testOrder);

            int initialStock = testProduct.getStock();

            // When
            orderService.cancelOrder(1L);

            // Then
            assertThat(testProduct.getStock()).isEqualTo(initialStock + 2); // Restored 2 units
            verify(orderRepository, times(1)).findById(1L);
            verify(productRepository, times(1)).findById(1L);
            verify(productRepository, times(1)).save(testProduct);
            verify(orderRepository, times(1)).delete(testOrder);
        }

        @Test
        @DisplayName("Should throw exception when canceling non-existent order")
        void shouldThrowExceptionWhenCancelingNonExistentOrder() {
            // Given
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> orderService.cancelOrder(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Order not found with id: 999");

            verify(orderRepository, never()).delete(any());
            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when product not found during cancellation")
        void shouldThrowExceptionWhenProductNotFoundDuringCancellation() {
            // Given
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(productRepository.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> orderService.cancelOrder(1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Product not found with id: 1");

            verify(orderRepository, never()).delete(any());
        }
    }
}
