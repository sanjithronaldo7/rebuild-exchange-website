package com.rebuildexchange.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class ChatMessageRequest {

    @NotBlank(message = "Message text is required")
    private String message;

    private String sessionId;

    public ChatMessageRequest() {}

    public ChatMessageRequest(String message, String sessionId) {
        this.message = message;
        this.sessionId = sessionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
