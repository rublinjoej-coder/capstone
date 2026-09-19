package com.rublin.rublinmart.util;

import com.rublin.rublinmart.exception.AppException;
import java.math.BigDecimal;
import java.util.regex.Pattern;

public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private ValidationUtil() {}

    public static void validateEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new AppException("VALIDATION_ERROR", "Invalid email format");
        }
    }

    public static void validateRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new AppException("VALIDATION_ERROR", fieldName + " is required");
        }
    }

    public static void validateRole(String role) {
        if (role == null || (!role.equalsIgnoreCase("BUYER") && !role.equalsIgnoreCase("SELLER") && !role.equalsIgnoreCase("ADMIN"))) {
            throw new AppException("VALIDATION_ERROR", "Invalid role specified");
        }
    }

    public static void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException("VALIDATION_ERROR", "Price must be greater than 0");
        }
    }

    public static void validateStock(Integer stock) {
        if (stock == null || stock < 0) {
            throw new AppException("VALIDATION_ERROR", "Stock quantity must be 0 or greater");
        }
    }

    public static void validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new AppException("VALIDATION_ERROR", "Rating must be between 1 and 5");
        }
    }

    public static String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#x27;");
    }
}
