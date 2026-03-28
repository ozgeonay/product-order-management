package com.demo.productordermanagement.service;

import com.demo.productordermanagement.dto.ProductDTO;
import com.demo.productordermanagement.entity.Product;
import com.demo.productordermanagement.exception.ResourceNotFoundException;
import com.demo.productordermanagement.mapper.ProductMapper;
import com.demo.productordermanagement.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private ProductDTO testProductDTO;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Laptop");
        testProduct.setPrice(999.99);
        testProduct.setStock(10);

        testProductDTO = ProductDTO.builder()
                .id(1L)
                .name("Laptop")
                .price(999.99)
                .stock(10)
                .build();
    }

    @Nested
    @DisplayName("getAllProducts() Tests")
    class GetAllProductsTests {

        @Test
        @DisplayName("Should return all products successfully")
        void shouldReturnAllProducts() {
            // Given
            Product product2 = new Product();
            product2.setId(2L);
            product2.setName("Mouse");
            product2.setPrice(29.99);
            product2.setStock(50);

            List<Product> products = Arrays.asList(testProduct, product2);

            ProductDTO productDTO2 = ProductDTO.builder()
                    .id(2L)
                    .name("Mouse")
                    .price(29.99)
                    .stock(50)
                    .build();

            when(productRepository.findAll()).thenReturn(products);
            when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);
            when(productMapper.toDTO(product2)).thenReturn(productDTO2);

            // When
            List<ProductDTO> result = productService.getAllProducts();

            // Then
            assertAll(
                    () -> assertThat(result).hasSize(2),
                    () -> assertThat(result).extracting(ProductDTO::getName)
                            .containsExactly("Laptop", "Mouse")
            );
            verify(productRepository, times(1)).findAll();
            verify(productMapper, times(2)).toDTO(any(Product.class));
        }

        @Test
        @DisplayName("Should return empty list when no products exist")
        void shouldReturnEmptyListWhenNoProducts() {
            // Given
            when(productRepository.findAll()).thenReturn(Arrays.asList());

            // When
            List<ProductDTO> result = productService.getAllProducts();

            // Then
            assertThat(result).isEmpty();
            verify(productRepository, times(1)).findAll();
        }
    }

    @Nested
    @DisplayName("getProductById() Tests")
    class GetProductByIdTests {

        @Test
        @DisplayName("Should return product when ID exists")
        void shouldReturnProductWhenIdExists() {
            // Given
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

            // When
            ProductDTO result = productService.getProductById(1L);

            // Then
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.getId()).isEqualTo(1L),
                    () -> assertThat(result.getName()).isEqualTo("Laptop")
            );
            verify(productRepository, times(1)).findById(1L);
            verify(productMapper, times(1)).toDTO(testProduct);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when product not found")
        void shouldThrowExceptionWhenProductNotFound() {
            // Given
            when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> productService.getProductById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Product not found with id: 999");

            verify(productRepository, times(1)).findById(999L);
            verify(productMapper, never()).toDTO(any());
        }
    }

    @Nested
    @DisplayName("createProduct() Tests")
    class CreateProductTests {

        @Test
        @DisplayName("Should create product successfully")
        void shouldCreateProductSuccessfully() {
            // Given
            ProductDTO newProductDTO = ProductDTO.builder()
                    .name("Keyboard")
                    .price(49.99)
                    .stock(30)
                    .build();

            Product newProduct = new Product();
            newProduct.setName("Keyboard");
            newProduct.setPrice(49.99);
            newProduct.setStock(30);

            Product savedProduct = new Product();
            savedProduct.setId(3L);
            savedProduct.setName("Keyboard");
            savedProduct.setPrice(49.99);
            savedProduct.setStock(30);

            ProductDTO savedProductDTO = ProductDTO.builder()
                    .id(3L)
                    .name("Keyboard")
                    .price(49.99)
                    .stock(30)
                    .build();

            when(productMapper.toEntity(newProductDTO)).thenReturn(newProduct);
            when(productRepository.save(newProduct)).thenReturn(savedProduct);
            when(productMapper.toDTO(savedProduct)).thenReturn(savedProductDTO);

            // When
            ProductDTO result = productService.createProduct(newProductDTO);

            // Then
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.getId()).isEqualTo(3L),
                    () -> assertThat(result.getName()).isEqualTo("Keyboard"),
                    () -> assertThat(result.getPrice()).isEqualTo(49.99)
            );
            verify(productMapper, times(1)).toEntity(newProductDTO);
            verify(productRepository, times(1)).save(newProduct);
            verify(productMapper, times(1)).toDTO(savedProduct);
        }
    }

    @Nested
    @DisplayName("updateProduct() Tests")
    class UpdateProductTests {

        @Test
        @DisplayName("Should update product successfully")
        void shouldUpdateProductSuccessfully() {
            // Given
            ProductDTO updateDTO = ProductDTO.builder()
                    .name("Updated Laptop")
                    .price(1099.99)
                    .stock(15)
                    .build();

            Product updatedProduct = new Product();
            updatedProduct.setId(1L);
            updatedProduct.setName("Updated Laptop");
            updatedProduct.setPrice(1099.99);
            updatedProduct.setStock(15);

            ProductDTO updatedProductDTO = ProductDTO.builder()
                    .id(1L)
                    .name("Updated Laptop")
                    .price(1099.99)
                    .stock(15)
                    .build();

            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            doNothing().when(productMapper).updateEntityFromDTO(updateDTO, testProduct);
            when(productRepository.save(testProduct)).thenReturn(updatedProduct);
            when(productMapper.toDTO(updatedProduct)).thenReturn(updatedProductDTO);

            // When
            ProductDTO result = productService.updateProduct(1L, updateDTO);

            // Then
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.getName()).isEqualTo("Updated Laptop"),
                    () -> assertThat(result.getPrice()).isEqualTo(1099.99)
            );
            verify(productRepository, times(1)).findById(1L);
            verify(productMapper, times(1)).updateEntityFromDTO(updateDTO, testProduct);
            verify(productRepository, times(1)).save(testProduct);
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent product")
        void shouldThrowExceptionWhenUpdatingNonExistentProduct() {
            // Given
            ProductDTO updateDTO = ProductDTO.builder()
                    .name("Updated")
                    .price(100.0)
                    .stock(5)
                    .build();

            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> productService.updateProduct(999L, updateDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Product not found with id: 999");

            verify(productRepository, times(1)).findById(999L);
            verify(productRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteProduct() Tests")
    class DeleteProductTests {

        @Test
        @DisplayName("Should delete product successfully")
        void shouldDeleteProductSuccessfully() {
            // Given
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            doNothing().when(productRepository).delete(testProduct);

            // When
            productService.deleteProduct(1L);

            // Then
            verify(productRepository, times(1)).findById(1L);
            verify(productRepository, times(1)).delete(testProduct);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent product")
        void shouldThrowExceptionWhenDeletingNonExistentProduct() {
            // Given
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> productService.deleteProduct(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Product not found with id: 999");

            verify(productRepository, times(1)).findById(999L);
            verify(productRepository, never()).delete(any());
        }
    }
}
