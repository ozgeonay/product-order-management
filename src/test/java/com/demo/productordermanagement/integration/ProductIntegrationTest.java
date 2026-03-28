package com.demo.productordermanagement.integration;

import com.demo.productordermanagement.dto.ProductDTO;
import com.demo.productordermanagement.entity.Product;
import com.demo.productordermanagement.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Product Integration Tests - Full Stack")
class ProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Nested
    @DisplayName("Product CRUD Flow")
    class ProductCRUDFlowTests {

        @Test
        @DisplayName("Should create, read, update, and delete product in complete flow")
        void shouldPerformCompleteCRUDFlow() throws Exception {
            // 1. CREATE - POST /api/products
            ProductDTO newProduct = ProductDTO.builder()
                    .name("Integration Test Laptop")
                    .price(1499.99)
                    .stock(25)
                    .build();

            String createResponse = mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newProduct)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.name", is("Integration Test Laptop")))
                    .andExpect(jsonPath("$.price", is(1499.99)))
                    .andExpect(jsonPath("$.stock", is(25)))
                    .andReturn().getResponse().getContentAsString();

            ProductDTO createdProduct = objectMapper.readValue(createResponse, ProductDTO.class);
            Long productId = createdProduct.getId();

            // 2. READ - GET /api/products/{id}
            mockMvc.perform(get("/api/products/" + productId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(productId.intValue())))
                    .andExpect(jsonPath("$.name", is("Integration Test Laptop")))
                    .andExpect(jsonPath("$.price", is(1499.99)));

            // 3. UPDATE - PUT /api/products/{id}
            ProductDTO updateProduct = ProductDTO.builder()
                    .name("Updated Integration Laptop")
                    .price(1299.99)
                    .stock(30)
                    .build();

            mockMvc.perform(put("/api/products/" + productId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateProduct)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(productId.intValue())))
                    .andExpect(jsonPath("$.name", is("Updated Integration Laptop")))
                    .andExpect(jsonPath("$.price", is(1299.99)))
                    .andExpect(jsonPath("$.stock", is(30)));

            // 4. DELETE - DELETE /api/products/{id}
            mockMvc.perform(delete("/api/products/" + productId))
                    .andExpect(status().isNoContent());

            // 5. VERIFY DELETION - GET should return 404
            mockMvc.perform(get("/api/products/" + productId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error", is("Not Found")));
        }
    }

    @Nested
    @DisplayName("Product List Operations")
    class ProductListTests {

        @Test
        @DisplayName("Should return empty list when no products exist")
        void shouldReturnEmptyListWhenNoProducts() throws Exception {
            mockMvc.perform(get("/api/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("Should return all products when multiple exist")
        void shouldReturnAllProducts() throws Exception {
            // Given - Create products directly in DB
            Product product1 = new Product();
            product1.setName("Laptop");
            product1.setPrice(999.99);
            product1.setStock(10);
            productRepository.save(product1);

            Product product2 = new Product();
            product2.setName("Mouse");
            product2.setPrice(29.99);
            product2.setStock(50);
            productRepository.save(product2);

            Product product3 = new Product();
            product3.setName("Keyboard");
            product3.setPrice(49.99);
            product3.setStock(30);
            productRepository.save(product3);

            // When & Then
            mockMvc.perform(get("/api/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)))
                    .andExpect(jsonPath("$[*].name", containsInAnyOrder("Laptop", "Mouse", "Keyboard")));
        }
    }

    @Nested
    @DisplayName("Product Validation Tests")
    class ProductValidationTests {

        @Test
        @DisplayName("Should reject product with blank name")
        void shouldRejectProductWithBlankName() throws Exception {
            ProductDTO invalidProduct = ProductDTO.builder()
                    .name("")
                    .price(99.99)
                    .stock(10)
                    .build();

            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidProduct)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error", is("Validation Error")))
                    .andExpect(jsonPath("$.validationErrors", hasSize(greaterThan(0))));
        }

        @Test
        @DisplayName("Should reject product with negative price")
        void shouldRejectProductWithNegativePrice() throws Exception {
            ProductDTO invalidProduct = ProductDTO.builder()
                    .name("Test Product")
                    .price(-50.0)
                    .stock(10)
                    .build();

            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidProduct)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error", is("Validation Error")));
        }

        @Test
        @DisplayName("Should reject product with negative stock")
        void shouldRejectProductWithNegativeStock() throws Exception {
            ProductDTO invalidProduct = ProductDTO.builder()
                    .name("Test Product")
                    .price(99.99)
                    .stock(-5)
                    .build();

            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidProduct)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject product with multiple validation errors")
        void shouldRejectProductWithMultipleErrors() throws Exception {
            ProductDTO invalidProduct = ProductDTO.builder()
                    .name("")
                    .price(-10.0)
                    .stock(-5)
                    .build();

            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidProduct)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.validationErrors", hasSize(3)));
        }
    }

    @Nested
    @DisplayName("Product Error Handling")
    class ProductErrorHandlingTests {

        @Test
        @DisplayName("Should return 404 when getting non-existent product")
        void shouldReturn404ForNonExistentProduct() throws Exception {
            mockMvc.perform(get("/api/products/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status", is(404)))
                    .andExpect(jsonPath("$.error", is("Not Found")))
                    .andExpect(jsonPath("$.message", containsString("Product not found")))
                    .andExpect(jsonPath("$.path", is("/api/products/999")));
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent product")
        void shouldReturn404WhenUpdatingNonExistent() throws Exception {
            ProductDTO updateProduct = ProductDTO.builder()
                    .name("Updated")
                    .price(100.0)
                    .stock(5)
                    .build();

            mockMvc.perform(put("/api/products/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateProduct)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent product")
        void shouldReturn404WhenDeletingNonExistent() throws Exception {
            mockMvc.perform(delete("/api/products/999"))
                    .andExpect(status().isNotFound());
        }
    }
}
