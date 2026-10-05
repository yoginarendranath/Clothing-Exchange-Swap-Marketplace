package com.clothingexchange.clothing_exchange_marketplace.repository;

import com.clothingexchange.clothing_exchange_marketplace.model.SwapRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SwapRequestRepository extends JpaRepository<SwapRequest, Long> {

    List<SwapRequest> findByRequesterId(Long requesterId);

    List<SwapRequest> findByOwnerId(Long ownerId);

    List<SwapRequest> findByStatus(String status);

    long countByStatus(String status);
}