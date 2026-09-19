package com.rublin.rublinmart.dao;

import com.rublin.rublinmart.model.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class UserDAOTest {

    private static UserDAO userDAO;

    @BeforeAll
    public static void setupDatabase() throws Exception {
        // Init H2 in-memory DB for DAO testing
        com.zaxxer.hikari.HikariConfig config = new com.zaxxer.hikari.HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:usertaotest;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        config.setDriverClassName("org.h2.Driver");
        com.zaxxer.hikari.HikariDataSource ds = new com.zaxxer.hikari.HikariDataSource(config);

        DBConnection.init(ds);

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS users (id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100), email VARCHAR(150) UNIQUE, password_hash VARCHAR(255), role VARCHAR(20), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        }

        userDAO = new UserDAO();
    }

    @Test
    public void testCreateAndFindUser() {
        User user = new User(null, "Test User", "testuser@rublinmart.com", "hashedpass", "BUYER", null);
        User created = userDAO.createUser(user);

        assertNotNull(created.getId());
        Optional<User> found = userDAO.findByEmail("testuser@rublinmart.com");
        assertTrue(found.isPresent());
        assertEquals("Test User", found.get().getName());
        assertEquals("BUYER", found.get().getRole());
    }
}
