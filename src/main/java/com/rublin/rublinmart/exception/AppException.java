package com.rublin.rublinmart.exception;

public class AppException extends RuntimeException {
    private final String code;
    private final int statusCode;

    public AppException(String message) {
        this("BAD_REQUEST", message, 400);
    }

    public AppException(String code, String message) {
        this(code, message, 400);
    }

    public AppException(String code, String message, int statusCode) {
        super(message);
        this.code = code;
        this.statusCode = statusCode;
    }

    public String getCode() { return code; }
    public int getStatusCode() { return statusCode; }
}
