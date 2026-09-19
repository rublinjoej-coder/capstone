package com.rublin.rublinmart.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class JsonUtil {

    private static final Gson GSON = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .serializeNulls()
            .create();

    private JsonUtil() {}

    public static <T> T readJson(HttpServletRequest request, Class<T> clazz) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        if (sb.length() == 0) {
            return null;
        }
        return GSON.fromJson(sb.toString(), clazz);
    }

    public static void sendSuccess(HttpServletResponse response, Object data) throws IOException {
        sendSuccess(response, data, HttpServletResponse.SC_OK);
    }

    public static void sendSuccess(HttpServletResponse response, Object data, int statusCode) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(statusCode);

        JsonObject root = new JsonObject();
        root.addProperty("success", true);
        root.add("data", GSON.toJsonTree(data));
        root.add("error", null);

        try (PrintWriter writer = response.getWriter()) {
            writer.print(GSON.toJson(root));
            writer.flush();
        }
    }

    public static void sendError(HttpServletResponse response, String code, String message, int statusCode) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(statusCode);

        JsonObject root = new JsonObject();
        root.addProperty("success", false);
        root.add("data", null);

        JsonObject errorObj = new JsonObject();
        errorObj.addProperty("code", code);
        errorObj.addProperty("message", message);
        root.add("error", errorObj);

        try (PrintWriter writer = response.getWriter()) {
            writer.print(GSON.toJson(root));
            writer.flush();
        }
    }

    public static Gson getGson() {
        return GSON;
    }
}
