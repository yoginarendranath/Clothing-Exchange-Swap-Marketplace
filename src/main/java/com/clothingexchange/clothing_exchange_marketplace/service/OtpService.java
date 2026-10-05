package com.clothingexchange.clothing_exchange_marketplace.service;

import com.clothingexchange.clothing_exchange_marketplace.model.OtpVerification;
import com.clothingexchange.clothing_exchange_marketplace.repository.OtpVerificationRepository;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class OtpService {

    private final OtpVerificationRepository otpRepository;
    private final JavaMailSender mailSender;

    public OtpService(
            OtpVerificationRepository otpRepository,
            JavaMailSender mailSender) {

        this.otpRepository = otpRepository;
        this.mailSender = mailSender;
    }

    // =========================
    // SEND OTP
    // =========================
    public void sendOtp(String email) {

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Email is required"
            );
        }

        String normalizedEmail =
                email.trim().toLowerCase();

        // Delete previous OTP
        otpRepository.deleteByEmail(normalizedEmail);

        // Generate 6-digit OTP
        String generatedOtp = String.valueOf(
                100000 + new Random().nextInt(900000)
        );

        OtpVerification verification =
                new OtpVerification();

        verification.setEmail(normalizedEmail);
        verification.setOtp(generatedOtp);

        verification.setExpiryTime(
                LocalDateTime.now().plusMinutes(5)
        );

        verification.setVerified(false);
        verification.setUsed(false);

        otpRepository.save(verification);

        // Send email
        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setTo(normalizedEmail);

        message.setSubject(
                "Clothing Exchange Marketplace - Password Reset OTP"
        );

        message.setText(
                "Hello,\n\n"
                        + "Your OTP for password reset is: "
                        + generatedOtp
                        + "\n\n"
                        + "This OTP is valid for 5 minutes.\n\n"
                        + "If you did not request this OTP, "
                        + "please ignore this email.\n\n"
                        + "Clothing Exchange & Swap Marketplace"
        );

        mailSender.send(message);
    }

    // =========================
    // VERIFY OTP
    // =========================
    public boolean verifyOtp(
            String email,
            String otp) {

        if (email == null
                || email.trim().isEmpty()
                || otp == null
                || otp.trim().isEmpty()) {

            return false;
        }

        String normalizedEmail =
                email.trim().toLowerCase();

        String enteredOtp =
                otp.trim();

        return otpRepository
                .findTopByEmailOrderByIdDesc(
                        normalizedEmail
                )
                .map(savedOtp -> {

                    // Already used
                    if (savedOtp.isUsed()) {
                        return false;
                    }

                    // Already verified
                    if (savedOtp.isVerified()) {
                        return true;
                    }

                    // OTP expired
                    if (savedOtp.getExpiryTime()
                            .isBefore(LocalDateTime.now())) {

                        return false;
                    }

                    // OTP does not match
                    if (!savedOtp.getOtp()
                            .equals(enteredOtp)) {

                        return false;
                    }

                    // Mark OTP as verified
                    savedOtp.setVerified(true);

                    otpRepository.save(savedOtp);

                    return true;
                })
                .orElse(false);
    }

    // =========================
    // CHECK WHETHER OTP IS VERIFIED
    // =========================
    public boolean isOtpVerified(String email) {

        if (email == null
                || email.trim().isEmpty()) {

            return false;
        }

        String normalizedEmail =
                email.trim().toLowerCase();

        return otpRepository
                .findTopByEmailOrderByIdDesc(
                        normalizedEmail
                )
                .map(savedOtp -> {

                    // Already used
                    if (savedOtp.isUsed()) {
                        return false;
                    }

                    // Not verified
                    if (!savedOtp.isVerified()) {
                        return false;
                    }

                    // Expired
                    if (savedOtp.getExpiryTime()
                            .isBefore(LocalDateTime.now())) {

                        return false;
                    }

                    return true;
                })
                .orElse(false);
    }

    // =========================
    // MARK OTP AS USED
    // =========================
    public void markOtpAsUsed(String email) {

        if (email == null
                || email.trim().isEmpty()) {

            return;
        }

        String normalizedEmail =
                email.trim().toLowerCase();

        otpRepository
                .findTopByEmailOrderByIdDesc(
                        normalizedEmail
                )
                .ifPresent(savedOtp -> {

                    savedOtp.setUsed(true);

                    otpRepository.save(savedOtp);
                });
    }
}