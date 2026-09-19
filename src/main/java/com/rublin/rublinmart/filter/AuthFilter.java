package com.rublin.rublinmart.filter;

import com.rublin.rublinmart.model.User;
import com.rublin.rublinmart.util.JsonUtil;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        String path = uri.substring(contextPath.length());

        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        boolean isApiRequest = path.startsWith("/api/v1/");

        // Check Admin protected routes
        if (path.startsWith("/api/v1/admin") || path.endsWith("admin-dashboard.html")) {
            if (user == null) {
                handleUnauthorized(req, res, isApiRequest);
                return;
            }
            if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
                handleForbidden(req, res, isApiRequest);
                return;
            }
        }

        // Check Seller protected routes
        if (path.startsWith("/api/v1/seller") || path.endsWith("seller-dashboard.html")) {
            if (user == null) {
                handleUnauthorized(req, res, isApiRequest);
                return;
            }
            if (!"SELLER".equalsIgnoreCase(user.getRole())) {
                handleForbidden(req, res, isApiRequest);
                return;
            }
        }

        // Check Buyer protected routes
        if (path.startsWith("/api/v1/cart") || path.endsWith("cart.html") ||
            path.endsWith("checkout.html") || path.endsWith("order-success.html") || path.endsWith("orders.html")) {
            if (user == null) {
                handleUnauthorized(req, res, isApiRequest);
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private void handleUnauthorized(HttpServletRequest req, HttpServletResponse res, boolean isApiRequest) throws IOException {
        if (isApiRequest) {
            JsonUtil.sendError(res, "UNAUTHORIZED", "Authentication required. Please log in.", HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            res.sendRedirect(req.getContextPath() + "/pages/login.html");
        }
    }

    private void handleForbidden(HttpServletRequest req, HttpServletResponse res, boolean isApiRequest) throws IOException {
        if (isApiRequest) {
            JsonUtil.sendError(res, "FORBIDDEN", "Access denied. Insufficient permissions.", HttpServletResponse.SC_FORBIDDEN);
        } else {
            res.sendRedirect(req.getContextPath() + "/403.html");
        }
    }

    @Override
    public void destroy() {}
}
