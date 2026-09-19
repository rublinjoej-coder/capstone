package com.rublin.rublinmart.model;

import java.math.BigDecimal;

public class CartItem {
    private Long id;
    private Long userId;
    private Long productId;
    private Integer quantity;

    // Display fields joined from Product
    private String productName;
    private BigDecimal productPrice;
    private Integer stockQty;
    private String imageUrl;
    private String category;
    private Long sellerId;

    public CartItem() {}

    public CartItem(Long id, Long userId, Long productId, Integer quantity) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public BigDecimal getProductPrice() { return productPrice; }
    public void setProductPrice(BigDecimal productPrice) { this.productPrice = productPrice; }

    public Integer getStockQty() { return stockQty; }
    public void setStockQty(Integer stockQty) { this.stockQty = stockQty; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }

    public BigDecimal getSubtotal() {
        if (productPrice == null || quantity == null) return BigDecimal.ZERO;
        return productPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
