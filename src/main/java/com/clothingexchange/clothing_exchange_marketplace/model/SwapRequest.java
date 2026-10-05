package com.clothingexchange.clothing_exchange_marketplace.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "swap_requests")
public class SwapRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long requesterId;

    private Long ownerId;

    private Long requestedItemId;

    private Long offeredItemId;

    private String message;

    private String status = "PENDING";

    private LocalDateTime createdAt;

    public SwapRequest() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(Long requesterId) {
        this.requesterId = requesterId;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Long getRequestedItemId() {
        return requestedItemId;
    }

    public void setRequestedItemId(Long requestedItemId) {
        this.requestedItemId = requestedItemId;
    }

    public Long getOfferedItemId() {
        return offeredItemId;
    }

    public void setOfferedItemId(Long offeredItemId) {
        this.offeredItemId = offeredItemId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}