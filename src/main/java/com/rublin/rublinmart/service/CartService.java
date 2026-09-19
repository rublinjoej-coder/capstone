package com.rublin.rublinmart.service;

import com.rublin.rublinmart.dao.CartDAO;
import com.rublin.rublinmart.dao.ProductDAO;
import com.rublin.rublinmart.exception.AppException;
import com.rublin.rublinmart.model.CartItem;
import com.rublin.rublinmart.model.Product;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartService {

    private final CartDAO cartDAO;
    private final ProductDAO productDAO;

    public CartService() {
        this.cartDAO = new CartDAO();
        this.productDAO = new ProductDAO();
    }

    public CartService(CartDAO cartDAO, ProductDAO productDAO) {
        this.cartDAO = cartDAO;
        this.productDAO = productDAO;
    }

    public void addToCart(Long userId, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new AppException("VALIDATION_ERROR", "Quantity must be greater than 0");
        }

        Product product = productDAO.findById(productId)
                .orElseThrow(() -> new AppException("NOT_FOUND", "Product not found", 404));

        if (product.getStockQty() < quantity) {
            throw new AppException("STOCK_EXCEEDED", "Cannot add " + quantity + " items. Only " + product.getStockQty() + " available in stock.");
        }

        // Check if item is already in cart to ensure total quantity doesn't exceed stock
        List<CartItem> currentCart = cartDAO.getCartItems(userId);
        int currentQtyInCart = currentCart.stream()
                .filter(item -> item.getProductId().equals(productId))
                .mapToInt(CartItem::getQuantity)
                .findFirst()
                .orElse(0);

        if (currentQtyInCart + quantity > product.getStockQty()) {
            throw new AppException("STOCK_EXCEEDED", "Total requested quantity (" + (currentQtyInCart + quantity) + ") exceeds available stock (" + product.getStockQty() + ").");
        }

        cartDAO.addToCart(userId, productId, quantity);
    }

    public void updateQuantity(Long userId, Long productId, int quantity) {
        if (quantity <= 0) {
            cartDAO.removeFromCart(userId, productId);
            return;
        }

        Product product = productDAO.findById(productId)
                .orElseThrow(() -> new AppException("NOT_FOUND", "Product not found", 404));

        if (quantity > product.getStockQty()) {
            throw new AppException("STOCK_EXCEEDED", "Requested quantity (" + quantity + ") exceeds available stock (" + product.getStockQty() + ").");
        }

        cartDAO.updateQuantity(userId, productId, quantity);
    }

    public void removeFromCart(Long userId, Long productId) {
        cartDAO.removeFromCart(userId, productId);
    }

    public Map<String, Object> getCartSummary(Long userId) {
        List<CartItem> items = cartDAO.getCartItems(userId);

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem item : items) {
            subtotal = subtotal.add(item.getSubtotal());
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("items", items);
        summary.put("subtotal", subtotal);
        summary.put("totalAmount", subtotal); // Additional fees or taxes can be layered safely server-side
        summary.put("itemCount", items.stream().mapToInt(CartItem::getQuantity).sum());

        return summary;
    }

    public void clearCart(Long userId) {
        cartDAO.clearCart(userId);
    }
}
