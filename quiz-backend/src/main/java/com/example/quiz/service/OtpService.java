package com.example.quiz.service;

import com.example.quiz.entity.User;
import com.example.quiz.exception.BadRequestException;
import com.example.quiz.exception.ResourceNotFoundException;
import com.example.quiz.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    // Stores OTP details in memory, mapped by email
    private final ConcurrentMap<String, OtpDetails> otpCache = new ConcurrentHashMap<>();

    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Data
    @AllArgsConstructor
    private static class OtpDetails {
        private String code;
        private LocalDateTime expiryTime;
    }

    /**
     * Generates a 6-digit OTP, stores it, and sends it to the user's email.
     */
    public void generateAndSendOtp(String email) {
        // Verify user exists
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No account found with email: " + email));

        // Generate a 6-digit random number
        String otp = String.format("%06d", SECURE_RANDOM.nextInt(1000000));

        // Store OTP with expiration
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);
        otpCache.put(email, new OtpDetails(otp, expiry));

        log.info("Generated OTP {} for email: {}. Expiration: {}", otp, email, expiry);

        // Prepare email
        String subject = "AssessSphere — Password Reset Verification Code";
        String body = String.format(
                "Hello %s,\n\n" +
                "You have requested to reset your password for your AssessSphere account.\n\n" +
                "Your password reset verification code is:\n" +
                "🔑 %s\n\n" +
                "This OTP is valid for %d minutes. If you did not request this password reset, please ignore this email.\n\n" +
                "Regards,\n" +
                "AssessSphere Support Team",
                user.getName(), otp, OTP_EXPIRY_MINUTES
        );

        // Send email
        emailService.sendEmail(email, subject, body);
    }

    /**
     * Verifies the OTP and updates the password of the user.
     */
    public void verifyOtpAndResetPassword(String email, String otp, String newPassword) {
        // Validate user exists
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No account found with email: " + email));

        // Retrieve and check stored OTP
        OtpDetails details = otpCache.get(email);
        if (details == null) {
            throw new BadRequestException("No active OTP request found for this email address");
        }

        // Check expiry
        if (LocalDateTime.now().isAfter(details.getExpiryTime())) {
            otpCache.remove(email);
            throw new BadRequestException("The verification code has expired. Please request a new one.");
        }

        // Compare OTP codes
        if (!details.getCode().equals(otp.trim())) {
            throw new BadRequestException("Invalid verification code. Please try again.");
        }

        // Success: Reset password with BCrypt hash
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Remove OTP from cache
        otpCache.remove(email);

        log.info("Password reset successful for email: {}", email);

        // Send confirmation email
        String subject = "AssessSphere — Password Reset Confirmation";
        String body = String.format(
                "Hello %s,\n\n" +
                "The password for your AssessSphere account has been successfully reset.\n\n" +
                "If you did not perform this action, please contact support immediately.\n\n" +
                "Regards,\n" +
                "AssessSphere Support Team",
                user.getName()
        );
        try {
            emailService.sendEmail(email, subject, body);
        } catch (Exception e) {
            log.warn("Failed to send password reset confirmation email: {}", e.getMessage());
        }
    }
}
