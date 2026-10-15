package com.rebuildexchange.dto;

import java.util.List;

public class ChatMessageResponse {

    private String reply;
    private String sessionId;
    private boolean isAiGenerated;
    private List<String> suggestions;

    public ChatMessageResponse() {}

    public ChatMessageResponse(String reply, String sessionId, boolean isAiGenerated, List<String> suggestions) {
        this.reply = reply;
        this.sessionId = sessionId;
        this.isAiGenerated = isAiGenerated;
        this.suggestions = suggestions;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isAiGenerated() {
        return isAiGenerated;
    }

    public void setAiGenerated(boolean aiGenerated) {
        isAiGenerated = aiGenerated;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }
}
