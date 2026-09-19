package com.rublin.rublinmart.service;

import com.rublin.rublinmart.dao.OrderDAO;
import com.rublin.rublinmart.dao.ProductDAO;
import com.rublin.rublinmart.dao.ReviewDAO;
import com.rublin.rublinmart.exception.AppException;
import com.rublin.rublinmart.model.Review;
import com.rublin.rublinmart.util.ValidationUtil;

import java.util.List;

public class ReviewService {

    private final ReviewDAO reviewDAO;
    private final ProductDAO productDAO;
    private final OrderDAO orderDAO;

    public ReviewService() {
        this.reviewDAO = new ReviewDAO();
        this.productDAO = new ProductDAO();
        this.orderDAO = new OrderDAO();
    }

    public ReviewService(ReviewDAO reviewDAO, ProductDAO productDAO, OrderDAO orderDAO) {
        this.reviewDAO = reviewDAO;
        this.productDAO = productDAO;
        this.orderDAO = orderDAO;
    }

    public Review addReview(Long userId, Long productId, Integer rating, String comment) {
        ValidationUtil.validateRating(rating);
        ValidationUtil.validateRequired(comment, "Comment");

        productDAO.findById(productId)
                .orElseThrow(() -> new AppException("NOT_FOUND", "Product not found", 404));

        boolean hasPurchased = orderDAO.hasBuyerPurchasedProduct(userId, productId);
        if (!hasPurchased) {
            throw new AppException("FORBIDDEN", "Only buyers who purchased this product can leave a review.", 403);
        }

        boolean alreadyReviewed = reviewDAO.hasUserReviewedProduct(userId, productId);
        if (alreadyReviewed) {
            throw new AppException("DUPLICATE_REVIEW", "You have already reviewed this product.", 409);
        }

        Review review = new Review(null, productId, userId, rating, comment.trim(), null);
        return reviewDAO.createReview(review);
    }

    public List<Review> getProductReviews(Long productId) {
        productDAO.findById(productId)
                .orElseThrow(() -> new AppException("NOT_FOUND", "Product not found", 404));
        return reviewDAO.findByProductId(productId);
    }
}
