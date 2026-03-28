package com.demo.productordermanagement.mapper;

import com.demo.productordermanagement.dto.OrderDTO;
import com.demo.productordermanagement.entity.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("OrderMapper Unit Tests")
class OrderMapperTest {

    private OrderMapper orderMapper;

    @BeforeEach
    void setUp() {
        orderMapper = new OrderMapper();
    }

    @Test
    @DisplayName("Should map Order entity to OrderDTO successfully")
    void shouldMapOrderToDTO() {
        // Given
        LocalDateTime orderDate = LocalDateTime.of(2024, 1, 15, 10, 30);
        Order order = new Order();
        order.setId(1L);
        order.setProductId(10L);
        order.setCustomerName("John Doe");
        order.setQuantity(2);
        order.setTotalPrice(1999.98);
        order.setOrderDate(orderDate);

        // When
        OrderDTO result = orderMapper.toDTO(order);

        // Then
        assertAll("OrderDTO should have all fields mapped correctly",
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getId()).isEqualTo(1L),
                () -> assertThat(result.getProductId()).isEqualTo(10L),
                () -> assertThat(result.getCustomerName()).isEqualTo("John Doe"),
                () -> assertThat(result.getQuantity()).isEqualTo(2),
                () -> assertThat(result.getTotalPrice()).isEqualTo(1999.98),
                () -> assertThat(result.getOrderDate()).isEqualTo(orderDate)
        );
    }

    @Test
    @DisplayName("Should return null when Order entity is null")
    void shouldReturnNullWhenOrderIsNull() {
        // When
        OrderDTO result = orderMapper.toDTO(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should map OrderDTO to Order entity successfully")
    void shouldMapDTOToOrder() {
        // Given
        LocalDateTime orderDate = LocalDateTime.of(2024, 1, 15, 14, 45);
        OrderDTO dto = OrderDTO.builder()
                .id(1L)
                .productId(5L)
                .customerName("Jane Smith")
                .quantity(3)
                .totalPrice(299.97)
                .orderDate(orderDate)
                .build();

        // When
        Order result = orderMapper.toEntity(dto);

        // Then
        assertAll("Order entity should have all fields mapped correctly",
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getId()).isEqualTo(1L),
                () -> assertThat(result.getProductId()).isEqualTo(5L),
                () -> assertThat(result.getCustomerName()).isEqualTo("Jane Smith"),
                () -> assertThat(result.getQuantity()).isEqualTo(3),
                () -> assertThat(result.getTotalPrice()).isEqualTo(299.97),
                () -> assertThat(result.getOrderDate()).isEqualTo(orderDate)
        );
    }

    @Test
    @DisplayName("Should return null when OrderDTO is null")
    void shouldReturnNullWhenDTOIsNull() {
        // When
        Order result = orderMapper.toEntity(null);

        // Then
        assertThat(result).isNull();
    }
}
