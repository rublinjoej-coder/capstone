package com.rublin.rublinmart.dto;

import com.rublin.rublinmart.model.Order;
import com.rublin.rublinmart.model.OrderItem;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

public class OrderResponseDTO {
    private Long id;
    private Long buyerId;
    private String buyerName;
    private String buyerEmail;
    private String status;
    private BigDecimal totalAmount;
    private Timestamp createdAt;
    private List<OrderItem> items;

    public OrderResponseDTO() {}

    public OrderResponseDTO(Order order) {
        if (order != null) {
            this.id = order.getId();
            this.buyerId = order.getBuyerId();
            this.buyerName = order.getBuyerName();
            this.buyerEmail = order.getBuyerEmail();
            this.status = order.getStatus();
            this.totalAmount = order.getTotalAmount();
            this.createdAt = order.getCreatedAt();
            this.items = order.getItems();
        }
    }

    public Long getId() { return id; }
    public Long getBuyerId() { return buyerId; }
    public String getBuyerName() { return buyerName; }
    public String getBuyerEmail() { return buyerEmail; }
    public String getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public Timestamp getCreatedAt() { return createdAt; }
    public List<OrderItem> getItems() { return items; }
}
