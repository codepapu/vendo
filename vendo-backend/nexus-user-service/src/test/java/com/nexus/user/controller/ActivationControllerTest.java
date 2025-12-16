package com.nexus.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.user.dto.request.ActivateUserRequest;
import com.nexus.user.entity.Activation;
import com.nexus.user.entity.User;
import com.nexus.user.repository.ActivationRepository;
import com.nexus.user.repository.UserRepository;
import com.nexus.user.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ActivationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivationRepository activationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        activationRepository.deleteAll();

        testUser = User.builder()
                .email("test@example.com")
                .password(passwordEncoder.encode("password123"))
                .firstName("Test")
                .lastName("User")
                .username("testuser")
                .superUser(false)
                .build();
        testUser = userRepository.save(testUser);
    }

    @Test
    void testActivateUser_Success() throws Exception {
        // Create activation code
        Activation activation = Activation.builder()
                .user(testUser)
                .code("123456")
                .completed(false)
                .build();
        activation = activationRepository.save(activation);

        ActivateUserRequest request = new ActivateUserRequest();
        request.setCode("123456");

        mockMvc.perform(post("/api/v1/auth/activation/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testActivateUser_InvalidCode() throws Exception {
        ActivateUserRequest request = new ActivateUserRequest();
        request.setCode("999999");

        mockMvc.perform(post("/api/v1/auth/activation/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testResendActivationCode_Success() throws Exception {
        String accessToken = jwtService.generateToken(testUser);

        mockMvc.perform(post("/api/v1/auth/activation/resend")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
