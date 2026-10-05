package com.clothingexchange.clothing_exchange_marketplace.service;

import com.clothingexchange.clothing_exchange_marketplace.model.ChatMessage;
import com.clothingexchange.clothing_exchange_marketplace.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessageService(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    public ChatMessage saveMessage(ChatMessage message) {

        if (message.getSenderId() == null ||
            message.getReceiverId() == null ||
            message.getMessage() == null ||
            message.getMessage().trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid chat message");
        }

        message.setMessage(message.getMessage().trim());

        if (message.getTimestamp() == null) {
            message.setTimestamp(LocalDateTime.now());
        }

        return chatMessageRepository.save(message);
    }

    public List<ChatMessage> getConversation(Long user1, Long user2) {
        return chatMessageRepository
                .findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByTimestampAsc(
                        user1,
                        user2,
                        user2,
                        user1
                );
    }
}