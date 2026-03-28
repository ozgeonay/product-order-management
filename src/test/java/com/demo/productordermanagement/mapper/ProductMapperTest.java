package com.demo.productordermanagement.mapper;

import com.demo.productordermanagement.dto.ProductDTO;
import com.demo.productordermanagement.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("ProductMapper Unit Tests")
class ProductMapperTest {

    private ProductMapper productMapper;

    @BeforeEach
    void setUp() {
        productMapper = new ProductMapper();
    }

    @Test
    @DisplayName("Should map Product entity to ProductDTO successfully")
    void shouldMapProductToDTO() {
        // Given
        Product product = new Product();
        product.setId(1L);
        product.setName("Laptop");
        product.setPrice(999.99);
        product.setStock(10);

        // When
        ProductDTO result = productMapper.toDTO(product);

        // Then
        assertAll("ProductDTO should have all fields mapped correctly",
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getId()).isEqualTo(1L),
                () -> assertThat(result.getName()).isEqualTo("Laptop"),
                () -> assertThat(result.getPrice()).isEqualTo(999.99),
                () -> assertThat(result.getStock()).isEqualTo(10)
        );
    }

    @Test
    @DisplayName("Should return null when Product entity is null")
    void shouldReturnNullWhenProductIsNull() {
        // When
        ProductDTO result = productMapper.toDTO(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should map ProductDTO to Product entity successfully")
    void shouldMapDTOToProduct() {
        // Given
        ProductDTO dto = ProductDTO.builder()
                .id(1L)
                .name("Mouse")
                .price(29.99)
                .stock(50)
                .build();

        // When
        Product result = productMapper.toEntity(dto);

        // Then
        assertAll("Product entity should have all fields mapped correctly",
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getId()).isEqualTo(1L),
                () -> assertThat(result.getName()).isEqualTo("Mouse"),
                () -> assertThat(result.getPrice()).isEqualTo(29.99),
                () -> assertThat(result.getStock()).isEqualTo(50)
        );
    }

    @Test
    @DisplayName("Should return null when ProductDTO is null")
    void shouldReturnNullWhenDTOIsNull() {
        // When
        Product result = productMapper.toEntity(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should update entity from DTO successfully")
    void shouldUpdateEntityFromDTO() {
        // Given
        Product existingProduct = new Product();
        existingProduct.setId(1L);
        existingProduct.setName("Old Name");
        existingProduct.setPrice(100.0);
        existingProduct.setStock(5);

        ProductDTO updateDTO = ProductDTO.builder()
                .name("Updated Name")
                .price(150.0)
                .stock(20)
                .build();

        // When
        productMapper.updateEntityFromDTO(updateDTO, existingProduct);

        // Then
        assertAll("Product should be updated with DTO values",
                () -> assertThat(existingProduct.getId()).isEqualTo(1L), // ID should not change
                () -> assertThat(existingProduct.getName()).isEqualTo("Updated Name"),
                () -> assertThat(existingProduct.getPrice()).isEqualTo(150.0),
                () -> assertThat(existingProduct.getStock()).isEqualTo(20)
        );
    }

    @Test
    @DisplayName("Should handle null values gracefully when updating")
    void shouldHandleNullValuesWhenUpdating() {
        // Given
        Product product = new Product();
        product.setId(1L);

        // When & Then - should not throw exception
        productMapper.updateEntityFromDTO(null, product);
        productMapper.updateEntityFromDTO(ProductDTO.builder().build(), null);
    }
}
