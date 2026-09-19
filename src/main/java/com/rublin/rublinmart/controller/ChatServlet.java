package com.rublin.rublinmart.controller;

import com.google.gson.JsonObject;
import com.rublin.rublinmart.exception.AppException;
import com.rublin.rublinmart.service.ChatService;
import com.rublin.rublinmart.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/v1/chat")
public class ChatServlet extends HttpServlet {

    private final ChatService chatService = new ChatService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userIp = req.getRemoteAddr();

        try {
            JsonObject body = JsonUtil.readJson(req, JsonObject.class);
            if (body == null || !body.has("message")) {
                throw new AppException("VALIDATION_ERROR", "Message is required");
            }

            String message = body.get("message").getAsString();
            String reply = chatService.processChatMessage(userIp, message);

            JsonObject responseData = new JsonObject();
            responseData.addProperty("reply", reply);

            JsonUtil.sendSuccess(resp, responseData);

        } catch (AppException e) {
            JsonUtil.sendError(resp, e.getCode(), e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            JsonUtil.sendError(resp, "INTERNAL_ERROR", "Error processing chat request", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
