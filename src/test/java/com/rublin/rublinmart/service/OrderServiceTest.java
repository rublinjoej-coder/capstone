package com.rublin.rublinmart.service;

import com.rublin.rublinmart.dao.CartDAO;
import com.rublin.rublinmart.dao.OrderDAO;
import com.rublin.rublinmart.dao.ProductDAO;
import com.rublin.rublinmart.dto.OrderResponseDTO;
import com.rublin.rublinmart.exception.AppException;
import com.rublin.rublinmart.model.CartItem;
import com.rublin.rublinmart.model.Order;
import com.rublin.rublinmart.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class OrderServiceTest {

    private OrderDAO orderDAO;
    private CartDAO cartDAO;
    private ProductDAO productDAO;
    private OrderService orderService;

    @BeforeEach
    public void setUp() {
        orderDAO = mock(OrderDAO.class);
        cartDAO = mock(CartDAO.class);
        productDAO = mock(ProductDAO.class);
        orderService = new OrderService(orderDAO, cartDAO, productDAO);
    }

    @Test
    public void testPlaceOrderSuccess() {
        CartItem cartItem = new CartItem(1L, 4L, 10L, 2);
        when(cartDAO.getCartItems(4L)).thenReturn(List.of(cartItem));

        Product product = new Product(10L, 2L, "Desk Chair", "Comfortable", BigDecimal.valueOf(150.00), 10, "Furniture", "img", null);
        when(productDAO.findById(10L)).thenReturn(Optional.of(product));

        Order createdOrder = new Order(100L, 4L, "CONFIRMED", BigDecimal.valueOf(300.00), null);
        when(orderDAO.createOrderTransactional(any(), any())).thenReturn(createdOrder);
        when(orderDAO.findById(100L)).thenReturn(Optional.of(createdOrder));

        OrderResponseDTO response = orderService.placeOrder(4L);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(BigDecimal.valueOf(300.00), response.getTotalAmount());
        verify(orderDAO, times(1)).createOrderTransactional(any(), any());
    }

    @Test
    public void testPlaceOrderEmptyCartFails() {
        when(cartDAO.getCartItems(4L)).thenReturn(List.of());

        assertThrows(AppException.class, () -> orderService.placeOrder(4L));
    }
}
