package com.rublin.rublinmart.dao;

import com.rublin.rublinmart.exception.AppException;
import com.rublin.rublinmart.model.Order;
import com.rublin.rublinmart.model.OrderItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderDAO {

    private static final Logger logger = LoggerFactory.getLogger(OrderDAO.class);
    private final ProductDAO productDAO = new ProductDAO();
    private final CartDAO cartDAO = new CartDAO();

    public Order createOrderTransactional(Order order, List<OrderItem> items) {
        String insertOrderSql = "INSERT INTO orders (buyer_id, status, total_amount) VALUES (?, ?, ?)";
        String insertItemSql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Insert order record
            try (PreparedStatement psOrder = conn.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS)) {
                psOrder.setLong(1, order.getBuyerId());
                psOrder.setString(2, order.getStatus() != null ? order.getStatus() : "PENDING");
                psOrder.setBigDecimal(3, order.getTotalAmount());
                psOrder.executeUpdate();

                try (ResultSet rs = psOrder.getGeneratedKeys()) {
                    if (rs.next()) {
                        order.setId(rs.getLong(1));
                    }
                }
            }

            // 2. Insert order items & reduce stock atomically
            try (PreparedStatement psItem = conn.prepareStatement(insertItemSql)) {
                for (OrderItem item : items) {
                    item.setOrderId(order.getId());
                    boolean stockUpdated = productDAO.reduceStock(conn, item.getProductId(), item.getQuantity());
                    if (!stockUpdated) {
                        throw new AppException("OUT_OF_STOCK", "Insufficient stock for product ID: " + item.getProductId());
                    }

                    psItem.setLong(1, item.getOrderId());
                    psItem.setLong(2, item.getProductId());
                    psItem.setInt(3, item.getQuantity());
                    psItem.setBigDecimal(4, item.getUnitPrice());
                    psItem.addBatch();
                }
                psItem.executeBatch();
            }

            // 3. Clear cart
            cartDAO.clearCart(conn, order.getBuyerId());

            // 4. Commit transaction
            conn.commit();
            order.setItems(items);
            logger.info("Order placed successfully with ID: {}", order.getId());
            return order;
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Failed to rollback order transaction", ex);
                }
            }
            if (e instanceof AppException) {
                throw (AppException) e;
            }
            logger.error("Error executing order creation transaction", e);
            throw new RuntimeException("Database error placing order", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.error("Failed to restore connection autoCommit status", e);
                }
            }
        }
    }

    public Optional<Order> findById(Long orderId) {
        String sql = "SELECT o.*, u.name as buyer_name, u.email as buyer_email " +
                "FROM orders o JOIN users u ON o.buyer_id = u.id WHERE o.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    order.setItems(getOrderItems(conn, order.getId()));
                    return Optional.of(order);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding order by id: {}", orderId, e);
            throw new RuntimeException("Database error finding order", e);
        }
        return Optional.empty();
    }

    public List<Order> findByBuyerId(Long buyerId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, u.name as buyer_name, u.email as buyer_email " +
                "FROM orders o JOIN users u ON o.buyer_id = u.id WHERE o.buyer_id = ? ORDER BY o.id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, buyerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    order.setItems(getOrderItems(conn, order.getId()));
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding orders for buyer: {}", buyerId, e);
            throw new RuntimeException("Database error finding buyer orders", e);
        }
        return orders;
    }

    public List<Order> findBySellerId(Long sellerId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT DISTINCT o.*, u.name as buyer_name, u.email as buyer_email " +
                "FROM orders o " +
                "JOIN users u ON o.buyer_id = u.id " +
                "JOIN order_items oi ON o.id = oi.order_id " +
                "JOIN products p ON oi.product_id = p.id " +
                "WHERE p.seller_id = ? ORDER BY o.id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    order.setItems(getSellerOrderItems(conn, order.getId(), sellerId));
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding orders for seller: {}", sellerId, e);
            throw new RuntimeException("Database error finding seller orders", e);
        }
        return orders;
    }

    public List<Order> findAll() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, u.name as buyer_name, u.email as buyer_email " +
                "FROM orders o JOIN users u ON o.buyer_id = u.id ORDER BY o.id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                order.setItems(getOrderItems(conn, order.getId()));
                orders.add(order);
            }
        } catch (SQLException e) {
            logger.error("Error finding all orders", e);
            throw new RuntimeException("Database error finding all orders", e);
        }
        return orders;
    }

    public boolean updateOrderStatus(Long orderId, String newStatus) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus.toUpperCase());
            ps.setLong(2, orderId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating status for order: {}", orderId, e);
            throw new RuntimeException("Database error updating order status", e);
        }
    }

    public boolean hasBuyerPurchasedProduct(Long buyerId, Long productId) {
        String sql = "SELECT COUNT(oi.id) FROM order_items oi " +
                "JOIN orders o ON oi.order_id = o.id " +
                "WHERE o.buyer_id = ? AND oi.product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, buyerId);
            ps.setLong(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking buyer purchase history", e);
        }
        return false;
    }

    private List<OrderItem> getOrderItems(Connection conn, Long orderId) throws SQLException {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT oi.*, p.name as product_name, p.image_url as product_image, p.seller_id " +
                "FROM order_items oi JOIN products p ON oi.product_id = p.id WHERE oi.order_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setId(rs.getLong("id"));
                    item.setOrderId(rs.getLong("order_id"));
                    item.setProductId(rs.getLong("product_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setProductName(rs.getString("product_name"));
                    item.setProductImage(rs.getString("product_image"));
                    item.setSellerId(rs.getLong("seller_id"));
                    items.add(item);
                }
            }
        }
        return items;
    }

    private List<OrderItem> getSellerOrderItems(Connection conn, Long orderId, Long sellerId) throws SQLException {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT oi.*, p.name as product_name, p.image_url as product_image, p.seller_id " +
                "FROM order_items oi JOIN products p ON oi.product_id = p.id " +
                "WHERE oi.order_id = ? AND p.seller_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            ps.setLong(2, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setId(rs.getLong("id"));
                    item.setOrderId(rs.getLong("order_id"));
                    item.setProductId(rs.getLong("product_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setProductName(rs.getString("product_name"));
                    item.setProductImage(rs.getString("product_image"));
                    item.setSellerId(rs.getLong("seller_id"));
                    items.add(item);
                }
            }
        }
        return items;
    }

    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getLong("id"));
        order.setBuyerId(rs.getLong("buyer_id"));
        order.setStatus(rs.getString("status"));
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setCreatedAt(rs.getTimestamp("created_at"));
        order.setBuyerName(rs.getString("buyer_name"));
        order.setBuyerEmail(rs.getString("buyer_email"));
        return order;
    }
}
