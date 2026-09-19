package com.rublin.rublinmart.controller;

import com.rublin.rublinmart.dto.OrderResponseDTO;
import com.rublin.rublinmart.dto.ProductResponseDTO;
import com.rublin.rublinmart.dto.UserResponseDTO;
import com.rublin.rublinmart.exception.AppException;
import com.rublin.rublinmart.service.AdminService;
import com.rublin.rublinmart.service.OrderService;
import com.rublin.rublinmart.service.ProductService;
import com.rublin.rublinmart.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/v1/admin/*")
public class AdminServlet extends HttpServlet {

    private final AdminService adminService = new AdminService();
    private final ProductService productService = new ProductService();
    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        try {
            if ("/users".equalsIgnoreCase(pathInfo)) {
                String role = req.getParameter("role");
                List<UserResponseDTO> users = (role != null && !role.trim().isEmpty())
                        ? adminService.getUsersByRole(role)
                        : adminService.getAllUsers();
                JsonUtil.sendSuccess(resp, users);

            } else if ("/products".equalsIgnoreCase(pathInfo)) {
                List<ProductResponseDTO> products = productService.searchProducts(null, null);
                JsonUtil.sendSuccess(resp, products);

            } else if ("/orders".equalsIgnoreCase(pathInfo)) {
                List<OrderResponseDTO> orders = orderService.getAllOrdersForAdmin();
                JsonUtil.sendSuccess(resp, orders);

            } else {
                JsonUtil.sendError(resp, "NOT_FOUND", "Admin endpoint not found", HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (AppException e) {
            JsonUtil.sendError(resp, e.getCode(), e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            JsonUtil.sendError(resp, "INTERNAL_ERROR", "Error executing admin request", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
