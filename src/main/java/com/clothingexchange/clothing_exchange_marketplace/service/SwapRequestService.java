package com.clothingexchange.clothing_exchange_marketplace.service;

import com.clothingexchange.clothing_exchange_marketplace.model.SwapRequest;
import com.clothingexchange.clothing_exchange_marketplace.repository.SwapRequestRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SwapRequestService {

    private final SwapRequestRepository swapRequestRepository;

    public SwapRequestService(
            SwapRequestRepository swapRequestRepository) {
        this.swapRequestRepository = swapRequestRepository;
    }

    // =========================
    // CREATE SWAP REQUEST
    // =========================

    public SwapRequest createRequest(SwapRequest request) {

        if (request.getRequesterId() == null ||
                request.getOwnerId() == null) {

            throw new IllegalArgumentException(
                    "Requester and owner are required");
        }

        if (request.getRequesterId()
                .equals(request.getOwnerId())) {

            throw new IllegalArgumentException(
                    "You cannot send a swap request to yourself");
        }

        request.setStatus("PENDING");
        request.setCreatedAt(LocalDateTime.now());

        return swapRequestRepository.save(request);
    }

    // Keep compatibility with existing code
    public SwapRequest createSwapRequest(SwapRequest request) {
        return createRequest(request);
    }

    // =========================
    // SENT REQUESTS
    // =========================

    public List<SwapRequest> getSentRequests(Long requesterId) {
        return swapRequestRepository.findByRequesterId(requesterId);
    }

    // =========================
    // RECEIVED REQUESTS
    // =========================

    public List<SwapRequest> getReceivedRequests(Long ownerId) {
        return swapRequestRepository.findByOwnerId(ownerId);
    }

    // =========================
    // GET BY STATUS
    // =========================

    public List<SwapRequest> getByStatus(String status) {
        return swapRequestRepository.findByStatus(status);
    }

    // =========================
    // ACCEPT / REJECT
    // =========================

    public SwapRequest updateStatus(
            Long swapId,
            Long ownerId,
            String status) {

        SwapRequest request = swapRequestRepository.findById(swapId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Swap request not found"));

        // ONLY ITEM OWNER CAN ACCEPT / REJECT
        if (!request.getOwnerId().equals(ownerId)) {

            throw new SecurityException(
                    "Only the item owner can update this request");
        }

        if (!"ACCEPTED".equals(status) &&
                !"REJECTED".equals(status)) {

            throw new IllegalArgumentException(
                    "Invalid swap status");
        }

        if (!"PENDING".equals(request.getStatus())) {

            throw new IllegalStateException(
                    "This swap request has already been processed");
        }

        request.setStatus(status);

        return swapRequestRepository.save(request);
    }

    // =========================
    // DELETE REQUEST
    // =========================

    public void deleteRequest(
            Long swapId,
            Long userId) {

        SwapRequest request = swapRequestRepository.findById(swapId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Swap request not found"));

        // REQUESTER OR OWNER CAN DELETE
        boolean allowed =
                request.getRequesterId().equals(userId) ||
                request.getOwnerId().equals(userId);

        if (!allowed) {

            throw new SecurityException(
                    "You are not allowed to delete this request");
        }

        swapRequestRepository.delete(request);
    }

    // Keep compatibility with existing code
    public void deleteSwapRequest(
            Long swapId,
            Long userId) {

        deleteRequest(swapId, userId);
    }
}