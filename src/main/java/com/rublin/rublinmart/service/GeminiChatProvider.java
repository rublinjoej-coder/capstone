package com.rublin.rublinmart.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GeminiChatProvider implements ChatProvider {

    private static final Logger logger = LoggerFactory.getLogger(GeminiChatProvider.class);
    private final String apiKey;
    private final MockChatProvider fallbackProvider = new MockChatProvider();
    private final Gson gson = new Gson();

    public GeminiChatProvider(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public String getResponse(String userMessage) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            logger.info("Gemini API key not configured, using mock provider");
            return fallbackProvider.getResponse(userMessage);
        }

        try {
            String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(10000);

            JsonObject textPart = new JsonObject();
            textPart.addProperty("text", "You are the RublinMart e-commerce assistant. Answer concisely and only about products, shopping, orders, and e-commerce: " + userMessage);

            JsonArray parts = new JsonArray();
            parts.add(textPart);

            JsonObject contentObj = new JsonObject();
            contentObj.add("parts", parts);

            JsonArray contents = new JsonArray();
            contents.add(contentObj);

            JsonObject requestBody = new JsonObject();
            requestBody.add("contents", contents);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = gson.toJson(requestBody).getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            if (conn.getResponseCode() == 200) {
                try (InputStreamReader isr = new InputStreamReader(conn.getInputStream(), "utf-8")) {
                    JsonObject responseObj = gson.fromJson(isr, JsonObject.class);
                    return responseObj.getAsJsonArray("candidates")
                            .get(0).getAsJsonObject()
                            .getAsJsonObject("content")
                            .getAsJsonArray("parts")
                            .get(0).getAsJsonObject()
                            .get("text").getAsString();
                }
            } else {
                logger.warn("Gemini API returned response code: {}", conn.getResponseCode());
            }
        } catch (Exception e) {
            logger.error("Error communicating with Gemini API, falling back to mock provider", e);
        }

        return fallbackProvider.getResponse(userMessage);
    }
}
