package com.clothingexchange.clothing_exchange_marketplace.controller;

import com.clothingexchange.clothing_exchange_marketplace.model.SwapRequest;
import com.clothingexchange.clothing_exchange_marketplace.model.User;
import com.clothingexchange.clothing_exchange_marketplace.service.SwapRequestService;
import com.clothingexchange.clothing_exchange_marketplace.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/swaps")
@CrossOrigin(origins = "*")
public class SwapRequestController {

    private final SwapRequestService swapRequestService;
    private final UserService userService;

    public SwapRequestController(
            SwapRequestService swapRequestService,
            UserService userService) {

        this.swapRequestService = swapRequestService;
        this.userService = userService;
    }

    // =========================================================
    // SEND SWAP REQUEST
    // =========================================================

    @PostMapping
    public ResponseEntity<?> createRequest(
            @RequestBody SwapRequest request) {

        try {

            User loggedInUser = getAuthenticatedUser();

            if (loggedInUser == null) {
                return ResponseEntity
                        .status(401)
                        .body("Not authenticated");
            }

            // Never trust requesterId from frontend
            request.setRequesterId(
                    loggedInUser.getId()
            );

            SwapRequest savedRequest =
                    swapRequestService.createRequest(request);

            return ResponseEntity.ok(savedRequest);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        } catch (Exception e) {

            return ResponseEntity
                    .internalServerError()
                    .body(
                            "Unable to create swap request."
                    );
        }
    }

    // =========================================================
    // SENT REQUESTS
    // =========================================================

    @GetMapping("/requester/{requesterId}")
    public ResponseEntity<?> getSentRequests(
            @PathVariable Long requesterId) {

        User loggedInUser = getAuthenticatedUser();

        if (loggedInUser == null) {
            return ResponseEntity
                    .status(401)
                    .body("Not authenticated");
        }

        boolean isAdmin =
                "ADMIN".equalsIgnoreCase(
                        loggedInUser.getRole()
                );

        if (!isAdmin &&
                !loggedInUser.getId().equals(requesterId)) {

            return ResponseEntity
                    .status(403)
                    .body(
                            "You are not authorized to view these requests"
                    );
        }

        List<SwapRequest> requests =
                swapRequestService.getSentRequests(
                        requesterId
                );

        return ResponseEntity.ok(requests);
    }

    // =========================================================
    // RECEIVED REQUESTS
    // =========================================================

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<?> getReceivedRequests(
            @PathVariable Long ownerId) {

        User loggedInUser = getAuthenticatedUser();

        if (loggedInUser == null) {
            return ResponseEntity
                    .status(401)
                    .body("Not authenticated");
        }

        boolean isAdmin =
                "ADMIN".equalsIgnoreCase(
                        loggedInUser.getRole()
                );

        if (!isAdmin &&
                !loggedInUser.getId().equals(ownerId)) {

            return ResponseEntity
                    .status(403)
                    .body(
                            "You are not authorized to view these requests"
                    );
        }

        List<SwapRequest> requests =
                swapRequestService.getReceivedRequests(
                        ownerId
                );

        return ResponseEntity.ok(requests);
    }

    // =========================================================
    // GET REQUESTS BY STATUS
    // ADMIN ONLY
    // =========================================================

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getByStatus(
            @PathVariable String status) {

        User loggedInUser = getAuthenticatedUser();

        if (loggedInUser == null) {
            return ResponseEntity
                    .status(401)
                    .body("Not authenticated");
        }

        if (!"ADMIN".equalsIgnoreCase(
                loggedInUser.getRole())) {

            return ResponseEntity
                    .status(403)
                    .body(
                            "Only administrators can view requests by status"
                    );
        }

        return ResponseEntity.ok(
                swapRequestService.getByStatus(
                        status.toUpperCase()
                )
        );
    }

    // =========================================================
    // ACCEPT / REJECT SWAP REQUEST
    // =========================================================

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        try {

            User loggedInUser = getAuthenticatedUser();

            if (loggedInUser == null) {
                return ResponseEntity
                        .status(401)
                        .body("Not authenticated");
            }

            // Authenticated user's ID is used.
            // Frontend cannot choose another ownerId.
            Long authenticatedUserId =
                    loggedInUser.getId();

            SwapRequest updatedRequest =
                    swapRequestService.updateStatus(
                            id,
                            authenticatedUserId,
                            status.toUpperCase()
                    );

            return ResponseEntity.ok(updatedRequest);

        } catch (SecurityException e) {

            return ResponseEntity
                    .status(403)
                    .body(e.getMessage());

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        } catch (IllegalStateException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        } catch (Exception e) {

            return ResponseEntity
                    .internalServerError()
                    .body(
                            "Unable to update swap request."
                    );
        }
    }

    // =========================================================
    // DELETE SWAP REQUEST
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRequest(
            @PathVariable Long id) {

        try {

            User loggedInUser = getAuthenticatedUser();

            if (loggedInUser == null) {
                return ResponseEntity
                        .status(401)
                        .body("Not authenticated");
            }

            Long authenticatedUserId =
                    loggedInUser.getId();

            swapRequestService.deleteRequest(
                    id,
                    authenticatedUserId
            );

            return ResponseEntity.ok(
                    "Swap request deleted successfully"
            );

        } catch (SecurityException e) {

            return ResponseEntity
                    .status(403)
                    .body(e.getMessage());

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        } catch (Exception e) {

            return ResponseEntity
                    .internalServerError()
                    .body(
                            "Unable to delete swap request."
                    );
        }
    }

    // =========================================================
    // GET AUTHENTICATED USER
    // =========================================================

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