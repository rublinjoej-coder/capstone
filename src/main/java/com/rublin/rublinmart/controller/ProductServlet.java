package com.rublin.rublinmart.controller;

import com.google.gson.JsonObject;
import com.rublin.rublinmart.dto.ProductResponseDTO;
import com.rublin.rublinmart.exception.AppException;
import com.rublin.rublinmart.model.User;
import com.rublin.rublinmart.service.ProductService;
import com.rublin.rublinmart.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet("/api/v1/products/*")
public class ProductServlet extends HttpServlet {

    private final ProductService productService = new ProductService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo == null || "/".equals(pathInfo)) {
                String search = req.getParameter("search");
                String category = req.getParameter("category");
                List<ProductResponseDTO> products = productService.searchProducts(search, category);
                JsonUtil.sendSuccess(resp, products);
            } else if ("/categories".equalsIgnoreCase(pathInfo)) {
                List<String> categories = productService.getCategories();
                JsonUtil.sendSuccess(resp, categories);
            } else {
                // Parse ID
                Long productId = parseId(pathInfo);
                ProductResponseDTO product = productService.getProductDetails(productId);
                JsonUtil.sendSuccess(resp, product);
            }
        } catch (AppException e) {
            JsonUtil.sendError(resp, e.getCode(), e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            JsonUtil.sendError(resp, "INTERNAL_ERROR", "Error processing product request", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getAuthenticatedUser(req, "SELLER");
        if (user == null) {
            JsonUtil.sendError(resp, "FORBIDDEN", "Only sellers can add products", HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try {
            JsonObject body = JsonUtil.readJson(req, JsonObject.class);
            if (body == null) {
                throw new AppException("VALIDATION_ERROR", "Invalid JSON payload");
            }

            String name = body.has("name") ? body.get("name").getAsString() : null;
            String description = body.has("description") ? body.get("description").getAsString() : null;
            BigDecimal price = body.has("price") ? body.get("price").getAsBigDecimal() : null;
            Integer stockQty = body.has("stockQty") ? body.get("stockQty").getAsInt() : null;
            String category = body.has("category") ? body.get("category").getAsString() : null;
            String imageUrl = body.has("imageUrl") ? body.get("imageUrl").getAsString() : null;

            ProductResponseDTO newProduct = productService.addProduct(user.getId(), name, description, price, stockQty, category, imageUrl);
            JsonUtil.sendSuccess(resp, newProduct, HttpServletResponse.SC_CREATED);

        } catch (AppException e) {
            JsonUtil.sendError(resp, e.getCode(), e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            JsonUtil.sendError(resp, "INTERNAL_ERROR", "Error adding product", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getAuthenticatedUser(req, "SELLER");
        if (user == null) {
            JsonUtil.sendError(resp, "FORBIDDEN", "Only sellers can edit products", HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try {
            Long productId = parseId(req.getPathInfo());
            JsonObject body = JsonUtil.readJson(req, JsonObject.class);
            if (body == null) {
                throw new AppException("VALIDATION_ERROR", "Invalid JSON payload");
            }

            String name = body.has("name") ? body.get("name").getAsString() : null;
            String description = body.has("description") ? body.get("description").getAsString() : null;
            BigDecimal price = body.has("price") ? body.get("price").getAsBigDecimal() : null;
            Integer stockQty = body.has("stockQty") ? body.get("stockQty").getAsInt() : null;
            String category = body.has("category") ? body.get("category").getAsString() : null;
            String imageUrl = body.has("imageUrl") ? body.get("imageUrl").getAsString() : null;

            ProductResponseDTO updatedProduct = productService.updateProduct(productId, user.getId(), name, description, price, stockQty, category, imageUrl);
            JsonUtil.sendSuccess(resp, updatedProduct);

        } catch (AppException e) {
            JsonUtil.sendError(resp, e.getCode(), e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            JsonUtil.sendError(resp, "INTERNAL_ERROR", "Error updating product", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            JsonUtil.sendError(resp, "UNAUTHORIZED", "Authentication required", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            Long productId = parseId(req.getPathInfo());

            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                productService.deleteProductByAdmin(productId);
            } else if ("SELLER".equalsIgnoreCase(user.getRole())) {
                productService.deleteProductBySeller(productId, user.getId());
            } else {
                throw new AppException("FORBIDDEN", "Access denied", 403);
            }

            JsonUtil.sendSuccess(resp, "Product deleted successfully");

        } catch (AppException e) {
            JsonUtil.sendError(resp, e.getCode(), e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            JsonUtil.sendError(resp, "INTERNAL_ERROR", "Error deleting product", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private User getAuthenticatedUser(HttpServletRequest req, String requiredRole) {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            User user = (User) session.getAttribute("user");
            if (requiredRole == null || requiredRole.equalsIgnoreCase(user.getRole())) {
                return user;
            }
        }
        return null;
    }

    private Long parseId(String pathInfo) {
        if (pathInfo == null || pathInfo.length() <= 1) {
            throw new AppException("VALIDATION_ERROR", "Product ID required in path");
        }
        try {
            return Long.parseLong(pathInfo.substring(1));
        } catch (NumberFormatException e) {
            throw new AppException("VALIDATION_ERROR", "Invalid Product ID format");
        }
    }
}
