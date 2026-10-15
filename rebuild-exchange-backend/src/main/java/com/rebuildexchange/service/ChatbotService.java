package com.rebuildexchange.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebuildexchange.dto.ChatMessageRequest;
import com.rebuildexchange.dto.ChatMessageResponse;
import com.rebuildexchange.entity.ChatbotConversation;
import com.rebuildexchange.entity.ChatbotMessage;
import com.rebuildexchange.repository.ChatbotConversationRepository;
import com.rebuildexchange.repository.ChatbotMessageRepository;
import com.rebuildexchange.repository.MaterialCategoryRepository;
import com.rebuildexchange.repository.MaterialRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.*;

@Service
public class ChatbotService {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotService.class);

    private final ChatbotConversationRepository conversationRepository;
    private final ChatbotMessageRepository messageRepository;
    private final MaterialRepository materialRepository;
    private final MaterialCategoryRepository categoryRepository;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    @Value("${app.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${app.gemini.model:gemini-2.5-flash}")
    private String geminiModel;

    public ChatbotService(ChatbotConversationRepository conversationRepository,
                          ChatbotMessageRepository messageRepository,
                          MaterialRepository materialRepository,
                          MaterialCategoryRepository categoryRepository,
                          ObjectMapper objectMapper) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.materialRepository = materialRepository;
        this.categoryRepository = categoryRepository;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().build();
    }

    @Transactional
    public ChatMessageResponse processMessage(ChatMessageRequest req) {
        String sessionId = StringUtils.hasText(req.getSessionId())
                ? req.getSessionId()
                : UUID.randomUUID().toString();

        ChatbotConversation conversation = conversationRepository.findBySessionId(sessionId)
                .orElseGet(() -> conversationRepository.save(new ChatbotConversation(sessionId, null)));

        // Record user message
        ChatbotMessage userMsg = new ChatbotMessage(conversation, "USER", req.getMessage().trim());
        messageRepository.save(userMsg);

        String botReply;
        boolean isAi = false;

        // Try Gemini API if key is configured
        if (StringUtils.hasText(geminiApiKey) && !geminiApiKey.startsWith("MY_")) {
            try {
                botReply = callGeminiApi(req.getMessage().trim());
                isAi = true;
            } catch (Exception ex) {
                logger.warn("Gemini API call failed ({}), falling back to knowledge base", ex.getMessage());
                botReply = generateKnowledgeBaseReply(req.getMessage().trim());
            }
        } else {
            botReply = generateKnowledgeBaseReply(req.getMessage().trim());
        }

        // Record bot response
        ChatbotMessage botMsg = new ChatbotMessage(conversation, "ASSISTANT", botReply);
        messageRepository.save(botMsg);

        List<String> suggestions = Arrays.asList(
                "How do I list materials?",
                "How does pickup and payment work?",
                "What material categories are available?",
                "How are impact estimates calculated?"
        );

        return new ChatMessageResponse(botReply, sessionId, isAi, suggestions);
    }

    private String callGeminiApi(String userMessage) throws Exception {
        long availableCount = materialRepository.countByAvailabilityStatus("AVAILABLE");
        List<String> categories = categoryRepository.findAllByIsActiveTrueOrderByDisplayOrderAsc()
                .stream().map(c -> c.getName()).toList();

        String systemPrompt = "You are the ReBuild Helper assistant for 'ReBuild Exchange', a circular construction material leftover exchange marketplace based in Bengaluru.\n"
                + "Live website statistics:\n"
                + "- Available surplus materials in database: " + availableCount + "\n"
                + "- Active material categories: " + String.join(", ", categories) + "\n\n"
                + "Guidelines:\n"
                + "1. Guide users on searching materials, listing surplus, arranging safe local pickup, and how environmental impact is calculated.\n"
                + "2. Never invent fake private listings, seller phone numbers, or credit card procedures.\n"
                + "3. Keep answers concise, polite, friendly, and practical (under 3 paragraphs).";

        Map<String, Object> requestBody = Map.of(
                "system_instruction", Map.of(
                        "parts", List.of(Map.of("text", systemPrompt))
                ),
                "contents", List.of(
                        Map.of("role", "user", "parts", List.of(Map.of("text", userMessage)))
                )
        );

        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + geminiModel + ":generateContent?key=" + geminiApiKey;

        String responseJson = restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        JsonNode root = objectMapper.readTree(responseJson);
        JsonNode candidate = root.path("candidates").path(0);
        JsonNode textNode = candidate.path("content").path("parts").path(0).path("text");

        if (textNode.isMissingNode() || textNode.asText().isEmpty()) {
            throw new RuntimeException("Empty response from Gemini API");
        }

        return textNode.asText().trim();
    }

    private String generateKnowledgeBaseReply(String query) {
        String q = query.toLowerCase();

        if (q.contains("list") || q.contains("sell") || q.contains("post") || q.contains("upload")) {
            return "To list surplus materials:\n1. Click '+ List materials' in the top navigation.\n2. Choose your category, specify quantity, price (or 0 for Free to collect), and your pickup area.\n3. Add a photo and short description.\nYour listing will be published immediately for local builders to discover.";
        }
        if (q.contains("pickup") || q.contains("pay") || q.contains("collect") || q.contains("contact")) {
            return "Pickup and payment coordination:\n1. Browse listings and click on any material card to view full details.\n2. Click 'Message seller' to send a purchase request with your quantity and timing.\n3. Once the seller accepts, you coordinate pickup and payment securely via the platform.";
        }
        if (q.contains("categor") || q.contains("type") || q.contains("what can i")) {
            return "ReBuild Exchange supports 9 standard surplus construction categories: Tiles, Wood, Bricks, Paint, Cement, Electrical, Plumbing, Pipes, and Other materials. Both brand new leftovers and sound reclaimed items are welcome.";
        }
        if (q.contains("impact") || q.contains("carbon") || q.contains("waste") || q.contains("sustain")) {
            return "Our impact metrics measure circular reuse by multiplying listed quantities by typical standard material weights (kg/tonnes). Every material rehomed diverts valuable resources from landfill and keeps good things in local circulation.";
        }
        if (q.contains("sign") || q.contains("login") || q.contains("account") || q.contains("register")) {
            return "You can browse materials as a guest! To message sellers, save favourites, or list your own surplus, sign in or create an account via the 'Sign in' link at the top.";
        }

        return "I'm the ReBuild helper! I can guide you through finding materials, listing surplus, arranging local pickup, or understanding our circular impact metrics. What would you like to know?";
    }
}
