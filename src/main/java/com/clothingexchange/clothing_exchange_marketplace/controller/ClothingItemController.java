package com.clothingexchange.clothing_exchange_marketplace.controller;

import com.clothingexchange.clothing_exchange_marketplace.model.ClothingItem;
import com.clothingexchange.clothing_exchange_marketplace.model.User;
import com.clothingexchange.clothing_exchange_marketplace.service.ClothingItemService;
import com.clothingexchange.clothing_exchange_marketplace.service.CloudinaryService;
import com.clothingexchange.clothing_exchange_marketplace.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "*")
public class ClothingItemController {

    private final ClothingItemService clothingItemService;
    private final CloudinaryService cloudinaryService;
    private final UserService userService;

    public ClothingItemController(
            ClothingItemService clothingItemService,
            CloudinaryService cloudinaryService,
            UserService userService) {

        this.clothingItemService = clothingItemService;
        this.cloudinaryService = cloudinaryService;
        this.userService = userService;
    }

    // =========================================================
    // ADD CLOTHING ITEM
    // =========================================================

    @PostMapping
    public ResponseEntity<?> addItem(
            @RequestBody ClothingItem item) {

        try {

            User loggedInUser = getAuthenticatedUser();

            if (loggedInUser == null) {
                return ResponseEntity
                        .status(401)
                        .body("Not authenticated");
            }

            // Never trust ownerId sent by frontend
            item.setOwnerId(loggedInUser.getId());

            ClothingItem savedItem =
                    clothingItemService.addItem(item);

            return ResponseEntity.ok(savedItem);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        } catch (Exception e) {

            return ResponseEntity
                    .internalServerError()
                    .body("Unable to add clothing item.");
        }
    }

    // =========================================================
    // UPLOAD IMAGE TO CLOUDINARY
    // =========================================================

    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file) {

        try {

            User loggedInUser = getAuthenticatedUser();

            if (loggedInUser == null) {
                return ResponseEntity
                        .status(401)
                        .body("Not authenticated");
            }

            String imageUrl =
                    cloudinaryService.uploadImage(file);

            Map<String, String> response =
                    new LinkedHashMap<>();

            response.put("imageUrl", imageUrl);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        } catch (Exception e) {

            return ResponseEntity
                    .internalServerError()
                    .body(
                            "Image upload failed. Please try again."
                    );
        }
    }

    // =========================================================
    // GET ALL CLOTHING ITEMS
    // =========================================================

    @GetMapping
    public ResponseEntity<List<ClothingItem>> getAllItems() {

        return ResponseEntity.ok(
                clothingItemService.getAllItems()
        );
    }

    // =========================================================
    // GET AVAILABLE ITEMS
    // =========================================================

    @GetMapping("/available")
    public ResponseEntity<List<ClothingItem>> getAvailableItems() {

        return ResponseEntity.ok(
                clothingItemService.getAvailableItems()
        );
    }

    // =========================================================
    // GET ITEMS BY CATEGORY
    // =========================================================

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ClothingItem>> getByCategory(
            @PathVariable String category) {

        return ResponseEntity.ok(
                clothingItemService.getByCategory(category)
        );
    }

    // =========================================================
    // GET ITEMS BY LOCATION
    // =========================================================

    @GetMapping("/location/{location}")
    public ResponseEntity<List<ClothingItem>> getByLocation(
            @PathVariable String location) {

        return ResponseEntity.ok(
                clothingItemService.getByLocation(location)
        );
    }

    // =========================================================
    // GET ITEMS BY OWNER
    // =========================================================

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<?> getByOwner(
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

        // Users can only view their own owner's list
        if (!isAdmin &&
                !loggedInUser.getId().equals(ownerId)) {

            return ResponseEntity
                    .status(403)
                    .body(
                            "You are not authorized to view these items"
                    );
        }

        return ResponseEntity.ok(
                clothingItemService.getByOwner(ownerId)
        );
    }

    // =========================================================
    // GET SINGLE ITEM BY ID
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<?> getItemById(
            @PathVariable Long id) {

        try {

            ClothingItem item =
                    clothingItemService.getItemById(id);

            return ResponseEntity.ok(item);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .notFound()
                    .build();
        }
    }

    // =========================================================
    // UPDATE CLOTHING ITEM
    // =========================================================

    @PutMapping("/{id}")
    public ResponseEntity<?> updateItem(
            @PathVariable Long id,
            @RequestBody ClothingItem item) {

        try {

            User loggedInUser = getAuthenticatedUser();

            if (loggedInUser == null) {
                return ResponseEntity
                        .status(401)
                        .body("Not authenticated");
            }

            Long authenticatedUserId =
                    loggedInUser.getId();

            ClothingItem updatedItem =
                    clothingItemService.updateItem(
                            id,
                            authenticatedUserId,
                            item
                    );

            return ResponseEntity.ok(updatedItem);

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
                            "Unable to update clothing item."
                    );
        }
    }

    // =========================================================
    // DELETE CLOTHING ITEM
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItem(
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

            clothingItemService.deleteItem(
                    id,
                    authenticatedUserId
            );

            return ResponseEntity.ok(
                    "Clothing item deleted successfully"
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
                            "Unable to delete clothing item."
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