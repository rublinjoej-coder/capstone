package com.rublin.rublinmart.dto;

import com.rublin.rublinmart.model.Product;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class ProductResponseDTO {
    private Long id;
    private Long sellerId;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQty;
    private String category;
    private String imageUrl;
    private Timestamp createdAt;
    private String sellerName;
    private Double averageRating;
    private Integer reviewCount;

    public ProductResponseDTO() {}

    public ProductResponseDTO(Product p) {
        if (p != null) {
            this.id = p.getId();
            this.sellerId = p.getSellerId();
            this.name = p.getName();
            this.description = p.getDescription();
            this.price = p.getPrice();
            this.stockQty = p.getStockQty();
            this.category = p.getCategory();
            this.imageUrl = p.getImageUrl();
            this.createdAt = p.getCreatedAt();
            this.sellerName = p.getSellerName();
            this.averageRating = p.getAverageRating() != null ? p.getAverageRating() : 0.0;
            this.reviewCount = p.getReviewCount() != null ? p.getReviewCount() : 0;
        }
    }

    public Long getId() { return id; }
    public Long getSellerId() { return sellerId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public Integer getStockQty() { return stockQty; }
    public String getCategory() { return category; }
    public String getImageUrl() { return imageUrl; }
    public Timestamp getCreatedAt() { return createdAt; }
    public String getSellerName() { return sellerName; }
    public Double getAverageRating() { return averageRating; }
    public Integer getReviewCount() { return reviewCount; }
}
