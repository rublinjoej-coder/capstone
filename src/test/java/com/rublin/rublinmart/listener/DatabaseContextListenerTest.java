package com.rublin.rublinmart.listener;

import com.rublin.rublinmart.dao.DBConnection;
import com.rublin.rublinmart.util.PasswordUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseContextListenerTest {

    @AfterEach
    public void tearDown() {
        DBConnection.close();
        File dbFile = new File("data/rublinmartdb.mv.db");
        File lockFile = new File("data/rublinmartdb.lock.db");
        File traceFile = new File("data/rublinmartdb.trace.db");
        if (dbFile.exists()) dbFile.delete();
        if (lockFile.exists()) lockFile.delete();
        if (traceFile.exists()) traceFile.delete();
    }

    @Test
    public void testDatabaseContextInitializesSchemaAndSeedUsers() throws Exception {
        DatabaseContextListener listener = new DatabaseContextListener();
        listener.contextInitialized(null);

        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM users")) {
                ResultSet rs = ps.executeQuery();
                assertTrue(rs.next());
                assertTrue(rs.getInt(1) >= 6);
            }

            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM products")) {
                ResultSet rs = ps.executeQuery();
                assertTrue(rs.next());
                assertTrue(rs.getInt(1) >= 8);
            }

            try (PreparedStatement ps = conn.prepareStatement("SELECT password_hash FROM users WHERE email = ?")) {
                ps.setString(1, "admin@rublinmart.com");
                ResultSet rs = ps.executeQuery();
                assertTrue(rs.next());
                String hash = rs.getString("password_hash");
                assertTrue(PasswordUtil.checkPassword("Password@123", hash));
            }
        }
    }

    @Test
    public void testDatabaseContextPreservesRegisteredUsersOnReinitialize() throws Exception {
        DatabaseContextListener listener = new DatabaseContextListener();
        listener.contextInitialized(null);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO users (name, email, password_hash, role) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, "Registered User");
            ps.setString(2, "registered@example.com");
            ps.setString(3, "hashed-password");
            ps.setString(4, "BUYER");
            ps.executeUpdate();
        }

        DBConnection.close();
        listener.contextInitialized(null);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT name FROM users WHERE email = ?")) {
            ps.setString(1, "registered@example.com");
            ResultSet rs = ps.executeQuery();
            assertTrue(rs.next());
            assertEquals("Registered User", rs.getString("name"));
        }
    }
}
