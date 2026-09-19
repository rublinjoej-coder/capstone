package com.rublin.rublinmart.dao;

import com.rublin.rublinmart.model.CartItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartDAO {

    private static final Logger logger = LoggerFactory.getLogger(CartDAO.class);

    public boolean addToCart(Long userId, Long productId, int quantity) {
        String sql = "MERGE INTO cart_items (user_id, product_id, quantity) KEY(user_id, product_id) VALUES (?, ?, " +
                "COALESCE((SELECT quantity FROM cart_items WHERE user_id = ? AND product_id = ?), 0) + ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ps.setLong(2, productId);
            ps.setLong(3, userId);
            ps.setLong(4, productId);
            ps.setInt(5, quantity);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error adding to cart: user={}, product={}", userId, productId, e);
            throw new RuntimeException("Database error adding to cart", e);
        }
    }

    public boolean updateQuantity(Long userId, Long productId, int quantity) {
        if (quantity <= 0) {
            return removeFromCart(userId, productId);
        }
        String sql = "UPDATE cart_items SET quantity = ? WHERE user_id = ? AND product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, quantity);
            ps.setLong(2, userId);
            ps.setLong(3, productId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating cart item quantity: user={}, product={}", userId, productId, e);
            throw new RuntimeException("Database error updating cart", e);
        }
    }

    public boolean removeFromCart(Long userId, Long productId) {
        String sql = "DELETE FROM cart_items WHERE user_id = ? AND product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ps.setLong(2, productId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error removing item from cart: user={}, product={}", userId, productId, e);
            throw new RuntimeException("Database error removing cart item", e);
        }
    }

    public List<CartItem> getCartItems(Long userId) {
        List<CartItem> items = new ArrayList<>();
        String sql = "SELECT c.id, c.user_id, c.product_id, c.quantity, " +
                "p.name as product_name, p.price as product_price, p.stock_qty, p.image_url, p.category, p.seller_id " +
                "FROM cart_items c JOIN products p ON c.product_id = p.id " +
                "WHERE c.user_id = ? ORDER BY c.id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CartItem item = new CartItem();
                    item.setId(rs.getLong("id"));
                    item.setUserId(rs.getLong("user_id"));
                    item.setProductId(rs.getLong("product_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setProductName(rs.getString("product_name"));
                    item.setProductPrice(rs.getBigDecimal("product_price"));
                    item.setStockQty(rs.getInt("stock_qty"));
                    item.setImageUrl(rs.getString("image_url"));
                    item.setCategory(rs.getString("category"));
                    item.setSellerId(rs.getLong("seller_id"));
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting cart items for user: {}", userId, e);
            throw new RuntimeException("Database error getting cart items", e);
        }
        return items;
    }

    public boolean clearCart(Long userId) {
        String sql = "DELETE FROM cart_items WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            return ps.executeUpdate() >= 0;
        } catch (SQLException e) {
            logger.error("Error clearing cart for user: {}", userId, e);
            throw new RuntimeException("Database error clearing cart", e);
        }
    }

    public boolean clearCart(Connection conn, Long userId) throws SQLException {
        String sql = "DELETE FROM cart_items WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            return ps.executeUpdate() >= 0;
        }
    }
}
