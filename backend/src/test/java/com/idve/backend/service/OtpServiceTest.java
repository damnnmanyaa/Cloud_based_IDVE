package com.idve.backend.service;

import com.idve.backend.entity.OtpVerification;
import com.idve.backend.exception.BadRequestException;
import com.idve.backend.repository.OtpVerificationRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private OtpVerificationRepository otpVerificationRepository;

    @Mock
    private ObjectProvider<org.springframework.mail.javamail.JavaMailSender> mailSenderProvider;

    @Mock
    private org.springframework.mail.javamail.JavaMailSender mailSender;

    private OtpService otpService;

    @BeforeEach
    void setUp() {
        when(mailSenderProvider.getIfAvailable()).thenReturn(null);
        otpService = new OtpService(mailSenderProvider, otpVerificationRepository);
        ReflectionTestUtils.setField(otpService, "devFallbackEnabled", true);
        ReflectionTestUtils.setField(otpService, "mailFrom", "test@example.com");
    }

    @Test
    void verifyOtp_ExpiredOTP_ThrowsBadRequestAndDeletesRecord() {
        OtpVerification record = new OtpVerification();
        record.setId(1L);
        record.setEmail("test@example.com");
        record.setOtp("123456");
        record.setExpiresAt(Instant.now().minusSeconds(60)); // expired 60s ago
        record.setCreatedAt(Instant.now().minusSeconds(360));

        when(otpVerificationRepository.findByEmail("test@example.com")).thenReturn(Optional.of(record));
        doNothing().when(otpVerificationRepository).deleteByEmail("test@example.com");

        BadRequestException ex = assertThrows(
            BadRequestException.class,
            () -> otpService.verifyOtp("test@example.com", "123456")
        );

        assertEquals("OTP expired. Please request a new OTP.", ex.getMessage());
        verify(otpVerificationRepository).deleteByEmail("test@example.com");
    }

    @Test
    void verifyOtp_CorrectOTP_SucceedsAndDeletes() {
        OtpVerification record = new OtpVerification();
        record.setId(1L);
        record.setEmail("test@example.com");
        record.setOtp("123456");
        record.setExpiresAt(Instant.now().plusSeconds(240)); // valid for 4 more minutes
        record.setCreatedAt(Instant.now());

        when(otpVerificationRepository.findByEmail("test@example.com")).thenReturn(Optional.of(record));
        doNothing().when(otpVerificationRepository).deleteByEmail("test@example.com");

        otpService.verifyOtp("test@example.com", "123456");

        verify(otpVerificationRepository).deleteByEmail("test@example.com");
    }

    @Test
    void verifyOtp_IncorrectOTP_ThrowsBadRequestWithoutDeleting() {
        OtpVerification record = new OtpVerification();
        record.setId(1L);
        record.setEmail("test@example.com");
        record.setOtp("123456");
        record.setExpiresAt(Instant.now().plusSeconds(240));
        record.setCreatedAt(Instant.now());

        when(otpVerificationRepository.findByEmail("test@example.com")).thenReturn(Optional.of(record));

        BadRequestException ex = assertThrows(
            BadRequestException.class,
            () -> otpService.verifyOtp("test@example.com", "999999")
        );

        assertEquals("OTP is incorrect.", ex.getMessage());
        verify(otpVerificationRepository, never()).deleteByEmail(anyString());
    }

    @Test
    void verifyOtp_NoSavedOTP_ThrowsBadRequest() {
        when(otpVerificationRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        BadRequestException ex = assertThrows(
            BadRequestException.class,
            () -> otpService.verifyOtp("unknown@example.com", "123456")
        );

        assertEquals("OTP not found. Please request a new OTP.", ex.getMessage());
    }
}
