package com.demo.productordermanagement.controller;

import com.demo.productordermanagement.dto.ProductDTO;
import com.demo.productordermanagement.exception.ResourceNotFoundException;
import com.demo.productordermanagement.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@DisplayName("ProductController Integration Tests")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @Nested
    @DisplayName("GET /api/products Tests")
    class GetAllProductsTests {

        @Test
        @DisplayName("Should return all products with 200 OK")
        void shouldReturnAllProducts() throws Exception {
            // Given
            ProductDTO product1 = ProductDTO.builder()
                    .id(1L)
                    .name("Laptop")
                    .price(999.99)
                    .stock(10)
                    .build();

            ProductDTO product2 = ProductDTO.builder()
                    .id(2L)
                    .name("Mouse")
                    .price(29.99)
                    .stock(50)
                    .build();

            List<ProductDTO> products = Arrays.asList(product1, product2);
            when(productService.getAllProducts()).thenReturn(products);

            // When & Then
            mockMvc.perform(get("/api/products"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].name", is("Laptop")))
                    .andExpect(jsonPath("$[0].price", is(999.99)))
                    .andExpect(jsonPath("$[1].name", is("Mouse")));

            verify(productService, times(1)).getAllProducts();
        }

        @Test
        @DisplayName("Should return empty array when no products exist")
        void shouldReturnEmptyArrayWhenNoProducts() throws Exception {
            // Given
            when(productService.getAllProducts()).thenReturn(Arrays.asList());

            // When & Then
            mockMvc.perform(get("/api/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/products/{id} Tests")
    class GetProductByIdTests {

        @Test
        @DisplayName("Should return product when ID exists")
        void shouldReturnProductWhenIdExists() throws Exception {
            // Given
            ProductDTO product = ProductDTO.builder()
                    .id(1L)
                    .name("Laptop")
                    .price(999.99)
                    .stock(10)
                    .build();

            when(productService.getProductById(1L)).thenReturn(product);

            // When & Then
            mockMvc.perform(get("/api/products/1"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.name", is("Laptop")))
                    .andExpect(jsonPath("$.price", is(999.99)))
                    .andExpect(jsonPath("$.stock", is(10)));

            verify(productService, times(1)).getProductById(1L);
        }

        @Test
        @DisplayName("Should return 404 when product not found")
        void shouldReturn404WhenProductNotFound() throws Exception {
            // Given
            when(productService.getProductById(999L))
                    .thenThrow(new ResourceNotFoundException("Product not found with id: 999"));

            // When & Then
            mockMvc.perform(get("/api/products/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status", is(404)))
                    .andExpect(jsonPath("$.error", is("Not Found")))
                    .andExpect(jsonPath("$.message", is("Product not found with id: 999")));
        }
    }

    @Nested
    @DisplayName("POST /api/products Tests")
    class CreateProductTests {

        @Test
        @DisplayName("Should create product with valid data and return 201")
        void shouldCreateProductWithValidData() throws Exception {
            // Given
            ProductDTO inputDTO = ProductDTO.builder()
                    .name("Keyboard")
                    .price(49.99)
                    .stock(30)
                    .build();

            ProductDTO savedDTO = ProductDTO.builder()
                    .id(5L)
                    .name("Keyboard")
                    .price(49.99)
                    .stock(30)
                    .build();

            when(productService.createProduct(any(ProductDTO.class))).thenReturn(savedDTO);

            // When & Then
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", is(5)))
                    .andExpect(jsonPath("$.name", is("Keyboard")))
                    .andExpect(jsonPath("$.price", is(49.99)));

            verify(productService, times(1)).createProduct(any(ProductDTO.class));
        }

        @Test
        @DisplayName("Should return 400 when product name is blank")
        void shouldReturn400WhenNameIsBlank() throws Exception {
            // Given
            ProductDTO invalidDTO = ProductDTO.builder()
                    .name("")
                    .price(49.99)
                    .stock(30)
                    .build();

            // When & Then
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error", is("Validation Error")));

            verify(productService, never()).createProduct(any());
        }

        @Test
        @DisplayName("Should return 400 when price is negative")
        void shouldReturn400WhenPriceIsNegative() throws Exception {
            // Given
            ProductDTO invalidDTO = ProductDTO.builder()
                    .name("Keyboard")
                    .price(-10.0)
                    .stock(30)
                    .build();

            // When & Then
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error", is("Validation Error")));
        }
    }

    @Nested
    @DisplayName("PUT /api/products/{id} Tests")
    class UpdateProductTests {

        @Test
        @DisplayName("Should update product successfully")
        void shouldUpdateProductSuccessfully() throws Exception {
            // Given
            ProductDTO updateDTO = ProductDTO.builder()
                    .name("Updated Laptop")
                    .price(1099.99)
                    .stock(15)
                    .build();

            ProductDTO updatedDTO = ProductDTO.builder()
                    .id(1L)
                    .name("Updated Laptop")
                    .price(1099.99)
                    .stock(15)
                    .build();

            when(productService.updateProduct(eq(1L), any(ProductDTO.class))).thenReturn(updatedDTO);

            // When & Then
            mockMvc.perform(put("/api/products/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.name", is("Updated Laptop")))
                    .andExpect(jsonPath("$.price", is(1099.99)));

            verify(productService, times(1)).updateProduct(eq(1L), any(ProductDTO.class));
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent product")
        void shouldReturn404WhenUpdatingNonExistentProduct() throws Exception {
            // Given
            ProductDTO updateDTO = ProductDTO.builder()
                    .name("Updated")
                    .price(100.0)
                    .stock(5)
                    .build();

            when(productService.updateProduct(eq(999L), any(ProductDTO.class)))
                    .thenThrow(new ResourceNotFoundException("Product not found with id: 999"));

            // When & Then
            mockMvc.perform(put("/api/products/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/products/{id} Tests")
    class DeleteProductTests {

        @Test
        @DisplayName("Should delete product successfully")
        void shouldDeleteProductSuccessfully() throws Exception {
            // Given
            doNothing().when(productService).deleteProduct(1L);

            // When & Then
            mockMvc.perform(delete("/api/products/1"))
                    .andExpect(status().isNoContent());

            verify(productService, times(1)).deleteProduct(1L);
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent product")
        void shouldReturn404WhenDeletingNonExistentProduct() throws Exception {
            // Given
            doThrow(new ResourceNotFoundException("Product not found with id: 999"))
                    .when(productService).deleteProduct(999L);

            // When & Then
            mockMvc.perform(delete("/api/products/999"))
                    .andExpect(status().isNotFound());
        }
    }
}
