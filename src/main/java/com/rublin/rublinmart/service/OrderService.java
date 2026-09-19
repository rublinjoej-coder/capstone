package com.rublin.rublinmart.service;

import com.rublin.rublinmart.dao.CartDAO;
import com.rublin.rublinmart.dao.OrderDAO;
import com.rublin.rublinmart.dao.ProductDAO;
import com.rublin.rublinmart.dto.OrderResponseDTO;
import com.rublin.rublinmart.exception.AppException;
import com.rublin.rublinmart.model.CartItem;
import com.rublin.rublinmart.model.Order;
import com.rublin.rublinmart.model.OrderItem;
import com.rublin.rublinmart.model.Product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OrderService {

    private final OrderDAO orderDAO;
    private final CartDAO cartDAO;
    private final ProductDAO productDAO;

    public OrderService() {
        this.orderDAO = new OrderDAO();
        this.cartDAO = new CartDAO();
        this.productDAO = new ProductDAO();
    }

    public OrderService(OrderDAO orderDAO, CartDAO cartDAO, ProductDAO productDAO) {
        this.orderDAO = orderDAO;
        this.cartDAO = cartDAO;
        this.productDAO = productDAO;
    }

    public OrderResponseDTO placeOrder(Long buyerId) {
        List<CartItem> cartItems = cartDAO.getCartItems(buyerId);
        if (cartItems.isEmpty()) {
            throw new AppException("EMPTY_CART", "Cannot place an order with an empty shopping cart.");
        }

        BigDecimal serverCalculatedTotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            Product product = productDAO.findById(cartItem.getProductId())
                    .orElseThrow(() -> new AppException("NOT_FOUND", "Product ID " + cartItem.getProductId() + " no longer exists.", 404));

            if (product.getStockQty() < cartItem.getQuantity()) {
                throw new AppException("OUT_OF_STOCK", "Product '" + product.getName() + "' has only " + product.getStockQty() + " left in stock.");
            }

            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            serverCalculatedTotal = serverCalculatedTotal.add(itemTotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getImageUrl());
            orderItems.add(orderItem);
        }

        Order order = new Order();
        order.setBuyerId(buyerId);
        order.setStatus("CONFIRMED"); // Payment mocked & confirmed
        order.setTotalAmount(serverCalculatedTotal);

        Order placedOrder = orderDAO.createOrderTransactional(order, orderItems);
        return getOrderDetails(placedOrder.getId(), buyerId, "BUYER");
    }

    public OrderResponseDTO getOrderDetails(Long orderId, Long userId, String userRole) {
        Order order = orderDAO.findById(orderId)
                .orElseThrow(() -> new AppException("NOT_FOUND", "Order not found", 404));

        if ("BUYER".equalsIgnoreCase(userRole) && !order.getBuyerId().equals(userId)) {
            throw new AppException("FORBIDDEN", "Access denied for this order", 403);
        }

        if ("SELLER".equalsIgnoreCase(userRole)) {
            // Filter items to only show seller's products
            List<OrderItem> sellerItems = order.getItems().stream()
                    .filter(item -> userId.equals(item.getSellerId()))
                    .collect(Collectors.toList());

            if (sellerItems.isEmpty()) {
                throw new AppException("FORBIDDEN", "Access denied for this order", 403);
            }
        }

        return new OrderResponseDTO(order);
    }

    public List<OrderResponseDTO> getBuyerOrders(Long buyerId) {
        return orderDAO.findByBuyerId(buyerId).stream()
                .map(OrderResponseDTO::new)
                .collect(Collectors.toList());
    }

    public List<OrderResponseDTO> getSellerOrders(Long sellerId) {
        return orderDAO.findBySellerId(sellerId).stream()
                .map(OrderResponseDTO::new)
                .collect(Collectors.toList());
    }

    public List<OrderResponseDTO> getAllOrdersForAdmin() {
        return orderDAO.findAll().stream()
                .map(OrderResponseDTO::new)
                .collect(Collectors.toList());
    }

    public void updateOrderStatus(Long orderId, String status) {
        if (!List.of("PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED").contains(status.toUpperCase())) {
            throw new AppException("VALIDATION_ERROR", "Invalid order status specified");
        }
        boolean updated = orderDAO.updateOrderStatus(orderId, status);
        if (!updated) {
            throw new AppException("NOT_FOUND", "Order not found", 404);
        }
    }
}
