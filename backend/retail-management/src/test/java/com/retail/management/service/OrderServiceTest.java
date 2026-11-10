package com.retail.management.service;

import com.retail.management.dto.OrderItemRequest;
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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_ShouldReturnOrder_WhenValidRequest() {

        // Arrange
        Product product = new Product();
        product.setId(1L);
        product.setName("Laptop Dell");
        product.setPrice(new BigDecimal("1500.00"));
        product.setStockQuantity(10);
        product.setIsActive(true);

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(2);

        OrderRequest request = new OrderRequest();
        request.setCustomerName("Nguyen Van A");
        request.setItems(List.of(itemRequest));

        when(productRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(product));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> {
                    Order order = invocation.getArgument(0);
                    order.setId(1L);
                    return order;
                });

        // Act
        OrderResponse response = orderService.createOrder(request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Nguyen Van A", response.getCustomerName());
        assertEquals(OrderStatus.PENDING, response.getStatus());

        assertEquals(
                new BigDecimal("3000.00"),
                response.getTotalAmount()
        );

        assertEquals(8, product.getStockQuantity());

        verify(orderRepository, times(1)).save(any(Order.class));
    }
    @Test
    void createOrder_ShouldThrowException_WhenProductNotFound() {

        // Arrange
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(999L);
        itemRequest.setQuantity(2);

        OrderRequest request = new OrderRequest();
        request.setCustomerName("Nguyen Van A");
        request.setItems(List.of(itemRequest));

        when(productRepository.findByIdForUpdate(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.createOrder(request)
        );

        assertEquals("Product not found: 999", exception.getMessage());

        verify(orderRepository, never()).save(any(Order.class));
    }
    @Test
    void createOrder_ShouldThrowException_WhenStockInsufficient() {

        // Arrange
        Product product = new Product();
        product.setId(1L);
        product.setName("Laptop Dell");
        product.setPrice(new BigDecimal("1500.00"));
        product.setStockQuantity(5);
        product.setIsActive(true);

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(10);

        OrderRequest request = new OrderRequest();
        request.setCustomerName("Nguyen Van A");
        request.setItems(List.of(itemRequest));

        when(productRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(product));

        // Act & Assert
        assertThrows(
                InsufficientStockException.class,
                () -> orderService.createOrder(request)
        );

        assertEquals(5, product.getStockQuantity());

        verify(orderRepository, never()).save(any(Order.class));
    }
    @Test
    void createOrder_ShouldThrowException_WhenProductInactive() {

        // Arrange
        Product product = new Product();
        product.setId(1L);
        product.setName("Laptop Dell");
        product.setPrice(new BigDecimal("1500.00"));
        product.setStockQuantity(10);
        product.setIsActive(false);

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(2);

        OrderRequest request = new OrderRequest();
        request.setCustomerName("Nguyen Van A");
        request.setItems(List.of(itemRequest));

        when(productRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(product));

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrder(request)
        );

        assertEquals(10, product.getStockQuantity());

        verify(orderRepository, never()).save(any(Order.class));
    }
    @Test
    void cancelOrder_ShouldRestoreStock_WhenOrderIsPending() {

        // Arrange
        Product product = new Product();
        product.setId(1L);
        product.setName("Laptop Dell");
        product.setStockQuantity(8);
        product.setIsActive(true);

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(2);

        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.PENDING);
        order.addItem(item);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        // Act
        orderService.cancelOrder(1L);

        // Assert
        assertEquals(10, product.getStockQuantity());
        assertEquals(OrderStatus.CANCELLED, order.getStatus());

        verify(orderRepository).save(order);
    }

    @Test
    void cancelOrder_ShouldThrowException_WhenOrderNotFound() {

        // Arrange
        when(orderRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.cancelOrder(999L)
        );

        assertEquals("Order not found: 999", exception.getMessage());

        verify(orderRepository, never()).save(any(Order.class));
    }
    @Test
    void cancelOrder_ShouldThrowException_WhenOrderAlreadyCancelled() {

        // Arrange
        Product product = new Product();
        product.setId(1L);
        product.setStockQuantity(10);

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(2);

        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.CANCELLED);
        order.addItem(item);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(
                InvalidOrderStateException.class,
                () -> orderService.cancelOrder(1L)
        );

        // Stock must remain unchanged
        assertEquals(10, product.getStockQuantity());

        verify(orderRepository, never()).save(any(Order.class));
    }
    @Test
    void getAllOrders_ShouldReturnOrderList() {

        // Arrange
        Order order = new Order();
        order.setId(1L);
        order.setOrderNumber("ORD-12345678");
        order.setCustomerName("Nguyen Van A");
        order.setTotalAmount(new BigDecimal("3000.00"));
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findAll())
                .thenReturn(List.of(order));

        // Act
        List<OrderResponse> result = orderService.getAllOrders();

        // Assert
        assertEquals(1, result.size());
        assertEquals("ORD-12345678", result.get(0).getOrderNumber());
        assertEquals("Nguyen Van A", result.get(0).getCustomerName());
        assertEquals(OrderStatus.PENDING, result.get(0).getStatus());

        verify(orderRepository).findAll();
    }
    @Test
    void getOrderById_ShouldReturnOrder_WhenOrderExists() {

        // Arrange
        Order order = new Order();
        order.setId(1L);
        order.setOrderNumber("ORD-12345678");
        order.setCustomerName("Nguyen Van A");
        order.setTotalAmount(new BigDecimal("3000.00"));
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        // Act
        OrderResponse result = orderService.getOrderById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("ORD-12345678", result.getOrderNumber());
        assertEquals("Nguyen Van A", result.getCustomerName());
        assertEquals(new BigDecimal("3000.00"), result.getTotalAmount());
        assertEquals(OrderStatus.PENDING, result.getStatus());

        verify(orderRepository).findById(1L);
    }
    @Test
    void getOrderById_ShouldThrowException_WhenOrderNotFound() {

        // Arrange
        when(orderRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.getOrderById(999L)
        );

        assertEquals("Order not found: 999", exception.getMessage());

        verify(orderRepository).findById(999L);
    }
    @Test
    void confirmOrder_ShouldChangeStatusToConfirmed_WhenOrderIsPending() {

        // Arrange
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        // Act
        orderService.confirmOrder(1L);

        // Assert
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());

        verify(orderRepository).save(order);
    }
    @Test
    void confirmOrder_ShouldThrowException_WhenOrderAlreadyCompleted() {

        // Arrange
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.COMPLETED);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(
                InvalidOrderStateException.class,
                () -> orderService.confirmOrder(1L)
        );

        verify(orderRepository, never()).save(any(Order.class));
    }
    @Test
    void confirmOrder_ShouldThrowException_WhenOrderNotFound() {

        // Arrange
        when(orderRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.confirmOrder(999L)
        );

        assertEquals("Order not found: 999", exception.getMessage());

        verify(orderRepository, never()).save(any(Order.class));
    }
    @Test
    void completeOrder_ShouldChangeStatusToCompleted_WhenOrderIsConfirmed() {

        // Arrange
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.CONFIRMED);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        // Act
        orderService.completeOrder(1L);

        // Assert
        assertEquals(OrderStatus.COMPLETED, order.getStatus());

        verify(orderRepository).save(order);
    }
    @Test
    void completeOrder_ShouldThrowException_WhenOrderIsPending() {

        // Arrange
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(
                InvalidOrderStateException.class,
                () -> orderService.completeOrder(1L)
        );

        verify(orderRepository, never()).save(any(Order.class));
    }
    @Test
    void completeOrder_ShouldThrowException_WhenOrderNotFound() {

        // Arrange
        when(orderRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.completeOrder(999L)
        );

        assertEquals("Order not found: 999", exception.getMessage());

        verify(orderRepository, never()).save(any(Order.class));
    }
}