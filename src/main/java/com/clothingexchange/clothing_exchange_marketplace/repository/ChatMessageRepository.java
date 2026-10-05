package com.clothingexchange.clothing_exchange_marketplace.repository;

import com.clothingexchange.clothing_exchange_marketplace.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByTimestampAsc(
            Long senderId,
            Long receiverId,
            Long receiverId2,
            Long senderId2
    );
}