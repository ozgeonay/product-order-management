package com.demo.productordermanagement.service;

import com.demo.productordermanagement.constants.ErrorMessages;
import com.demo.productordermanagement.dto.OrderDTO;
import com.demo.productordermanagement.entity.Order;
import com.demo.productordermanagement.entity.Product;
import com.demo.productordermanagement.exception.OutOfStockException;
import com.demo.productordermanagement.exception.ResourceNotFoundException;
import com.demo.productordermanagement.mapper.OrderMapper;
import com.demo.productordermanagement.repository.OrderRepository;
import com.demo.productordermanagement.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;
    
    public List<OrderDTO> getAllOrders() {
        log.debug("Fetching all orders");
        List<Order> orders = orderRepository.findAll();
        log.debug("Found {} orders", orders.size());
        return orders.stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    public OrderDTO getOrderById(Long id) {
        log.debug("Fetching order with id: {}", id);
        Order order = findOrderByIdOrThrow(id);
        return orderMapper.toDTO(order);
    }
    
    @Transactional
    public OrderDTO createOrder(OrderDTO orderDTO) {
        log.debug("Creating order for customer: {}, product: {}, quantity: {}",
                orderDTO.getCustomerName(), orderDTO.getProductId(), orderDTO.getQuantity());
        
        Product product = findProductByIdOrThrow(orderDTO.getProductId());
        
        validateStockAvailability(product, orderDTO.getQuantity());
        
        Order order = orderMapper.toEntity(orderDTO);
        order.setTotalPrice(calculateTotalPrice(product.getPrice(), orderDTO.getQuantity()));
        order.setOrderDate(LocalDateTime.now());
        
        decreaseStock(product, orderDTO.getQuantity());
        productRepository.save(product);
        
        Order savedOrder = orderRepository.save(order);
        log.info("Created order with id: {} for product: {}", savedOrder.getId(), product.getName());
        
        return orderMapper.toDTO(savedOrder);
    }
    
    @Transactional
    public void cancelOrder(Long id) {
        log.debug("Cancelling order with id: {}", id);
        Order order = findOrderByIdOrThrow(id);
        
        Product product = findProductByIdOrThrow(order.getProductId());
        
        increaseStock(product, order.getQuantity());
        productRepository.save(product);
        
        orderRepository.delete(order);
        log.info("Cancelled order with id: {} and restored stock for product: {}", id, product.getName());
    }
    
    private Order findOrderByIdOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> {
                    String errorMessage = String.format(ErrorMessages.ORDER_NOT_FOUND, id);
                    log.error(errorMessage);
                    return new ResourceNotFoundException(errorMessage);
                });
    }
    
    private Product findProductByIdOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> {
                    String errorMessage = String.format(ErrorMessages.PRODUCT_NOT_FOUND, id);
                    log.error(errorMessage);
                    return new ResourceNotFoundException(errorMessage);
                });
    }
    
    private Double calculateTotalPrice(Double unitPrice, Integer quantity) {
        return unitPrice * quantity;
    }
    
    private void validateStockAvailability(Product product, Integer requestedQuantity) {
        log.debug("Validating stock availability for product: {} (Available: {}, Requested: {})",
                product.getName(), product.getStock(), requestedQuantity);
        
        if (product.getStock() < requestedQuantity) {
            String errorMessage = String.format(
                    ErrorMessages.OUT_OF_STOCK,
                    product.getName(),
                    product.getStock(),
                    requestedQuantity
            );
            log.warn("Stock validation failed: {}", errorMessage);
            throw new OutOfStockException(errorMessage);
        }
    }
    
    private void decreaseStock(Product product, Integer quantity) {
        log.debug("Decreasing stock for product: {} by {}", product.getName(), quantity);
        product.setStock(product.getStock() - quantity);
        log.debug("New stock level for product {}: {}", product.getName(), product.getStock());
    }
    
    private void increaseStock(Product product, Integer quantity) {
        log.debug("Increasing stock for product: {} by {}", product.getName(), quantity);
        product.setStock(product.getStock() + quantity);
        log.debug("New stock level for product {}: {}", product.getName(), product.getStock());
    }
}
