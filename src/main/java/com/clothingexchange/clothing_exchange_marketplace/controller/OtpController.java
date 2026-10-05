package com.clothingexchange.clothing_exchange_marketplace.controller;

import com.clothingexchange.clothing_exchange_marketplace.service.OtpService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/otp")
@CrossOrigin(origins = "*")
public class OtpController {

    private final OtpService otpService;

    public OtpController(OtpService otpService) {
        this.otpService = otpService;
    }

    // SEND OTP
    @PostMapping("/send")
    public ResponseEntity<?> sendOtp(
            @RequestParam String email) {

        try {

            if (email == null ||
                    email.trim().isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body("Email is required");
            }

            otpService.sendOtp(email);

            return ResponseEntity.ok(
                    "OTP sent successfully to your email."
            );

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Failed to send OTP: "
                                    + e.getMessage()
                    );
        }
    }

    // VERIFY OTP
    @PostMapping("/verify")
    public ResponseEntity<?> verifyOtp(
            @RequestParam String email,
            @RequestParam String otp) {

        try {

            if (email == null ||
                    email.trim().isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body("Email is required");
            }

            if (otp == null ||
                    otp.trim().isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body("OTP is required");
            }

            boolean valid =
                    otpService.verifyOtp(
                            email,
                            otp
                    );

            if (valid) {

                return ResponseEntity.ok(
                        "OTP verified successfully."
                );
            }

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Invalid or expired OTP."
                    );

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "OTP verification failed: "
                                    + e.getMessage()
                    );
        }
    }

    // CHECK OTP VERIFICATION STATUS
    @GetMapping("/status")
    public ResponseEntity<?> checkOtpStatus(
            @RequestParam String email) {

        try {

            if (email == null ||
                    email.trim().isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body("Email is required");
            }

            boolean verified =
                    otpService.isOtpVerified(email);

            return ResponseEntity.ok(
                    verified
            );

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Unable to check OTP status"
                    );
        }
    }
}