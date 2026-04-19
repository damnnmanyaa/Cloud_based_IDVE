package com.idve.backend.service;

import com.idve.backend.exception.BadRequestException;
import com.idve.backend.entity.OtpVerification;
import com.idve.backend.repository.OtpVerificationRepository;
import java.security.SecureRandom;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    private static final long OTP_EXPIRY_SECONDS = 300; // 5 minutes

    private final SecureRandom random = new SecureRandom();
    private final OtpVerificationRepository otpVerificationRepository;
    private final JavaMailSender mailSender;

    @Value("${app.otp.dev-fallback-enabled:false}")
    private boolean devFallbackEnabled;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    public OtpService(ObjectProvider<JavaMailSender> mailSenderProvider,
                      OtpVerificationRepository otpVerificationRepository) {
        this.mailSender = mailSenderProvider.getIfAvailable();
        this.otpVerificationRepository = otpVerificationRepository;
    }

    @Transactional
    public String sendOtp(String email) {
        String otp = String.format("%06d", random.nextInt(1_000_000));
        String normalizedEmail = email.toLowerCase();
        Instant expiresAt = Instant.now().plusSeconds(OTP_EXPIRY_SECONDS);
        boolean sent = false;

        try {
            sendEmail(email, otp);
            sent = true;
        } catch (BadRequestException ex) {
            if (!devFallbackEnabled) {
                throw ex;
            }
            log.warn("OTP email failed, using dev fallback for {}", normalizedEmail);
        }

        Instant now = Instant.now();
        OtpVerification record = otpVerificationRepository.findByEmail(normalizedEmail)
            .orElseGet(OtpVerification::new);

        record.setEmail(normalizedEmail);
        record.setOtp(otp);
        record.setExpiresAt(expiresAt);
        record.setCreatedAt(now);

        try {
            otpVerificationRepository.save(record);
        } catch (DataIntegrityViolationException ex) {
            OtpVerification existing = otpVerificationRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> ex);
            existing.setOtp(otp);
            existing.setExpiresAt(expiresAt);
            existing.setCreatedAt(now);
            otpVerificationRepository.save(existing);
        }

        return sent ? null : otp;
    }

    @Transactional
    public void verifyOtp(String email, String otp) {
        String normalizedEmail = email.toLowerCase();
        OtpVerification entry = otpVerificationRepository.findByEmail(normalizedEmail)
            .orElse(null);

        if (entry == null) {
            throw new BadRequestException("OTP not found. Please request a new OTP.");
        }

        if (Instant.now().isAfter(entry.getExpiresAt())) {
            otpVerificationRepository.deleteByEmail(normalizedEmail);
            throw new BadRequestException("OTP expired. Please request a new OTP.");
        }

        if (!entry.getOtp().equals(otp)) {
            throw new BadRequestException("OTP is incorrect.");
        }

        // OTP can be used only once.
        otpVerificationRepository.deleteByEmail(normalizedEmail);
    }

    private void sendEmail(String email, String otp) {
        if (mailSender == null) {
            throw new BadRequestException("Email service is not configured. OTP cannot be sent.");
        }

        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            if (mailFrom != null && !mailFrom.isBlank()) {
                msg.setFrom(mailFrom);
            }
            msg.setTo(email);
            msg.setSubject("Your IDVE verification code");
            msg.setText("Your verification code is: " + otp + "\nThis code expires in 5 minutes.");
            mailSender.send(msg);
        } catch (Exception ex) {
            log.error("Unable to send OTP email to {}", email, ex);
            throw new BadRequestException("Unable to send OTP email. Please verify SMTP settings.");
        }
    }
}
