package com.rublin.rublinmart.dao;

import com.rublin.rublinmart.model.Product;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ProductDAOTest {

    private static ProductDAO productDAO;

    @BeforeAll
    public static void setupDatabase() throws Exception {
        com.zaxxer.hikari.HikariConfig config = new com.zaxxer.hikari.HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:producttaotest;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        config.setDriverClassName("org.h2.Driver");
        com.zaxxer.hikari.HikariDataSource ds = new com.zaxxer.hikari.HikariDataSource(config);

        DBConnection.init(ds);

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS users (id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100), email VARCHAR(150) UNIQUE, password_hash VARCHAR(255), role VARCHAR(20), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            stmt.execute("CREATE TABLE IF NOT EXISTS products (id BIGINT AUTO_INCREMENT PRIMARY KEY, seller_id BIGINT, name VARCHAR(200), description TEXT, price DECIMAL(10,2), stock_qty INT, category VARCHAR(100), image_url VARCHAR(500), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            stmt.execute("CREATE TABLE IF NOT EXISTS reviews (id BIGINT AUTO_INCREMENT PRIMARY KEY, product_id BIGINT, user_id BIGINT, rating INT, comment TEXT, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            stmt.execute("INSERT INTO users (id, name, email, password_hash, role) VALUES (1, 'Seller 1', 'seller@test.com', 'hash', 'SELLER')");
        }

        productDAO = new ProductDAO();
    }

    @Test
    public void testProductCrudOperations() {
        Product product = new Product(null, 1L, "Test Phone", "Smart Phone", BigDecimal.valueOf(499.99), 10, "Electronics", "img.jpg", null);
        Product created = productDAO.createProduct(product);

        assertNotNull(created.getId());
        Optional<Product> found = productDAO.findById(created.getId());
        assertTrue(found.isPresent());
        assertEquals("Test Phone", found.get().getName());

        // Search test
        List<Product> searchResults = productDAO.findAll("Phone", "Electronics");
        assertFalse(searchResults.isEmpty());
        assertEquals(1, searchResults.size());
    }
}
