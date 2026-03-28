package com.demo.productordermanagement.mapper;

import com.demo.productordermanagement.dto.OrderDTO;
import com.demo.productordermanagement.entity.Order;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {
    
    public OrderDTO toDTO(Order order) {
        if (order == null) {
            return null;
        }
        
        return OrderDTO.builder()
                .id(order.getId())
                .productId(order.getProductId())
                .customerName(order.getCustomerName())
                .quantity(order.getQuantity())
                .totalPrice(order.getTotalPrice())
                .orderDate(order.getOrderDate())
                .build();
    }
    
    public Order toEntity(OrderDTO orderDTO) {
        if (orderDTO == null) {
            return null;
        }
        
        Order order = new Order();
        order.setId(orderDTO.getId());
        order.setProductId(orderDTO.getProductId());
        order.setCustomerName(orderDTO.getCustomerName());
        order.setQuantity(orderDTO.getQuantity());
        order.setTotalPrice(orderDTO.getTotalPrice());
        order.setOrderDate(orderDTO.getOrderDate());
        
        return order;
    }
}
