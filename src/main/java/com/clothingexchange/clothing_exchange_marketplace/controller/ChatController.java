package com.clothingexchange.clothing_exchange_marketplace.controller;

import com.clothingexchange.clothing_exchange_marketplace.model.ChatMessage;
import com.clothingexchange.clothing_exchange_marketplace.model.User;
import com.clothingexchange.clothing_exchange_marketplace.service.ChatMessageService;
import com.clothingexchange.clothing_exchange_marketplace.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.List;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;
    private final UserService userService;

    public ChatController(
            SimpMessagingTemplate messagingTemplate,
            ChatMessageService chatMessageService,
            UserService userService) {

        this.messagingTemplate = messagingTemplate;
        this.chatMessageService = chatMessageService;
        this.userService = userService;
    }

    // =========================================================
    // SEND REAL-TIME MESSAGE
    // =========================================================

    @MessageMapping("/chat")
    public void sendMessage(
            ChatMessage message,
            Principal principal) {

        try {

            // -------------------------------------------------
            // CHECK WEBSOCKET USER
            // -------------------------------------------------

            if (principal == null) {

                System.out.println(
                        "CHAT ERROR: WebSocket principal is null"
                );

                return;
            }

            String email =
                    principal.getName();

            if (email == null ||
                    email.trim().isEmpty()) {

                System.out.println(
                        "CHAT ERROR: User email not available"
                );

                return;
            }

            // -------------------------------------------------
            // FIND LOGGED-IN USER
            // -------------------------------------------------

            User loggedInUser =
                    userService.findByEmail(
                            email.trim().toLowerCase()
                    ).orElse(null);

            if (loggedInUser == null) {

                System.out.println(
                        "CHAT ERROR: Authenticated user not found"
                );

                return;
            }

            // -------------------------------------------------
            // VALIDATE MESSAGE OBJECT
            // -------------------------------------------------

            if (message == null) {

                System.out.println(
                        "CHAT ERROR: Message object is null"
                );

                return;
            }

            // -------------------------------------------------
            // VALIDATE RECEIVER
            // -------------------------------------------------

            if (message.getReceiverId() == null) {

                System.out.println(
                        "CHAT ERROR: Receiver ID is missing"
                );

                return;
            }

            // -------------------------------------------------
            // VALIDATE MESSAGE TEXT
            // -------------------------------------------------

            if (message.getMessage() == null ||
                    message.getMessage().trim().isEmpty()) {

                System.out.println(
                        "CHAT ERROR: Message is empty"
                );

                return;
            }

            String text =
                    message.getMessage().trim();

            // Prevent extremely large messages
            if (text.length() > 2000) {

                System.out.println(
                        "CHAT ERROR: Message too long"
                );

                return;
            }

            // -------------------------------------------------
            // NEVER TRUST FRONTEND senderId
            // -------------------------------------------------

            message.setSenderId(
                    loggedInUser.getId()
            );

            message.setMessage(text);

            // -------------------------------------------------
            // PREVENT SELF MESSAGING
            // -------------------------------------------------

            if (loggedInUser.getId()
                    .equals(message.getReceiverId())) {

                System.out.println(
                        "CHAT ERROR: Cannot message yourself"
                );

                return;
            }

            // -------------------------------------------------
            // CHECK RECEIVER EXISTS
            // -------------------------------------------------

            User receiver =
                    userService.findById(
                            message.getReceiverId()
                    ).orElse(null);

            if (receiver == null) {

                System.out.println(
                        "CHAT ERROR: Receiver not found"
                );

                return;
            }

            // -------------------------------------------------
            // SAVE MESSAGE IN DATABASE
            // -------------------------------------------------

            ChatMessage savedMessage =
                    chatMessageService.saveMessage(
                            message
                    );

            System.out.println(
                    "CHAT MESSAGE SAVED: " +
                    savedMessage.getSenderId() +
                    " -> " +
                    savedMessage.getReceiverId()
            );

            // -------------------------------------------------
            // SEND MESSAGE TO RECEIVER
            // -------------------------------------------------

            messagingTemplate.convertAndSend(
                    "/topic/user/" +
                            savedMessage.getReceiverId(),
                    savedMessage
            );

            // -------------------------------------------------
            // SEND MESSAGE BACK TO SENDER
            // -------------------------------------------------

            messagingTemplate.convertAndSend(
                    "/topic/user/" +
                            savedMessage.getSenderId(),
                    savedMessage
            );

        } catch (Exception e) {

            System.out.println(
                    "CHAT ERROR: " +
                    e.getMessage()
            );

            e.printStackTrace();
        }
    }

    // =========================================================
    // GET CHAT HISTORY
    // =========================================================

    @GetMapping("/api/chat/history")
    @ResponseBody
    public ResponseEntity<?> getChatHistory(
            @RequestParam Long user1,
            @RequestParam Long user2,
            Principal principal) {

        // -------------------------------------------------
        // CHECK LOGIN
        // -------------------------------------------------

        if (principal == null) {

            return ResponseEntity.status(401)
                    .body("Not authenticated");
        }

        String email =
                principal.getName();

        if (email == null ||
                email.trim().isEmpty()) {

            return ResponseEntity.status(401)
                    .body("Not authenticated");
        }

        // -------------------------------------------------
        // FIND LOGGED-IN USER
        // -------------------------------------------------

        User loggedInUser =
                userService.findByEmail(
                        email.trim().toLowerCase()
                ).orElse(null);

        if (loggedInUser == null) {

            return ResponseEntity.status(401)
                    .body("Not authenticated");
        }

        Long loggedInUserId =
                loggedInUser.getId();

        // -------------------------------------------------
        // USER MUST BE PART OF CONVERSATION
        // -------------------------------------------------

        boolean participant =
                loggedInUserId.equals(user1) ||
                loggedInUserId.equals(user2);

        if (!participant) {

            return ResponseEntity.status(403)
                    .body(
                            "You are not authorized to view this conversation"
                    );
        }

        // -------------------------------------------------
        // CHECK USER 1 EXISTS
        // -------------------------------------------------

        if (!userService.findById(user1).isPresent()) {

            return ResponseEntity.badRequest()
                    .body("User 1 not found");
        }

        // -------------------------------------------------
        // CHECK USER 2 EXISTS
        // -------------------------------------------------

        if (!userService.findById(user2).isPresent()) {

            return ResponseEntity.badRequest()
                    .body("User 2 not found");
        }

        // -------------------------------------------------
        // GET CONVERSATION
        // -------------------------------------------------

        List<ChatMessage> messages =
                chatMessageService.getConversation(
                        user1,
                        user2
                );

        return ResponseEntity.ok(messages);
    }
}