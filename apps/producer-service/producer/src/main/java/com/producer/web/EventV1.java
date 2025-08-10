package com.producer.web;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.UUID;

public class EventV1 {
    private String messageId;               // optional in request
    @NotBlank private String userId;
    @NotBlank private String action;
    private Instant sentAt;                 // optional

    // helpers (server can fill)
    public void ensureDefaults() {
        if (messageId == null || messageId.isBlank()) messageId = UUID.randomUUID().toString();
        if (sentAt == null) sentAt = Instant.now();
    }

    // getters/setters...
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }
}
