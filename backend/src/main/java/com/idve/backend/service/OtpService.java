package com.idve.backend.service;

import com.idve.backend.exception.BadRequestException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    private static final long OTP_EXPIRY_SECONDS = 300; // 5 minutes

    private final SecureRandom random = new SecureRandom();
    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();
    private final JavaMailSender mailSender;

    @Value("${app.otp.dev-fallback-enabled:true}")
    private boolean devFallbackEnabled;

    public OtpService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSender = mailSenderProvider.getIfAvailable();
    }

    public String sendOtp(String email) {
        String otp = String.format("%06d", random.nextInt(1_000_000));
        Instant expiresAt = Instant.now().plusSeconds(OTP_EXPIRY_SECONDS);
        boolean sent = false;

        try {
            sendEmail(email, otp);
            sent = true;
        } catch (BadRequestException ex) {
            if (!devFallbackEnabled) {
                throw ex;
            }
            log.warn("OTP email failed, using dev fallback for {}", email);
        }

        otpStore.put(email.toLowerCase(), new OtpEntry(otp, expiresAt));
        return sent ? null : otp;
    }

    public void verifyOtp(String email, String otp) {
        OtpEntry entry = otpStore.get(email.toLowerCase());
        if (entry == null) {
            throw new BadRequestException("OTP not found. Please request a new OTP.");
        }

        if (Instant.now().isAfter(entry.expiresAt())) {
            otpStore.remove(email.toLowerCase());
            throw new BadRequestException("OTP expired. Please request a new OTP.");
        }

        if (!entry.otp().equals(otp)) {
            throw new BadRequestException("OTP is incorrect.");
        }

        // OTP can be used only once.
        otpStore.remove(email.toLowerCase());
    }

    private void sendEmail(String email, String otp) {
        if (mailSender == null) {
            throw new BadRequestException("Email service is not configured. OTP cannot be sent.");
        }

        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(email);
            msg.setSubject("Your IDVE verification code");
            msg.setText("Your verification code is: " + otp + "\\nThis code expires in 5 minutes.");
            mailSender.send(msg);
        } catch (Exception ex) {
            log.error("Unable to send OTP email to {}", email, ex);
            throw new BadRequestException("Unable to send OTP email. Please verify SMTP settings.");
        }
    }

    private record OtpEntry(String otp, Instant expiresAt) {
    }
}
