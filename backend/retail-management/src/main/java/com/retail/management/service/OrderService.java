package com.retail.management.service;

import com.retail.management.dto.OrderItemRequest;
import com.retail.management.dto.OrderItemResponse;
import com.retail.management.dto.OrderRequest;
import com.retail.management.dto.OrderResponse;
import com.retail.management.entity.Order;
import com.retail.management.entity.OrderItem;
import com.retail.management.entity.OrderStatus;
import com.retail.management.entity.Product;
import com.retail.management.exception.InsufficientStockException;
import com.retail.management.exception.InvalidOrderStateException;
import com.retail.management.exception.ResourceNotFoundException;
import com.retail.management.repository.OrderRepository;
import com.retail.management.repository.ProductRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(
            OrderRepository orderRepository,
            ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {

        Order order = new Order();

        order.setOrderNumber(
                "ORD-" + UUID.randomUUID().toString().substring(0, 8)
        );

        order.setCustomerName(request.getCustomerName());
        order.setStatus(OrderStatus.PENDING);

        BigDecimal totalAmount = BigDecimal.ZERO;

        List<OrderItemResponse> itemResponses = new ArrayList<>();

        for (OrderItemRequest itemRequest : request.getItems()) {

            Product product = productRepository.findByIdForUpdate(itemRequest.getProductId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Product not found: " + itemRequest.getProductId()
                            )
                    );

            if (!product.getIsActive()) {
                throw new IllegalArgumentException(
                        "Product is inactive: " + product.getName()
                );
            }

            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for product: " + product.getName()
                );
            }

            BigDecimal unitPrice = product.getPrice();

            BigDecimal subtotal = unitPrice.multiply(
                    BigDecimal.valueOf(itemRequest.getQuantity())
            );

            OrderItem orderItem = new OrderItem();

            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(unitPrice);
            orderItem.setSubtotal(subtotal);

            order.addItem(orderItem);

            product.setStockQuantity(
                    product.getStockQuantity() - itemRequest.getQuantity()
            );

            totalAmount = totalAmount.add(subtotal);

            itemResponses.add(
                    new OrderItemResponse(
                            product.getId(),
                            product.getName(),
                            itemRequest.getQuantity(),
                            unitPrice,
                            subtotal
                    )
            );
        }

        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        return new OrderResponse(
                savedOrder.getId(),
                savedOrder.getOrderNumber(),
                savedOrder.getCustomerName(),
                savedOrder.getTotalAmount(),
                savedOrder.getStatus(),
                savedOrder.getCreatedAt(),
                itemResponses
        );
    }
    @Transactional
    public void cancelOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found: " + orderId
                        )
                );

        if (order.getStatus() != OrderStatus.PENDING
                && order.getStatus() != OrderStatus.CONFIRMED) {

            throw new InvalidOrderStateException(
                    "Cannot cancel order with status: " + order.getStatus()
            );
        }

        for (OrderItem item : order.getItems()) {

            Product product = item.getProduct();

            product.setStockQuantity(
                    product.getStockQuantity() + item.getQuantity()
            );
        }

        order.setStatus(OrderStatus.CANCELLED);

        orderRepository.save(order);
    }

    @Transactional
    public void confirmOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found: " + orderId
                        )
                );

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStateException(
                    "Cannot confirm order with status: " + order.getStatus()
            );
        }

        order.setStatus(OrderStatus.CONFIRMED);

        orderRepository.save(order);
    }

    @Transactional
    public void completeOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found: " + orderId
                        )
                );

        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new InvalidOrderStateException(
                    "Cannot complete order with status: " + order.getStatus()
            );
        }

        order.setStatus(OrderStatus.COMPLETED);

        orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {

        return orderRepository.findAll()
                .stream()
                .map(this::mapToOrderResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found: " + id)
                );

        return mapToOrderResponse(order);
    }

    private OrderResponse mapToOrderResponse(Order order) {

        List<OrderItemResponse> items = order.getItems()
                .stream()
                .map(item -> new OrderItemResponse(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerName(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt(),
                items
        );
    }
}