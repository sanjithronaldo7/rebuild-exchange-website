package com.rebuildexchange.controller;

import com.rebuildexchange.dto.ApiResponse;
import com.rebuildexchange.dto.ChatMessageRequest;
import com.rebuildexchange.dto.ChatMessageResponse;
import com.rebuildexchange.service.ChatbotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
@Tag(name = "AI Chatbot", description = "Endpoints for Google Gemini AI assistant and guidance")
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/message")
    @Operation(summary = "Send a question to the ReBuild helper and get an AI response")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(@Valid @RequestBody ChatMessageRequest req) {
        ChatMessageResponse response = chatbotService.processMessage(req);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
