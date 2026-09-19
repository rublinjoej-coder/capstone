package com.rublin.rublinmart.controller;

import com.rublin.rublinmart.dao.DBConnection;
import com.rublin.rublinmart.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/v1/health")
public class HealthServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT 1");
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                health.put("db", "UP");
            } else {
                health.put("db", "DOWN");
            }
            JsonUtil.sendSuccess(resp, health);
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("db", "DOWN");
            JsonUtil.sendError(resp, "SERVICE_UNAVAILABLE", "Database connection failed", HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
    }
}
