package com.rublin.rublinmart.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PasswordUtilTest {

    @Test
    public void testHashPasswordAndCheck() {
        String password = "MySecretPassword123!";
        String hash = PasswordUtil.hashPassword(password);

        assertNotNull(hash);
        assertNotEquals(password, hash);
        assertTrue(PasswordUtil.checkPassword(password, hash));
        assertFalse(PasswordUtil.checkPassword("WrongPassword", hash));
    }

    @Test
    public void testEmptyPasswordThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hashPassword(""));
    }
}
