
package com.clothingexchange.clothing_exchange_marketplace.service;

import com.clothingexchange.clothing_exchange_marketplace.model.OtpVerification;
import com.clothingexchange.clothing_exchange_marketplace.repository.OtpVerificationRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

@Service
public class OtpService {

    private final OtpVerificationRepository otpRepository;
    private final RestClient restClient;

    private final String clientId;
    private final String clientSecret;
    private final String refreshToken;
    private final String senderEmail;

    private static final SecureRandom RANDOM = new SecureRandom();

    public OtpService(
            OtpVerificationRepository otpRepository,
            RestClient.Builder restClientBuilder,
            @Value("${GMAIL_CLIENT_ID}") String clientId,
            @Value("${GMAIL_CLIENT_SECRET}") String clientSecret,
            @Value("${GMAIL_REFRESH_TOKEN}") String refreshToken,
            @Value("${GMAIL_SENDER_EMAIL}") String senderEmail) {

        this.otpRepository = otpRepository;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.refreshToken = refreshToken;
        this.senderEmail = senderEmail;

        this.restClient = restClientBuilder.build();
    }

    // GET GOOGLE ACCESS TOKEN
    private String getAccessToken() {

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();

        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("refresh_token", refreshToken);
        form.add("grant_type", "refresh_token");

        Map<String, Object> response = restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});

        if (response == null || response.get("access_token") == null) {
            throw new IllegalStateException("Google access token not received");
        }

        return response.get("access_token").toString();
    }

    // SEND EMAIL USING GMAIL API
    private void sendEmail(String to, String subject, String message) {

        String accessToken = getAccessToken();

        String encodedBody = Base64.getMimeEncoder(
                76, "\r\n".getBytes(StandardCharsets.UTF_8)
        ).encodeToString(message.getBytes(StandardCharsets.UTF_8));

        String mimeMessage =
                "From: " + senderEmail + "\r\n" +
                "To: " + to + "\r\n" +
                "Subject: " + subject + "\r\n" +
                "MIME-Version: 1.0\r\n" +
                "Content-Type: text/plain; charset=UTF-8\r\n" +
                "Content-Transfer-Encoding: base64\r\n\r\n" +
                encodedBody;

        String raw = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(mimeMessage.getBytes(StandardCharsets.UTF_8));

        restClient.post()
                .uri("https://gmail.googleapis.com/gmail/v1/users/me/messages/send")
                .headers(headers -> headers.setBearerAuth(accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("raw", raw))
                .retrieve()
                .toBodilessEntity();
    }

    // SEND OTP
    public void sendOtp(String email) {

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        String normalizedEmail = email.trim().toLowerCase();

        otpRepository.deleteByEmail(normalizedEmail);

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

        sendEmail(
                normalizedEmail,
                "Clothing Exchange Marketplace - Password Reset OTP",
                "Hello,\n\nYour OTP for password reset is: "
                        + generatedOtp
                        + "\n\nThis OTP is valid for 5 minutes."
                        + "\n\nIf you did not request this OTP, please ignore this email."
                        + "\n\nClothing Exchange & Swap Marketplace"
        );
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
