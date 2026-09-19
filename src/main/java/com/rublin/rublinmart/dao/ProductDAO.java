package com.rublin.rublinmart.dao;

import com.rublin.rublinmart.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductDAO {

    private static final Logger logger = LoggerFactory.getLogger(ProductDAO.class);

    public Product createProduct(Product product) {
        String sql = "INSERT INTO products (seller_id, name, description, price, stock_qty, category, image_url) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, product.getSellerId());
            ps.setString(2, product.getName());
            ps.setString(3, product.getDescription());
            ps.setBigDecimal(4, product.getPrice());
            ps.setInt(5, product.getStockQty());
            ps.setString(6, product.getCategory());
            ps.setString(7, product.getImageUrl());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    product.setId(rs.getLong(1));
                }
            }
            return product;
        } catch (SQLException e) {
            logger.error("Error creating product", e);
            throw new RuntimeException("Database error creating product", e);
        }
    }

    public boolean updateProduct(Product product) {
        String sql = "UPDATE products SET name = ?, description = ?, price = ?, stock_qty = ?, category = ?, image_url = ? WHERE id = ? AND seller_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setBigDecimal(3, product.getPrice());
            ps.setInt(4, product.getStockQty());
            ps.setString(5, product.getCategory());
            ps.setString(6, product.getImageUrl());
            ps.setLong(7, product.getId());
            ps.setLong(8, product.getSellerId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating product id: {}", product.getId(), e);
            throw new RuntimeException("Database error updating product", e);
        }
    }

    public boolean deleteProduct(Long id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting product id: {}", id, e);
            throw new RuntimeException("Database error deleting product", e);
        }
    }

    public boolean deleteProductBySeller(Long id, Long sellerId) {
        String sql = "DELETE FROM products WHERE id = ? AND seller_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.setLong(2, sellerId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting product id: {} by seller: {}", id, sellerId, e);
            throw new RuntimeException("Database error deleting seller product", e);
        }
    }

    public Optional<Product> findById(Long id) {
        String sql = "SELECT p.*, u.name as seller_name, " +
                "(SELECT AVG(CAST(r.rating AS DOUBLE)) FROM reviews r WHERE r.product_id = p.id) as avg_rating, " +
                "(SELECT COUNT(r.id) FROM reviews r WHERE r.product_id = p.id) as review_count " +
                "FROM products p JOIN users u ON p.seller_id = u.id WHERE p.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding product by id: {}", id, e);
            throw new RuntimeException("Database error finding product", e);
        }
        return Optional.empty();
    }

    public List<Product> findAll(String search, String category) {
        List<Product> products = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT p.*, u.name as seller_name, " +
                "(SELECT AVG(CAST(r.rating AS DOUBLE)) FROM reviews r WHERE r.product_id = p.id) as avg_rating, " +
                "(SELECT COUNT(r.id) FROM reviews r WHERE r.product_id = p.id) as review_count " +
                "FROM products p JOIN users u ON p.seller_id = u.id WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();
        if (category != null && !category.trim().isEmpty() && !"all".equalsIgnoreCase(category)) {
            sql.append("AND LOWER(p.category) = ? ");
            params.add(category.toLowerCase().trim());
        }

        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND (LOWER(p.name) LIKE ? OR LOWER(p.description) LIKE ?) ");
            String queryPattern = "%" + search.toLowerCase().trim() + "%";
            params.add(queryPattern);
            params.add(queryPattern);
        }

        sql.append("ORDER BY p.id DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching products", e);
            throw new RuntimeException("Database error finding products", e);
        }
        return products;
    }

    public List<Product> findBySellerId(Long sellerId) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, u.name as seller_name, " +
                "(SELECT AVG(CAST(r.rating AS DOUBLE)) FROM reviews r WHERE r.product_id = p.id) as avg_rating, " +
                "(SELECT COUNT(r.id) FROM reviews r WHERE r.product_id = p.id) as review_count " +
                "FROM products p JOIN users u ON p.seller_id = u.id WHERE p.seller_id = ? ORDER BY p.id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error listing seller products: {}", sellerId, e);
            throw new RuntimeException("Database error listing seller products", e);
        }
        return products;
    }

    public List<String> getCategories() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM products ORDER BY category ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                categories.add(rs.getString("category"));
            }
        } catch (SQLException e) {
            logger.error("Error fetching product categories", e);
        }
        return categories;
    }

    public boolean reduceStock(Connection conn, Long productId, int quantity) throws SQLException {
        String sql = "UPDATE products SET stock_qty = stock_qty - ? WHERE id = ? AND stock_qty >= ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setLong(2, productId);
            ps.setInt(3, quantity);
            return ps.executeUpdate() > 0;
        }
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getLong("id"));
        p.setSellerId(rs.getLong("seller_id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setStockQty(rs.getInt("stock_qty"));
        p.setCategory(rs.getString("category"));
        p.setImageUrl(rs.getString("image_url"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        p.setSellerName(rs.getString("seller_name"));
        p.setAverageRating(rs.getDouble("avg_rating"));
        p.setReviewCount(rs.getInt("review_count"));
        return p;
    }
}
