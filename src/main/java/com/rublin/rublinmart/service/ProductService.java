package com.rublin.rublinmart.service;

import com.rublin.rublinmart.dao.ProductDAO;
import com.rublin.rublinmart.dto.ProductResponseDTO;
import com.rublin.rublinmart.exception.AppException;
import com.rublin.rublinmart.model.Product;
import com.rublin.rublinmart.util.ValidationUtil;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class ProductService {

    private final ProductDAO productDAO;

    public ProductService() {
        this.productDAO = new ProductDAO();
    }

    public ProductService(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    public ProductResponseDTO addProduct(Long sellerId, String name, String description,
                                         BigDecimal price, Integer stockQty, String category, String imageUrl) {
        ValidationUtil.validateRequired(name, "Product name");
        ValidationUtil.validateRequired(description, "Description");
        ValidationUtil.validatePrice(price);
        ValidationUtil.validateStock(stockQty);
        ValidationUtil.validateRequired(category, "Category");

        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            imageUrl = "https://images.unsplash.com/photo-1524758631624-e2822e304c36?w=500";
        }

        Product product = new Product(null, sellerId, name.trim(), description.trim(), price, stockQty, category.trim(), imageUrl.trim(), null);
        Product savedProduct = productDAO.createProduct(product);
        return getProductDetails(savedProduct.getId());
    }

    public ProductResponseDTO updateProduct(Long productId, Long sellerId, String name, String description,
                                            BigDecimal price, Integer stockQty, String category, String imageUrl) {
        ValidationUtil.validateRequired(name, "Product name");
        ValidationUtil.validateRequired(description, "Description");
        ValidationUtil.validatePrice(price);
        ValidationUtil.validateStock(stockQty);
        ValidationUtil.validateRequired(category, "Category");

        Product existing = productDAO.findById(productId)
                .orElseThrow(() -> new AppException("NOT_FOUND", "Product not found", 404));

        if (!existing.getSellerId().equals(sellerId)) {
            throw new AppException("FORBIDDEN", "You can only update your own products", 403);
        }

        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            imageUrl = existing.getImageUrl();
        }

        existing.setName(name.trim());
        existing.setDescription(description.trim());
        existing.setPrice(price);
        existing.setStockQty(stockQty);
        existing.setCategory(category.trim());
        existing.setImageUrl(imageUrl.trim());

        productDAO.updateProduct(existing);
        return getProductDetails(productId);
    }

    public void deleteProductBySeller(Long productId, Long sellerId) {
        Product existing = productDAO.findById(productId)
                .orElseThrow(() -> new AppException("NOT_FOUND", "Product not found", 404));

        if (!existing.getSellerId().equals(sellerId)) {
            throw new AppException("FORBIDDEN", "You can only delete your own products", 403);
        }

        productDAO.deleteProductBySeller(productId, sellerId);
    }

    public void deleteProductByAdmin(Long productId) {
        Product existing = productDAO.findById(productId)
                .orElseThrow(() -> new AppException("NOT_FOUND", "Product not found", 404));

        productDAO.deleteProduct(existing.getId());
    }

    public ProductResponseDTO getProductDetails(Long productId) {
        Product p = productDAO.findById(productId)
                .orElseThrow(() -> new AppException("NOT_FOUND", "Product not found", 404));
        return new ProductResponseDTO(p);
    }

    public List<ProductResponseDTO> searchProducts(String search, String category) {
        return productDAO.findAll(search, category).stream()
                .map(ProductResponseDTO::new)
                .collect(Collectors.toList());
    }

    public List<ProductResponseDTO> getSellerProducts(Long sellerId) {
        return productDAO.findBySellerId(sellerId).stream()
                .map(ProductResponseDTO::new)
                .collect(Collectors.toList());
    }

    public List<String> getCategories() {
        return productDAO.getCategories();
    }
}
