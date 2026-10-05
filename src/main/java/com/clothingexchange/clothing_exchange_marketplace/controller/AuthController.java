package com.clothingexchange.clothing_exchange_marketplace.controller;

import com.clothingexchange.clothing_exchange_marketplace.model.User;
import com.clothingexchange.clothing_exchange_marketplace.service.OtpService;
import com.clothingexchange.clothing_exchange_marketplace.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final OtpService otpService;

    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    public AuthController(
            UserService userService,
            OtpService otpService) {

        this.userService = userService;
        this.otpService = otpService;
    }

    // =====================================================
    // REGISTER
    // =====================================================

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody User user) {

        try {

            User registeredUser =
                    userService.registerUser(user);

            registeredUser.setPassword(null);

            return ResponseEntity.ok(registeredUser);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // =====================================================
    // LOGIN
    // =====================================================

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody User loginUser,
            HttpServletRequest request,
            HttpServletResponse response) {

        if (loginUser.getEmail() == null ||
                loginUser.getEmail().trim().isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body("Email is required");
        }

        if (loginUser.getPassword() == null ||
                loginUser.getPassword().trim().isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body("Password is required");
        }

        String email =
                loginUser.getEmail()
                        .trim()
                        .toLowerCase();

        User user =
                userService.findByEmail(email)
                        .orElse(null);

        if (user == null) {

            return ResponseEntity
                    .badRequest()
                    .body("Invalid email or password");
        }

        boolean passwordMatches =
                userService.verifyPassword(
                        loginUser.getPassword(),
                        user.getPassword()
                );

        if (!passwordMatches) {

            return ResponseEntity
                    .badRequest()
                    .body("Invalid email or password");
        }

        // =================================================
        // DETERMINE USER ROLE
        // =================================================

        String role = user.getRole();

        if (role == null ||
                role.trim().isEmpty()) {

            role = "USER";
        }

        role = role.trim().toUpperCase();

        String springRole = "ROLE_" + role;

        // =================================================
        // CREATE AUTHENTICATION
        // =================================================

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        null,
                        Collections.singletonList(
                                new SimpleGrantedAuthority(
                                        springRole
                                )
                        )
                );

        SecurityContext context =
                SecurityContextHolder
                        .createEmptyContext();

        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);

        // =================================================
        // SAVE SECURITY CONTEXT INTO SESSION
        // =================================================

        securityContextRepository.saveContext(
                context,
                request,
                response
        );

        // Never send password to frontend
        user.setPassword(null);

        return ResponseEntity.ok(user);
    }

    // =====================================================
    // LOGOUT
    // =====================================================

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            HttpServletRequest request,
            HttpServletResponse response) {

        SecurityContextHolder.clearContext();

        if (request.getSession(false) != null) {

            request.getSession(false)
                    .invalidate();
        }

        return ResponseEntity.ok(
                "Logged out successfully"
        );
    }

    // =====================================================
    // CURRENT LOGGED-IN USER
    // =====================================================

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {

        User loggedInUser =
                getAuthenticatedUser();

        if (loggedInUser == null) {

            return ResponseEntity
                    .status(401)
                    .body("Not authenticated");
        }

        loggedInUser.setPassword(null);

        return ResponseEntity.ok(loggedInUser);
    }

    // =====================================================
    // GET PROFILE
    // =====================================================

    @GetMapping("/profile/{id}")
    public ResponseEntity<?> getProfile(
            @PathVariable Long id) {

        User loggedInUser =
                getAuthenticatedUser();

        if (loggedInUser == null) {

            return ResponseEntity
                    .status(401)
                    .body("Not authenticated");
        }

        boolean isAdmin =
                "ADMIN".equalsIgnoreCase(
                        loggedInUser.getRole()
                );

        // Only owner or admin can view profile
        if (!isAdmin &&
                !loggedInUser.getId().equals(id)) {

            return ResponseEntity
                    .status(403)
                    .body(
                            "You are not authorized to view this profile"
                    );
        }

        User user =
                userService.findById(id)
                        .orElse(null);

        if (user == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        user.setPassword(null);

        return ResponseEntity.ok(user);
    }

    // =====================================================
    // UPDATE PROFILE
    // =====================================================

    @PutMapping("/profile/{id}")
    public ResponseEntity<?> updateProfile(
            @PathVariable Long id,
            @RequestBody User user) {

        User loggedInUser =
                getAuthenticatedUser();

        if (loggedInUser == null) {

            return ResponseEntity
                    .status(401)
                    .body("Not authenticated");
        }

        boolean isAdmin =
                "ADMIN".equalsIgnoreCase(
                        loggedInUser.getRole()
                );

        // Only owner or admin can update profile
        if (!isAdmin &&
                !loggedInUser.getId().equals(id)) {

            return ResponseEntity
                    .status(403)
                    .body(
                            "You are not authorized to update this profile"
                    );
        }

        try {

            User updatedUser =
                    userService.updateProfile(
                            id,
                            user
                    );

            updatedUser.setPassword(null);

            return ResponseEntity.ok(updatedUser);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // =====================================================
    // RESET PASSWORD
    // =====================================================

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestParam String email,
            @RequestParam String otp,
            @RequestParam String newPassword) {

        try {

            // -----------------------------
            // Validate email
            // -----------------------------

            if (email == null ||
                    email.trim().isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body("Email is required");
            }

            // -----------------------------
            // Validate OTP
            // -----------------------------

            if (otp == null ||
                    otp.trim().isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body("OTP is required");
            }

            // -----------------------------
            // Validate password
            // -----------------------------

            if (newPassword == null ||
                    newPassword.trim().isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body("New password is required");
            }

            if (newPassword.length() < 6) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                "Password must be at least 6 characters"
                        );
            }

            String normalizedEmail =
                    email.trim().toLowerCase();

            // -----------------------------
            // Verify OTP
            // -----------------------------

            boolean otpValid =
                    otpService.verifyOtp(
                            normalizedEmail,
                            otp
                    );

            if (!otpValid) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                "Invalid or expired OTP."
                        );
            }

            // -----------------------------
            // Reset password
            // -----------------------------

            userService.resetPassword(
                    normalizedEmail,
                    newPassword
            );

            // -----------------------------
            // Prevent OTP reuse
            // -----------------------------

            otpService.markOtpAsUsed(
                    normalizedEmail
            );

            return ResponseEntity.ok(
                    "Password reset successfully."
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // =====================================================
    // HELPER
    // GET AUTHENTICATED USER
    // =====================================================

    private User getAuthenticatedUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            return null;
        }

        if ("anonymousUser".equals(
                authentication.getPrincipal())) {

            return null;
        }

        String email =
                authentication.getName();

        if (email == null ||
                email.trim().isEmpty()) {

            return null;
        }

        return userService
                .findByEmail(
                        email.trim().toLowerCase()
                )
                .orElse(null);
    }
}