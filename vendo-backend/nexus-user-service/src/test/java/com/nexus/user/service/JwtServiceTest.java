package com.nexus.user.service;

import com.nexus.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .superUser(false)
                .build();
    }

    @Test
    void testGenerateToken() {
        String token = jwtService.generateToken(testUser);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testExtractUsername() {
        String token = jwtService.generateToken(testUser);
        String username = jwtService.extractUsername(token);
        
        assertEquals("test@example.com", username);
    }

    @Test
    void testValidateToken() {
        String token = jwtService.generateToken(testUser);
        boolean isValid = jwtService.validateToken(token, "test@example.com");
        
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_InvalidUser() {
        String token = jwtService.generateToken(testUser);
        boolean isValid = jwtService.validateToken(token, "other@example.com");
        
        assertFalse(isValid);
    }

    @Test
    void testGenerateRefreshToken() {
        String refreshToken = jwtService.generateRefreshToken(testUser);
        
        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());
    }

    @Test
    void testValidateRefreshToken() {
        String refreshToken = jwtService.generateRefreshToken(testUser);
        boolean isValid = jwtService.validateRefreshToken(refreshToken);
        
        assertTrue(isValid);
    }

    @Test
    void testExtractUsernameFromRefreshToken() {
        String refreshToken = jwtService.generateRefreshToken(testUser);
        String username = jwtService.extractUsernameFromRefreshToken(refreshToken);
        
        assertEquals("test@example.com", username);
    }
}
