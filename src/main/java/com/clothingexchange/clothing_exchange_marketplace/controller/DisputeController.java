package com.clothingexchange.clothing_exchange_marketplace.controller;

import com.clothingexchange.clothing_exchange_marketplace.model.Dispute;
import com.clothingexchange.clothing_exchange_marketplace.model.User;
import com.clothingexchange.clothing_exchange_marketplace.repository.DisputeRepository;
import com.clothingexchange.clothing_exchange_marketplace.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/disputes")
@CrossOrigin(origins = "*")
public class DisputeController {

    private final DisputeRepository disputeRepository;
    private final UserService userService;

    public DisputeController(
            DisputeRepository disputeRepository,
            UserService userService) {

        this.disputeRepository = disputeRepository;
        this.userService = userService;
    }

    // =========================
    // CREATE DISPUTE
    // =========================

    @PostMapping
    public ResponseEntity<?> createDispute(
            @RequestBody Dispute dispute) {

        try {

            User loggedInUser =
                    getAuthenticatedUser();

            if (loggedInUser == null) {
                return ResponseEntity.status(401)
                        .body("Not authenticated");
            }

            if (dispute == null) {
                return ResponseEntity.badRequest()
                        .body("Dispute data is required");
            }

            if (dispute.getSwapRequestId() == null) {
                return ResponseEntity.badRequest()
                        .body("Swap request ID is required");
            }

            if (dispute.getSubject() == null ||
                    dispute.getSubject().trim().isEmpty()) {

                return ResponseEntity.badRequest()
                        .body("Subject is required");
            }

            if (dispute.getDescription() == null ||
                    dispute.getDescription().trim().isEmpty()) {

                return ResponseEntity.badRequest()
                        .body("Description is required");
            }

            // Never trust reporterId from frontend
            dispute.setReporterId(
                    loggedInUser.getId()
            );

            dispute.setSubject(
                    dispute.getSubject().trim()
            );

            dispute.setDescription(
                    dispute.getDescription().trim()
            );

            dispute.setStatus("OPEN");
            dispute.setCreatedAt(LocalDateTime.now());
            dispute.setResolvedAt(null);

            return ResponseEntity.ok(
                    disputeRepository.save(dispute)
            );

        } catch (Exception e) {

            return ResponseEntity.internalServerError()
                    .body("Unable to create dispute.");
        }
    }

    // =========================
    // GET ALL DISPUTES
    // ADMIN ONLY
    // =========================

    @GetMapping
    public ResponseEntity<?> getAllDisputes() {

        User loggedInUser =
                getAuthenticatedUser();

        if (loggedInUser == null) {
            return ResponseEntity.status(401)
                    .body("Not authenticated");
        }

        if (!isAdmin(loggedInUser)) {
            return ResponseEntity.status(403)
                    .body("Only administrators can view all disputes");
        }

        return ResponseEntity.ok(
                disputeRepository.findAll()
        );
    }

    // =========================
    // GET USER DISPUTES
    // =========================

    @GetMapping("/reporter/{reporterId}")
    public ResponseEntity<?> getUserDisputes(
            @PathVariable Long reporterId) {

        User loggedInUser =
                getAuthenticatedUser();

        if (loggedInUser == null) {
            return ResponseEntity.status(401)
                    .body("Not authenticated");
        }

        boolean admin =
                isAdmin(loggedInUser);

        // Normal user can only see own disputes
        if (!admin &&
                !loggedInUser.getId().equals(reporterId)) {

            return ResponseEntity.status(403)
                    .body("You are not authorized to view these disputes");
        }

        List<Dispute> disputes =
                disputeRepository.findByReporterId(
                        reporterId
                );

        return ResponseEntity.ok(disputes);
    }

    // =========================
    // GET BY STATUS
    // ADMIN ONLY
    // =========================

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getByStatus(
            @PathVariable String status) {

        User loggedInUser =
                getAuthenticatedUser();

        if (loggedInUser == null) {
            return ResponseEntity.status(401)
                    .body("Not authenticated");
        }

        if (!isAdmin(loggedInUser)) {
            return ResponseEntity.status(403)
                    .body("Only administrators can view disputes by status");
        }

        if (status == null ||
                status.trim().isEmpty()) {

            return ResponseEntity.badRequest()
                    .body("Status is required");
        }

        String normalizedStatus =
                status.trim().toUpperCase();

        if (!normalizedStatus.equals("OPEN") &&
                !normalizedStatus.equals("RESOLVED") &&
                !normalizedStatus.equals("REJECTED")) {

            return ResponseEntity.badRequest()
                    .body(
                            "Status must be OPEN, RESOLVED or REJECTED"
                    );
        }

        return ResponseEntity.ok(
                disputeRepository.findByStatus(
                        normalizedStatus
                )
        );
    }

    // =========================
    // UPDATE STATUS
    // ADMIN ONLY
    // =========================

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        User loggedInUser =
                getAuthenticatedUser();

        if (loggedInUser == null) {
            return ResponseEntity.status(401)
                    .body("Not authenticated");
        }

        if (!isAdmin(loggedInUser)) {
            return ResponseEntity.status(403)
                    .body("Only administrators can update dispute status");
        }

        if (status == null ||
                status.trim().isEmpty()) {

            return ResponseEntity.badRequest()
                    .body("Status is required");
        }

        Dispute dispute =
                disputeRepository.findById(id)
                        .orElse(null);

        if (dispute == null) {
            return ResponseEntity.notFound().build();
        }

        String newStatus =
                status.trim().toUpperCase();

        if (!newStatus.equals("OPEN") &&
                !newStatus.equals("RESOLVED") &&
                !newStatus.equals("REJECTED")) {

            return ResponseEntity.badRequest()
                    .body(
                            "Status must be OPEN, RESOLVED or REJECTED"
                    );
        }

        dispute.setStatus(newStatus);

        if (newStatus.equals("RESOLVED") ||
                newStatus.equals("REJECTED")) {

            dispute.setResolvedAt(
                    LocalDateTime.now()
            );

        } else {

            dispute.setResolvedAt(null);
        }

        return ResponseEntity.ok(
                disputeRepository.save(dispute)
        );
    }

    // =========================
    // DELETE DISPUTE
    // ADMIN ONLY
    // =========================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDispute(
            @PathVariable Long id) {

        User loggedInUser =
                getAuthenticatedUser();

        if (loggedInUser == null) {
            return ResponseEntity.status(401)
                    .body("Not authenticated");
        }

        if (!isAdmin(loggedInUser)) {
            return ResponseEntity.status(403)
                    .body("Only administrators can delete disputes");
        }

        if (!disputeRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        disputeRepository.deleteById(id);

        return ResponseEntity.ok(
                "Dispute deleted successfully"
        );
    }

    // =========================
    // AUTHENTICATED USER
    // =========================

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

    // =========================
    // ADMIN CHECK
    // =========================

    private boolean isAdmin(User user) {

        return user != null &&
                "ADMIN".equalsIgnoreCase(
                        user.getRole()
                );
    }
}