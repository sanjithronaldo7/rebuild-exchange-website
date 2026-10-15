package com.rebuildexchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebuildexchange.dto.LoginRequest;
import com.rebuildexchange.dto.RegisterRequest;
import com.rebuildexchange.dto.VerifyOtpRequest;
import com.rebuildexchange.entity.OtpVerification;
import com.rebuildexchange.entity.Role;
import com.rebuildexchange.entity.User;
import com.rebuildexchange.repository.OtpVerificationRepository;
import com.rebuildexchange.repository.RoleRepository;
import com.rebuildexchange.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private OtpVerificationRepository otpRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setupRoles() {
        if (roleRepository.findByName("ROLE_BUYER").isEmpty()) {
            roleRepository.save(new Role("ROLE_BUYER", "Registered Buyer"));
        }
        if (roleRepository.findByName("ROLE_SELLER").isEmpty()) {
            roleRepository.save(new Role("ROLE_SELLER", "Verified Seller"));
        }
        if (roleRepository.findByName("ROLE_ADMIN").isEmpty()) {
            roleRepository.save(new Role("ROLE_ADMIN", "Administrator"));
        }
    }

    @Test
    @DisplayName("Should register new user and initiate OTP verification")
    void testRegisterSuccess() throws Exception {
        RegisterRequest req = new RegisterRequest(
                "Anita Desai", "anita.test@example.com", "+91 9988776655", "Secret@123", "ROLE_BUYER"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.requiresOtp", is(true)))
                .andExpect(jsonPath("$.data.email", is("anita.test@example.com")));

        // Verify password is encrypted with BCrypt
        User savedUser = userRepository.findByEmail("anita.test@example.com").orElseThrow();
        assertTrue(passwordEncoder.matches("Secret@123", savedUser.getPasswordHash()));
    }

    @Test
    @DisplayName("Should reject duplicate email registration with 400 Bad Request")
    void testDuplicateEmailRegistration() throws Exception {
        User existing = new User("Existing User", "duplicate@example.com", passwordEncoder.encode("Pass@123"), "+91 9123456789");
        userRepository.save(existing);

        RegisterRequest req = new RegisterRequest(
                "Another User", "duplicate@example.com", "+91 9999999999", "Pass@1234", "ROLE_BUYER"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    @DisplayName("Should prevent user from self-assigning ROLE_ADMIN")
    void testPreventAdminSelfRegistration() throws Exception {
        RegisterRequest req = new RegisterRequest(
                "Hacker User", "hacker@example.com", "+91 9999999999", "AdminPass@123", "ROLE_ADMIN"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("strictly forbidden")));
    }

    @Test
    @DisplayName("Should authenticate valid credentials and return JWT access token")
    void testLoginSuccess() throws Exception {
        User user = new User("Rahul Sharma", "rahul.login@example.com", passwordEncoder.encode("CorrectPass@123"), "+91 9876500000");
        user.getRoles().add(roleRepository.findByName("ROLE_BUYER").orElseThrow());
        userRepository.save(user);

        LoginRequest req = new LoginRequest("rahul.login@example.com", "CorrectPass@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andExpect(jsonPath("$.data.email", is("rahul.login@example.com")));
    }

    @Test
    @DisplayName("Should verify valid OTP and return JWT token")
    void testVerifyOtpSuccess() throws Exception {
        String testEmail = "otp.verify@example.com";
        User user = new User("Pooja Patel", testEmail, passwordEncoder.encode("Password@123"), "+91 9888877777");
        userRepository.save(user);

        OtpVerification otp = new OtpVerification(testEmail, "654321", "REGISTRATION", LocalDateTime.now().plusMinutes(10));
        otpRepository.save(otp);

        VerifyOtpRequest req = new VerifyOtpRequest(testEmail, "654321", "REGISTRATION");

        mockMvc.perform(post("/api/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.accessToken", notNullValue()));

        User verifiedUser = userRepository.findByEmail(testEmail).orElseThrow();
        assertTrue(verifiedUser.getIsEmailVerified());
    }
}
