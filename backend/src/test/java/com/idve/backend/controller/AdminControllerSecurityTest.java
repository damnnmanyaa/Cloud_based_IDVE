package com.idve.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idve.backend.entity.OtpVerification;
import com.idve.backend.repository.OtpVerificationRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OtpVerificationRepository otpVerificationRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanAccessUserList() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanApproveUser() throws Exception {
        mockMvc.perform(patch("/api/admin/users/999/approve"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void nonAdminGets403OnUserList() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void nonAdminGets403OnApprove() throws Exception {
        mockMvc.perform(patch("/api/admin/users/1/approve"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void nonAdminGets403OnReject() throws Exception {
        mockMvc.perform(patch("/api/admin/users/1/reject"))
            .andExpect(status().isForbidden());
    }

    @Test
    void registerSendOtpVerifyOtp_Success() throws Exception {
        // Step 1: send OTP
        MvcResult otpResult = mockMvc.perform(post("/api/auth/send-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"flowtest@example.com\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.otp").isNotEmpty())
            .andReturn();

        String otp = extractOtp(otpResult);

        // Step 2: verify OTP and register
        mockMvc.perform(post("/api/auth/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new VerifyOtpPayload("Test User", "flowtest@example.com", "Password123!", "USER", otp)
                )))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void verifyOtp_ExpiredOTP_Returns400() throws Exception {
        // Step 1: send OTP
        mockMvc.perform(post("/api/auth/send-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"expiretest@example.com\"}"))
            .andExpect(status().isOk());

        // Step 2: force expiry in the database
        OtpVerification record = otpVerificationRepository.findByEmail("expiretest@example.com")
            .orElseThrow(() -> new AssertionError("OTP record not found"));
        record.setExpiresAt(Instant.now().minusSeconds(60));
        otpVerificationRepository.save(record);

        // Step 3: attempt verification — should be rejected as expired
        mockMvc.perform(post("/api/auth/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new VerifyOtpPayload("Test User", "expiretest@example.com", "Password123!", "USER", "123456")
                )))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("OTP expired. Please request a new OTP."));
    }

    private String extractOtp(MvcResult result) throws Exception {
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("otp").asText();
    }

    private record VerifyOtpPayload(
        String name,
        String email,
        String password,
        String role,
        String otp
    ) {}
}
