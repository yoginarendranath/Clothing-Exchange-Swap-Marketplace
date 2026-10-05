package com.clothingexchange.clothing_exchange_marketplace.repository;

import com.clothingexchange.clothing_exchange_marketplace.model.ClothingItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClothingItemRepository extends JpaRepository<ClothingItem, Long> {

    List<ClothingItem> findByStatus(String status);

    List<ClothingItem> findByCategory(String category);

    List<ClothingItem> findByLocationIgnoreCase(String location);

    List<ClothingItem> findByOwnerId(Long ownerId);

    List<ClothingItem> findByStatusAndCategory(String status, String category);

    long countByStatus(String status);
}