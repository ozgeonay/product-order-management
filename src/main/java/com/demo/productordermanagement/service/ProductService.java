package com.demo.productordermanagement.service;

import com.demo.productordermanagement.constants.ErrorMessages;
import com.demo.productordermanagement.dto.ProductDTO;
import com.demo.productordermanagement.entity.Product;
import com.demo.productordermanagement.exception.ResourceNotFoundException;
import com.demo.productordermanagement.mapper.ProductMapper;
import com.demo.productordermanagement.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductService {
    
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    
    public List<ProductDTO> getAllProducts() {
        log.debug("Fetching all products");
        List<Product> products = productRepository.findAll();
        log.debug("Found {} products", products.size());
        return products.stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    public ProductDTO getProductById(Long id) {
        log.debug("Fetching product with id: {}", id);
        Product product = findProductByIdOrThrow(id);
        return productMapper.toDTO(product);
    }
    
    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        log.debug("Creating new product: {}", productDTO.getName());
        Product product = productMapper.toEntity(productDTO);
        Product savedProduct = productRepository.save(product);
        log.info("Created product with id: {}", savedProduct.getId());
        return productMapper.toDTO(savedProduct);
    }
    
    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        log.debug("Updating product with id: {}", id);
        Product product = findProductByIdOrThrow(id);
        productMapper.updateEntityFromDTO(productDTO, product);
        Product updatedProduct = productRepository.save(product);
        log.info("Updated product with id: {}", id);
        return productMapper.toDTO(updatedProduct);
    }
    
    @Transactional
    public void deleteProduct(Long id) {
        log.debug("Deleting product with id: {}", id);
        Product product = findProductByIdOrThrow(id);
        productRepository.delete(product);
        log.info("Deleted product with id: {}", id);
    }
    
    private Product findProductByIdOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> {
                    String errorMessage = String.format(ErrorMessages.PRODUCT_NOT_FOUND, id);
                    log.error(errorMessage);
                    return new ResourceNotFoundException(errorMessage);
                });
    }
}
