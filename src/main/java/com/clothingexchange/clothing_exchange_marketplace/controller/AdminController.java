package com.clothingexchange.clothing_exchange_marketplace.controller;

import com.clothingexchange.clothing_exchange_marketplace.model.ClothingItem;
import com.clothingexchange.clothing_exchange_marketplace.model.Dispute;
import com.clothingexchange.clothing_exchange_marketplace.model.SwapRequest;
import com.clothingexchange.clothing_exchange_marketplace.model.User;
import com.clothingexchange.clothing_exchange_marketplace.repository.ClothingItemRepository;
import com.clothingexchange.clothing_exchange_marketplace.repository.DisputeRepository;
import com.clothingexchange.clothing_exchange_marketplace.repository.SwapRequestRepository;
import com.clothingexchange.clothing_exchange_marketplace.repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final UserRepository userRepository;
    private final ClothingItemRepository clothingItemRepository;
    private final SwapRequestRepository swapRequestRepository;
    private final DisputeRepository disputeRepository;

    public AdminController(
            UserRepository userRepository,
            ClothingItemRepository clothingItemRepository,
            SwapRequestRepository swapRequestRepository,
            DisputeRepository disputeRepository) {

        this.userRepository = userRepository;
        this.clothingItemRepository = clothingItemRepository;
        this.swapRequestRepository = swapRequestRepository;
        this.disputeRepository = disputeRepository;
    }


    // =====================================================
    // USERS
    // =====================================================

    @GetMapping("/users/count")
    public ResponseEntity<Long> getUserCount() {

        return ResponseEntity.ok(
                userRepository.count()
        );
    }


    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {

        List<User> users =
                userRepository.findAll();

        List<Map<String, Object>> safeUsers =
                users.stream()
                        .map(this::createSafeUserResponse)
                        .toList();

        return ResponseEntity.ok(safeUsers);
    }


    /*
     * Creates a safe user response.
     *
     * IMPORTANT:
     * Password is intentionally NOT included.
     */
    private Map<String, Object> createSafeUserResponse(
            User user) {

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put("id", user.getId());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("phone", user.getPhone());
        response.put("location", user.getLocation());
        response.put("role", user.getRole());

        return response;
    }


    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(
            @PathVariable Long id) {

        if (!userRepository.existsById(id)) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        userRepository.deleteById(id);

        return ResponseEntity.ok(
                "User deleted successfully"
        );
    }


    // =====================================================
    // CLOTHING ITEMS
    // =====================================================

    @GetMapping("/items/count")
    public ResponseEntity<Long> getItemCount() {

        return ResponseEntity.ok(
                clothingItemRepository.count()
        );
    }


    @GetMapping("/items")
    public ResponseEntity<List<ClothingItem>> getAllItems() {

        return ResponseEntity.ok(
                clothingItemRepository.findAll()
        );
    }


    @DeleteMapping("/items/{id}")
    public ResponseEntity<?> deleteItem(
            @PathVariable Long id) {

        if (!clothingItemRepository.existsById(id)) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        clothingItemRepository.deleteById(id);

        return ResponseEntity.ok(
                "Clothing item deleted successfully"
        );
    }


    // =====================================================
    // SWAP REQUESTS
    // =====================================================

    @GetMapping("/swaps/count")
    public ResponseEntity<Long> getSwapCount() {

        return ResponseEntity.ok(
                swapRequestRepository.count()
        );
    }


    @GetMapping("/swaps")
    public ResponseEntity<List<SwapRequest>> getAllSwaps() {

        return ResponseEntity.ok(
                swapRequestRepository.findAll()
        );
    }


    @DeleteMapping("/swaps/{id}")
    public ResponseEntity<?> deleteSwap(
            @PathVariable Long id) {

        if (!swapRequestRepository.existsById(id)) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        swapRequestRepository.deleteById(id);

        return ResponseEntity.ok(
                "Swap request deleted successfully"
        );
    }


    // =====================================================
    // DISPUTES
    // =====================================================

    @GetMapping("/disputes/count")
    public ResponseEntity<Long> getDisputeCount() {

        return ResponseEntity.ok(
                disputeRepository.count()
        );
    }


    @GetMapping("/disputes/open/count")
    public ResponseEntity<Long> getOpenDisputeCount() {

        return ResponseEntity.ok(
                disputeRepository.countByStatus("OPEN")
        );
    }


    @GetMapping("/disputes/resolved/count")
    public ResponseEntity<Long> getResolvedDisputeCount() {

        return ResponseEntity.ok(
                disputeRepository.countByStatus("RESOLVED")
        );
    }


    @GetMapping("/disputes/rejected/count")
    public ResponseEntity<Long> getRejectedDisputeCount() {

        return ResponseEntity.ok(
                disputeRepository.countByStatus("REJECTED")
        );
    }


    @GetMapping("/disputes")
    public ResponseEntity<List<Dispute>> getAllDisputes() {

        return ResponseEntity.ok(
                disputeRepository.findAll()
        );
    }


    // =====================================================
    // UPDATE DISPUTE STATUS
    // =====================================================

    @PutMapping("/disputes/{id}/status")
    public ResponseEntity<?> updateDisputeStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        Dispute dispute =
                disputeRepository.findById(id)
                        .orElse(null);

        if (dispute == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }


        String newStatus =
                status.toUpperCase();


        if (!newStatus.equals("OPEN") &&
            !newStatus.equals("RESOLVED") &&
            !newStatus.equals("REJECTED")) {

            return ResponseEntity
                    .badRequest()
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


    // =====================================================
    // DELETE DISPUTE
    // =====================================================

    @DeleteMapping("/disputes/{id}")
    public ResponseEntity<?> deleteDispute(
            @PathVariable Long id) {

        if (!disputeRepository.existsById(id)) {

            return ResponseEntity
                    .notFound()
                    .build();
        }


        disputeRepository.deleteById(id);


        return ResponseEntity.ok(
                "Dispute deleted successfully"
        );
    }

}