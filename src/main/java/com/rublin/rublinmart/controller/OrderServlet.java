package com.rublin.rublinmart.controller;

import com.rublin.rublinmart.dto.OrderResponseDTO;
import com.rublin.rublinmart.exception.AppException;
import com.rublin.rublinmart.model.User;
import com.rublin.rublinmart.service.OrderService;
import com.rublin.rublinmart.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/v1/orders/*")
public class OrderServlet extends HttpServlet {

    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getAuthenticatedUser(req, resp);
        if (user == null) return;

        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo == null || "/".equals(pathInfo)) {
                List<OrderResponseDTO> orders = orderService.getBuyerOrders(user.getId());
                JsonUtil.sendSuccess(resp, orders);
            } else {
                Long orderId = Long.parseLong(pathInfo.substring(1));
                OrderResponseDTO order = orderService.getOrderDetails(orderId, user.getId(), user.getRole());
                JsonUtil.sendSuccess(resp, order);
            }
        } catch (AppException e) {
            JsonUtil.sendError(resp, e.getCode(), e.getMessage(), e.getStatusCode());
        } catch (NumberFormatException e) {
            JsonUtil.sendError(resp, "VALIDATION_ERROR", "Invalid order ID format", HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            JsonUtil.sendError(resp, "INTERNAL_ERROR", "Error fetching order", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getAuthenticatedUser(req, resp);
        if (user == null) return;

        try {
            OrderResponseDTO order = orderService.placeOrder(user.getId());
            JsonUtil.sendSuccess(resp, order, HttpServletResponse.SC_CREATED);
        } catch (AppException e) {
            JsonUtil.sendError(resp, e.getCode(), e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            JsonUtil.sendError(resp, "INTERNAL_ERROR", "Error placing order", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private User getAuthenticatedUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            return (User) session.getAttribute("user");
        }
        JsonUtil.sendError(resp, "UNAUTHORIZED", "Authentication required", HttpServletResponse.SC_UNAUTHORIZED);
        return null;
    }
}
