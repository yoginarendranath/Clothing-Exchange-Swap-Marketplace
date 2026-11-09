
package com.clothingexchange.clothing_exchange_marketplace.service;

import com.clothingexchange.clothing_exchange_marketplace.model.OtpVerification;
import com.clothingexchange.clothing_exchange_marketplace.repository.OtpVerificationRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

@Service
public class OtpService {

    private final OtpVerificationRepository otpRepository;
    private final RestClient restClient;
    private final String resendFromEmail;

    private static final SecureRandom RANDOM = new SecureRandom();

    public OtpService(
            OtpVerificationRepository otpRepository,
            RestClient.Builder restClientBuilder,
            @Value("${RESEND_API_KEY}") String resendApiKey,
            @Value("${RESEND_FROM_EMAIL}") String resendFromEmail) {

        this.otpRepository = otpRepository;
        this.resendFromEmail = resendFromEmail;

        this.restClient = restClientBuilder
                .baseUrl("https://api.resend.com")
                .defaultHeader("Authorization", "Bearer " + resendApiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    // SEND OTP
    public void sendOtp(String email) {

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        String normalizedEmail = email.trim().toLowerCase();

        // Delete previous OTP
        otpRepository.deleteByEmail(normalizedEmail);

        // Generate 6-digit OTP
        String generatedOtp = String.valueOf(
                100000 + RANDOM.nextInt(900000)
        );

        OtpVerification verification = new OtpVerification();

        verification.setEmail(normalizedEmail);
        verification.setOtp(generatedOtp);
        verification.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        verification.setVerified(false);
        verification.setUsed(false);

        otpRepository.save(verification);

        // Send OTP using Resend API
        Map<String, Object> emailRequest = Map.of(
                "from", resendFromEmail,
                "to", List.of(normalizedEmail),
                "subject", "Clothing Exchange Marketplace - Password Reset OTP",
                "text",
                "Hello,\n\n"
                        + "Your OTP for password reset is: "
                        + generatedOtp
                        + "\n\nThis OTP is valid for 5 minutes.\n\n"
                        + "If you did not request this OTP, please ignore this email.\n\n"
                        + "Clothing Exchange & Swap Marketplace"
        );

        restClient.post()
                .uri("/emails")
                .body(emailRequest)
                .retrieve()
                .toBodilessEntity();
    }

    // VERIFY OTP
    public boolean verifyOtp(String email, String otp) {

        if (email == null || email.trim().isEmpty()
                || otp == null || otp.trim().isEmpty()) {
            return false;
        }

        String normalizedEmail = email.trim().toLowerCase();
        String enteredOtp = otp.trim();

        return otpRepository
                .findTopByEmailOrderByIdDesc(normalizedEmail)
                .map(savedOtp -> {

                    if (savedOtp.isUsed()) {
                        return false;
                    }

                    if (savedOtp.isVerified()) {
                        return true;
                    }

                    if (savedOtp.getExpiryTime()
                            .isBefore(LocalDateTime.now())) {
                        return false;
                    }

                    if (!savedOtp.getOtp().equals(enteredOtp)) {
                        return false;
                    }

                    savedOtp.setVerified(true);
                    otpRepository.save(savedOtp);

                    return true;
                })
                .orElse(false);
    }

    // CHECK WHETHER OTP IS VERIFIED
    public boolean isOtpVerified(String email) {

        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        String normalizedEmail = email.trim().toLowerCase();

        return otpRepository
                .findTopByEmailOrderByIdDesc(normalizedEmail)
                .map(savedOtp -> {

                    if (savedOtp.isUsed()) {
                        return false;
                    }

                    if (!savedOtp.isVerified()) {
                        return false;
                    }

                    if (savedOtp.getExpiryTime()
                            .isBefore(LocalDateTime.now())) {
                        return false;
                    }

                    return true;
                })
                .orElse(false);
    }

    // MARK OTP AS USED
    public void markOtpAsUsed(String email) {

        if (email == null || email.trim().isEmpty()) {
            return;
        }

        String normalizedEmail = email.trim().toLowerCase();

        otpRepository
                .findTopByEmailOrderByIdDesc(normalizedEmail)
                .ifPresent(savedOtp -> {

                    savedOtp.setUsed(true);
                    otpRepository.save(savedOtp);
                });
    }
}
