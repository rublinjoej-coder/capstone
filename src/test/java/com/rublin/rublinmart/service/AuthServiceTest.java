package com.rublin.rublinmart.service;

import com.rublin.rublinmart.dao.UserDAO;
import com.rublin.rublinmart.dto.UserResponseDTO;
import com.rublin.rublinmart.exception.AppException;
import com.rublin.rublinmart.model.User;
import com.rublin.rublinmart.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AuthServiceTest {

    private UserDAO userDAO;
    private AuthService authService;

    @BeforeEach
    public void setUp() {
        userDAO = Mockito.mock(UserDAO.class);
        authService = new AuthService(userDAO);
    }

    @Test
    public void testSuccessfulRegistration() {
        when(userDAO.findByEmail("newbuyer@test.com")).thenReturn(Optional.empty());
        when(userDAO.createUser(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(10L);
            return u;
        });

        UserResponseDTO response = authService.register("New Buyer", "newbuyer@test.com", "Password123", "Password123", "BUYER");

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("BUYER", response.getRole());
        verify(userDAO, times(1)).createUser(any(User.class));
    }

    @Test
    public void testDuplicateEmailRegistrationFails() {
        User existing = new User(1L, "Existing", "exist@test.com", "hash", "BUYER", null);
        when(userDAO.findByEmail("exist@test.com")).thenReturn(Optional.of(existing));

        assertThrows(AppException.class, () -> 
            authService.register("Existing User", "exist@test.com", "Password123", "Password123", "BUYER")
        );
    }

    @Test
    public void testLoginSuccess() {
        String pass = "Password123";
        String hash = PasswordUtil.hashPassword(pass);
        User user = new User(1L, "Buyer", "buyer@test.com", hash, "BUYER", null);

        when(userDAO.findByEmail("buyer@test.com")).thenReturn(Optional.of(user));

        User loggedIn = authService.login("buyer@test.com", pass);
        assertNotNull(loggedIn);
        assertEquals(1L, loggedIn.getId());
    }

    @Test
    public void testLoginInvalidPasswordFails() {
        String hash = PasswordUtil.hashPassword("Password123");
        User user = new User(1L, "Buyer", "buyer@test.com", hash, "BUYER", null);

        when(userDAO.findByEmail("buyer@test.com")).thenReturn(Optional.of(user));

        assertThrows(AppException.class, () -> authService.login("buyer@test.com", "WrongPass"));
    }
}
