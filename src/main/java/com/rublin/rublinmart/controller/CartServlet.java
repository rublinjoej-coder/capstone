package com.rublin.rublinmart.controller;

import com.google.gson.JsonObject;
import com.rublin.rublinmart.exception.AppException;
import com.rublin.rublinmart.model.User;
import com.rublin.rublinmart.service.CartService;
import com.rublin.rublinmart.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

@WebServlet("/api/v1/cart/*")
public class CartServlet extends HttpServlet {

    private final CartService cartService = new CartService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getAuthenticatedBuyer(req, resp);
        if (user == null) return;

        try {
            Map<String, Object> summary = cartService.getCartSummary(user.getId());
            JsonUtil.sendSuccess(resp, summary);
        } catch (Exception e) {
            JsonUtil.sendError(resp, "INTERNAL_ERROR", "Error fetching cart", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getAuthenticatedBuyer(req, resp);
        if (user == null) return;

        String pathInfo = req.getPathInfo();

        try {
            JsonObject body = JsonUtil.readJson(req, JsonObject.class);
            if (body == null && !"/clear".equalsIgnoreCase(pathInfo)) {
                throw new AppException("VALIDATION_ERROR", "Invalid JSON payload");
            }

            if ("/add".equalsIgnoreCase(pathInfo)) {
                Long productId = body.has("productId") ? body.get("productId").getAsLong() : null;
                int quantity = body.has("quantity") ? body.get("quantity").getAsInt() : 1;

                cartService.addToCart(user.getId(), productId, quantity);
                JsonUtil.sendSuccess(resp, cartService.getCartSummary(user.getId()));

            } else if ("/update".equalsIgnoreCase(pathInfo)) {
                Long productId = body.has("productId") ? body.get("productId").getAsLong() : null;
                int quantity = body.has("quantity") ? body.get("quantity").getAsInt() : 0;

                cartService.updateQuantity(user.getId(), productId, quantity);
                JsonUtil.sendSuccess(resp, cartService.getCartSummary(user.getId()));

            } else if ("/remove".equalsIgnoreCase(pathInfo)) {
                Long productId = body.has("productId") ? body.get("productId").getAsLong() : null;

                cartService.removeFromCart(user.getId(), productId);
                JsonUtil.sendSuccess(resp, cartService.getCartSummary(user.getId()));

            } else {
                JsonUtil.sendError(resp, "NOT_FOUND", "Endpoint not found", HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (AppException e) {
            JsonUtil.sendError(resp, e.getCode(), e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            JsonUtil.sendError(resp, "INTERNAL_ERROR", "Error updating cart", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getAuthenticatedBuyer(req, resp);
        if (user == null) return;

        try {
            cartService.clearCart(user.getId());
            JsonUtil.sendSuccess(resp, cartService.getCartSummary(user.getId()));
        } catch (Exception e) {
            JsonUtil.sendError(resp, "INTERNAL_ERROR", "Error clearing cart", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private User getAuthenticatedBuyer(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            return (User) session.getAttribute("user");
        }
        JsonUtil.sendError(resp, "UNAUTHORIZED", "Authentication required", HttpServletResponse.SC_UNAUTHORIZED);
        return null;
    }
}
