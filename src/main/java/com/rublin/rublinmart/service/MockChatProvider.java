package com.rublin.rublinmart.service;

import com.rublin.rublinmart.dao.ProductDAO;
import com.rublin.rublinmart.model.Product;

import java.util.List;

public class MockChatProvider implements ChatProvider {

    private final ProductDAO productDAO = new ProductDAO();

    @Override
    public String getResponse(String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return "Hello! How can I assist you with RublinMart today?";
        }

        String query = userMessage.toLowerCase().trim();

        if (query.contains("laptop") || query.contains("computer") || query.contains("macbook")) {
            List<Product> laptops = productDAO.findAll("laptop", null);
            if (!laptops.isEmpty()) {
                Product p = laptops.get(0);
                return "Yes! We have laptops available such as '" + p.getName() + "' for $" + p.getPrice() + ". You can find it under Electronics!";
            }
            return "We carry great laptops in our Electronics category! Check out our catalog for current stock.";
        }

        if (query.contains("headphone") || query.contains("audio") || query.contains("speaker") || query.contains("sound")) {
            return "We have premium Wireless Noise-Canceling Headphones in Electronics starting from $249.50!";
        }

        if (query.contains("shipping") || query.contains("delivery") || query.contains("ship")) {
            return "RublinMart offers standard 2-4 business day shipping on all orders across registered sellers!";
        }

        if (query.contains("return") || query.contains("refund")) {
            return "Our customer satisfaction guarantee allows returns within 14 days of delivery for a full refund.";
        }

        if (query.contains("payment") || query.contains("pay") || query.contains("card")) {
            return "RublinMart supports instant order checkout with secure payment confirmation!";
        }

        if (query.contains("seller") || query.contains("vendor") || query.contains("sell")) {
            return "You can register a Seller account on RublinMart to list and manage your own products on our platform!";
        }

        if (query.contains("order") || query.contains("track") || query.contains("status")) {
            return "You can view your real-time order history and shipment tracking status anytime in your Buyer Orders page!";
        }

        return "I am RublinMart AI assistant. I can help you search for products like laptops, headphones, sneakers, or answer questions about shipping, orders, and selling!";
    }
}
