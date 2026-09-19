package com.rublin.rublinmart.service;

import com.rublin.rublinmart.dao.CartDAO;
import com.rublin.rublinmart.dao.ProductDAO;
import com.rublin.rublinmart.exception.AppException;
import com.rublin.rublinmart.model.CartItem;
import com.rublin.rublinmart.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CartServiceTest {

    private CartDAO cartDAO;
    private ProductDAO productDAO;
    private CartService cartService;

    @BeforeEach
    public void setUp() {
        cartDAO = mock(CartDAO.class);
        productDAO = mock(ProductDAO.class);
        cartService = new CartService(cartDAO, productDAO);
    }

    @Test
    public void testAddToCartSuccess() {
        Product p = new Product(1L, 2L, "Laptop", "Desc", BigDecimal.valueOf(1000), 5, "Electronics", "img", null);
        when(productDAO.findById(1L)).thenReturn(Optional.of(p));
        when(cartDAO.getCartItems(4L)).thenReturn(List.of());

        assertDoesNotThrow(() -> cartService.addToCart(4L, 1L, 2));
        verify(cartDAO, times(1)).addToCart(4L, 1L, 2);
    }

    @Test
    public void testAddToCartExceedsStockFails() {
        Product p = new Product(1L, 2L, "Laptop", "Desc", BigDecimal.valueOf(1000), 2, "Electronics", "img", null);
        when(productDAO.findById(1L)).thenReturn(Optional.of(p));
        when(cartDAO.getCartItems(4L)).thenReturn(List.of());

        assertThrows(AppException.class, () -> cartService.addToCart(4L, 1L, 5));
    }

    @Test
    public void testCartSummaryCalculation() {
        CartItem item1 = new CartItem(1L, 4L, 1L, 2);
        item1.setProductPrice(BigDecimal.valueOf(50.00));
        CartItem item2 = new CartItem(2L, 4L, 2L, 1);
        item2.setProductPrice(BigDecimal.valueOf(100.00));

        when(cartDAO.getCartItems(4L)).thenReturn(List.of(item1, item2));

        Map<String, Object> summary = cartService.getCartSummary(4L);
        assertEquals(BigDecimal.valueOf(200.00), summary.get("subtotal"));
        assertEquals(3, summary.get("itemCount"));
    }
}
