package com.rublin.rublinmart.controller;

import com.google.gson.JsonObject;
import com.rublin.rublinmart.dto.OrderResponseDTO;
import com.rublin.rublinmart.dto.ProductResponseDTO;
import com.rublin.rublinmart.exception.AppException;
import com.rublin.rublinmart.model.User;
import com.rublin.rublinmart.service.OrderService;
import com.rublin.rublinmart.service.ProductService;
import com.rublin.rublinmart.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/v1/seller/*")
public class SellerServlet extends HttpServlet {

    private final ProductService productService = new ProductService();
    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getAuthenticatedSeller(req, resp);
        if (user == null) return;

        String pathInfo = req.getPathInfo();

        try {
            if ("/products".equalsIgnoreCase(pathInfo)) {
                List<ProductResponseDTO> products = productService.getSellerProducts(user.getId());
                JsonUtil.sendSuccess(resp, products);
            } else if ("/orders".equalsIgnoreCase(pathInfo)) {
                List<OrderResponseDTO> orders = orderService.getSellerOrders(user.getId());
                JsonUtil.sendSuccess(resp, orders);
            } else {
                JsonUtil.sendError(resp, "NOT_FOUND", "Seller endpoint not found", HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (AppException e) {
            JsonUtil.sendError(resp, e.getCode(), e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            JsonUtil.sendError(resp, "INTERNAL_ERROR", "Error executing seller request", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getAuthenticatedSeller(req, resp);
        if (user == null) return;

        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo != null && pathInfo.startsWith("/orders/") && pathInfo.endsWith("/status")) {
                String[] parts = pathInfo.split("/");
                Long orderId = Long.parseLong(parts[2]);

                JsonObject body = JsonUtil.readJson(req, JsonObject.class);
                if (body == null || !body.has("status")) {
                    throw new AppException("VALIDATION_ERROR", "Status field is required");
                }

                String status = body.get("status").getAsString();
                orderService.updateOrderStatus(orderId, status);
                JsonUtil.sendSuccess(resp, "Order status updated to " + status);
            } else {
                JsonUtil.sendError(resp, "NOT_FOUND", "Seller endpoint not found", HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (AppException e) {
            JsonUtil.sendError(resp, e.getCode(), e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            JsonUtil.sendError(resp, "INTERNAL_ERROR", "Error updating seller order", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private User getAuthenticatedSeller(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            User user = (User) session.getAttribute("user");
            if ("SELLER".equalsIgnoreCase(user.getRole())) {
                return user;
            }
        }
        JsonUtil.sendError(resp, "FORBIDDEN", "Only registered sellers can access this endpoint", HttpServletResponse.SC_FORBIDDEN);
        return null;
    }
}
