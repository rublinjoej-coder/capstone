package com.rublin.rublinmart.service;

import com.rublin.rublinmart.exception.AppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    private final ChatProvider chatProvider;
    private final Map<String, Long> rateLimiter = new ConcurrentHashMap<>();

    public ChatService() {
        Properties props = new Properties();
        try (InputStream is = ChatService.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (Exception e) {
            logger.error("Failed to load application properties for ChatService", e);
        }

        String providerType = props.getProperty("ai.chatbot.provider", "mock");
        String geminiApiKey = props.getProperty("gemini.api.key", System.getenv("GEMINI_API_KEY"));

        if ("gemini".equalsIgnoreCase(providerType) && geminiApiKey != null && !geminiApiKey.trim().isEmpty()) {
            this.chatProvider = new GeminiChatProvider(geminiApiKey.trim());
        } else {
            this.chatProvider = new MockChatProvider();
        }
    }

    public ChatService(ChatProvider chatProvider) {
        this.chatProvider = chatProvider;
    }

    public String processChatMessage(String userIp, String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new AppException("VALIDATION_ERROR", "Message cannot be empty");
        }

        if (message.length() > 500) {
            throw new AppException("VALIDATION_ERROR", "Message exceeds maximum length of 500 characters");
        }

        // Rate limiting: 1 message per 2 seconds per IP
        long now = System.currentTimeMillis();
        Long lastTime = rateLimiter.get(userIp);
        if (lastTime != null && (now - lastTime) < 2000) {
            throw new AppException("RATE_LIMITED", "Please wait a moment before sending another message.", 429);
        }
        rateLimiter.put(userIp, now);

        try {
            return chatProvider.getResponse(message);
        } catch (Exception e) {
            logger.error("Error generating chat response", e);
            return "I'm having trouble processing your request right now. Please browse our product catalog or contact customer support.";
        }
    }
}
