package com.rublin.rublinmart.controller;

import com.google.gson.JsonObject;
import com.rublin.rublinmart.exception.AppException;
import com.rublin.rublinmart.model.Review;
import com.rublin.rublinmart.model.User;
import com.rublin.rublinmart.service.ReviewService;
import com.rublin.rublinmart.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/v1/reviews/*")
public class ReviewServlet extends HttpServlet {

    private final ReviewService reviewService = new ReviewService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String productIdParam = req.getParameter("productId");
        if (productIdParam == null || productIdParam.trim().isEmpty()) {
            JsonUtil.sendError(resp, "VALIDATION_ERROR", "productId parameter is required", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            Long productId = Long.parseLong(productIdParam);
            List<Review> reviews = reviewService.getProductReviews(productId);
            JsonUtil.sendSuccess(resp, reviews);
        } catch (AppException e) {
            JsonUtil.sendError(resp, e.getCode(), e.getMessage(), e.getStatusCode());
        } catch (NumberFormatException e) {
            JsonUtil.sendError(resp, "VALIDATION_ERROR", "Invalid productId format", HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            JsonUtil.sendError(resp, "INTERNAL_ERROR", "Error fetching reviews", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            JsonUtil.sendError(resp, "UNAUTHORIZED", "Authentication required to leave a review", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            JsonObject body = JsonUtil.readJson(req, JsonObject.class);
            if (body == null) {
                throw new AppException("VALIDATION_ERROR", "Invalid JSON payload");
            }

            Long productId = body.has("productId") ? body.get("productId").getAsLong() : null;
            Integer rating = body.has("rating") ? body.get("rating").getAsInt() : null;
            String comment = body.has("comment") ? body.get("comment").getAsString() : null;

            Review createdReview = reviewService.addReview(user.getId(), productId, rating, comment);
            JsonUtil.sendSuccess(resp, createdReview, HttpServletResponse.SC_CREATED);

        } catch (AppException e) {
            JsonUtil.sendError(resp, e.getCode(), e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            JsonUtil.sendError(resp, "INTERNAL_ERROR", "Error submitting review", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
