package com.demo.productordermanagement.mapper;

import com.demo.productordermanagement.dto.ProductDTO;
import com.demo.productordermanagement.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    
    public ProductDTO toDTO(Product product) {
        if (product == null) {
            return null;
        }
        
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stock(product.getStock())
                .build();
    }
    
    public Product toEntity(ProductDTO productDTO) {
        if (productDTO == null) {
            return null;
        }
        
        Product product = new Product();
        product.setId(productDTO.getId());
        product.setName(productDTO.getName());
        product.setPrice(productDTO.getPrice());
        product.setStock(productDTO.getStock());
        
        return product;
    }
    
    public void updateEntityFromDTO(ProductDTO productDTO, Product product) {
        if (productDTO == null || product == null) {
            return;
        }
        
        product.setName(productDTO.getName());
        product.setPrice(productDTO.getPrice());
        product.setStock(productDTO.getStock());
    }
}
