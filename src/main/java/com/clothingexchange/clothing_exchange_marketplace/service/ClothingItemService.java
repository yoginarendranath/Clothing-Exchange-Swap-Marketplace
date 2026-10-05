package com.clothingexchange.clothing_exchange_marketplace.service;

import com.clothingexchange.clothing_exchange_marketplace.model.ClothingItem;
import com.clothingexchange.clothing_exchange_marketplace.repository.ClothingItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClothingItemService {

    private final ClothingItemRepository clothingItemRepository;

    public ClothingItemService(ClothingItemRepository clothingItemRepository) {
        this.clothingItemRepository = clothingItemRepository;
    }

    // =========================
    // ADD ITEM
    // =========================

    public ClothingItem addItem(ClothingItem item) {

        if (item.getOwnerId() == null) {
            throw new IllegalArgumentException("Owner is required");
        }

        item.setStatus("AVAILABLE");

        return clothingItemRepository.save(item);
    }

    // Keep compatibility with existing code
    public ClothingItem createItem(ClothingItem item) {
        return addItem(item);
    }

    // =========================
    // GET ALL ITEMS
    // =========================

    public List<ClothingItem> getAllItems() {
        return clothingItemRepository.findAll();
    }

    // =========================
    // GET AVAILABLE ITEMS
    // =========================

    public List<ClothingItem> getAvailableItems() {
        return clothingItemRepository.findByStatus("AVAILABLE");
    }

    // =========================
    // GET BY CATEGORY
    // =========================

    public List<ClothingItem> getByCategory(String category) {
        return clothingItemRepository.findByCategory(category);
    }

    // =========================
    // GET BY LOCATION
    // =========================

    public List<ClothingItem> getByLocation(String location) {
        return clothingItemRepository.findByLocationIgnoreCase(location);
    }

    // =========================
    // GET BY OWNER
    // =========================

    public List<ClothingItem> getByOwner(Long ownerId) {
        return clothingItemRepository.findByOwnerId(ownerId);
    }

    // =========================
    // GET ITEM BY ID
    // =========================

    public ClothingItem getItemById(Long id) {

        return clothingItemRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Clothing item not found"
                        ));
    }

    // =========================
    // UPDATE ITEM
    // =========================

    public ClothingItem updateItem(
            Long itemId,
            Long ownerId,
            ClothingItem updatedItem) {

        ClothingItem existing = getItemById(itemId);

        // OWNER CHECK
        if (!existing.getOwnerId().equals(ownerId)) {
            throw new SecurityException(
                    "You are not allowed to edit this item"
            );
        }

        existing.setTitle(updatedItem.getTitle());
        existing.setCategory(updatedItem.getCategory());
        existing.setSize(updatedItem.getSize());
        existing.setBrand(updatedItem.getBrand());
        existing.setCondition(updatedItem.getCondition());
        existing.setDescription(updatedItem.getDescription());
        existing.setEstimatedValue(updatedItem.getEstimatedValue());
        existing.setLocation(updatedItem.getLocation());

        if (updatedItem.getImageUrl() != null &&
                !updatedItem.getImageUrl().isBlank()) {

            existing.setImageUrl(updatedItem.getImageUrl());
        }

        if (updatedItem.getStatus() != null &&
                !updatedItem.getStatus().isBlank()) {

            existing.setStatus(updatedItem.getStatus());
        }

        return clothingItemRepository.save(existing);
    }

    // =========================
    // DELETE ITEM
    // =========================

    public void deleteItem(
            Long itemId,
            Long ownerId) {

        ClothingItem existing = getItemById(itemId);

        // OWNER CHECK
        if (!existing.getOwnerId().equals(ownerId)) {
            throw new SecurityException(
                    "You are not allowed to delete this item"
            );
        }

        clothingItemRepository.delete(existing);
    }
}