package com.rublin.rublinmart.dao;

import com.rublin.rublinmart.model.Review;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewDAO {

    private static final Logger logger = LoggerFactory.getLogger(ReviewDAO.class);

    public Review createReview(Review review) {
        String sql = "INSERT INTO reviews (product_id, user_id, rating, comment) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, review.getProductId());
            ps.setLong(2, review.getUserId());
            ps.setInt(3, review.getRating());
            ps.setString(4, review.getComment());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    review.setId(rs.getLong(1));
                }
            }
            return review;
        } catch (SQLException e) {
            logger.error("Error creating review", e);
            throw new RuntimeException("Database error creating review", e);
        }
    }

    public List<Review> findByProductId(Long productId) {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT r.*, u.name as user_name FROM reviews r " +
                "JOIN users u ON r.user_id = u.id WHERE r.product_id = ? ORDER BY r.id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Review r = new Review();
                    r.setId(rs.getLong("id"));
                    r.setProductId(rs.getLong("product_id"));
                    r.setUserId(rs.getLong("user_id"));
                    r.setRating(rs.getInt("rating"));
                    r.setComment(rs.getString("comment"));
                    r.setCreatedAt(rs.getTimestamp("created_at"));
                    r.setUserName(rs.getString("user_name"));
                    reviews.add(r);
                }
            }
        } catch (SQLException e) {
            logger.error("Error listing reviews for product: {}", productId, e);
            throw new RuntimeException("Database error listing reviews", e);
        }
        return reviews;
    }

    public boolean hasUserReviewedProduct(Long userId, Long productId) {
        String sql = "SELECT COUNT(id) FROM reviews WHERE user_id = ? AND product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ps.setLong(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking user review status", e);
        }
        return false;
    }
}
