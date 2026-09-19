package com.rublin.rublinmart.controller;

import com.google.gson.JsonObject;
import com.rublin.rublinmart.dto.UserResponseDTO;
import com.rublin.rublinmart.exception.AppException;
import com.rublin.rublinmart.model.User;
import com.rublin.rublinmart.service.AuthService;
import com.rublin.rublinmart.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/api/v1/auth/*")
public class AuthServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if ("/me".equalsIgnoreCase(pathInfo)) {
            HttpSession session = req.getSession(false);
            if (session != null && session.getAttribute("user") != null) {
                User user = (User) session.getAttribute("user");
                JsonUtil.sendSuccess(resp, new UserResponseDTO(user));
            } else {
                JsonUtil.sendError(resp, "UNAUTHORIZED", "Not logged in", HttpServletResponse.SC_UNAUTHORIZED);
            }
        } else {
            JsonUtil.sendError(resp, "NOT_FOUND", "Endpoint not found", HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        try {
            if ("/register".equalsIgnoreCase(pathInfo)) {
                JsonObject body = JsonUtil.readJson(req, JsonObject.class);
                if (body == null) {
                    throw new AppException("VALIDATION_ERROR", "Invalid JSON payload");
                }

                String name = body.has("name") ? body.get("name").getAsString() : null;
                String email = body.has("email") ? body.get("email").getAsString() : null;
                String password = body.has("password") ? body.get("password").getAsString() : null;
                String confirmPassword = body.has("confirmPassword") ? body.get("confirmPassword").getAsString() : null;
                String role = body.has("role") ? body.get("role").getAsString() : "BUYER";

                UserResponseDTO registeredUser = authService.register(name, email, password, confirmPassword, role);
                JsonUtil.sendSuccess(resp, registeredUser, HttpServletResponse.SC_CREATED);

            } else if ("/login".equalsIgnoreCase(pathInfo)) {
                JsonObject body = JsonUtil.readJson(req, JsonObject.class);
                if (body == null) {
                    throw new AppException("VALIDATION_ERROR", "Invalid JSON payload");
                }

                String email = body.has("email") ? body.get("email").getAsString() : null;
                String password = body.has("password") ? body.get("password").getAsString() : null;

                User user = authService.login(email, password);

                // Session ID Regeneration & Security Configuration
                HttpSession oldSession = req.getSession(false);
                if (oldSession != null) {
                    oldSession.invalidate();
                }
                HttpSession session = req.getSession(true);
                session.setAttribute("user", user);
                session.setMaxInactiveInterval(1800); // 30 minutes timeout

                JsonUtil.sendSuccess(resp, new UserResponseDTO(user));

            } else if ("/logout".equalsIgnoreCase(pathInfo)) {
                HttpSession session = req.getSession(false);
                if (session != null) {
                    session.invalidate();
                }
                JsonUtil.sendSuccess(resp, "Successfully logged out");
            } else {
                JsonUtil.sendError(resp, "NOT_FOUND", "Endpoint not found", HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (AppException e) {
            JsonUtil.sendError(resp, e.getCode(), e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            JsonUtil.sendError(resp, "INTERNAL_ERROR", "An unexpected error occurred", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
