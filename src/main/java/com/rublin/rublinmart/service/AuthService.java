package com.rublin.rublinmart.service;

import com.rublin.rublinmart.dao.UserDAO;
import com.rublin.rublinmart.dto.UserResponseDTO;
import com.rublin.rublinmart.exception.AppException;
import com.rublin.rublinmart.model.User;
import com.rublin.rublinmart.util.PasswordUtil;
import com.rublin.rublinmart.util.ValidationUtil;

import java.util.Optional;

public class AuthService {

    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public UserResponseDTO register(String name, String email, String password, String confirmPassword, String role) {
        ValidationUtil.validateRequired(name, "Name");
        ValidationUtil.validateRequired(email, "Email");
        ValidationUtil.validateEmail(email);
        ValidationUtil.validateRequired(password, "Password");
        ValidationUtil.validateRequired(confirmPassword, "Password confirmation");
        ValidationUtil.validateRole(role);

        if ("ADMIN".equalsIgnoreCase(role)) {
            throw new AppException("FORBIDDEN", "Admin accounts cannot be registered publicly.", 403);
        }

        if (!password.equals(confirmPassword)) {
            throw new AppException("VALIDATION_ERROR", "Passwords do not match");
        }

        if (password.length() < 6) {
            throw new AppException("VALIDATION_ERROR", "Password must be at least 6 characters long");
        }

        if (userDAO.findByEmail(email).isPresent()) {
            throw new AppException("EMAIL_EXISTS", "Email address is already registered", 409);
        }

        String passwordHash = PasswordUtil.hashPassword(password);
        User newUser = new User(null, name.trim(), email.trim(), passwordHash, role.toUpperCase(), null);
        User savedUser = userDAO.createUser(newUser);

        return new UserResponseDTO(savedUser);
    }

    public User login(String email, String password) {
        ValidationUtil.validateRequired(email, "Email");
        ValidationUtil.validateEmail(email);
        ValidationUtil.validateRequired(password, "Password");

        Optional<User> userOpt = userDAO.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new AppException("UNAUTHORIZED", "Invalid email or password", 401);
        }

        User user = userOpt.get();
        if (!PasswordUtil.checkPassword(password, user.getPasswordHash())) {
            throw new AppException("UNAUTHORIZED", "Invalid email or password", 401);
        }

        return user;
    }

    public UserResponseDTO getUserProfile(Long userId) {
        User user = userDAO.findById(userId)
                .orElseThrow(() -> new AppException("NOT_FOUND", "User not found", 404));
        return new UserResponseDTO(user);
    }
}
