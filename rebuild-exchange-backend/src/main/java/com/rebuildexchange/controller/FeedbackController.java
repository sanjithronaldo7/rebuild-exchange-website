package com.rebuildexchange.controller;

import com.rebuildexchange.dto.ApiResponse;
import com.rebuildexchange.dto.FeedbackRequestDTO;
import com.rebuildexchange.dto.FeedbackResponseDTO;
import com.rebuildexchange.security.UserPrincipal;
import com.rebuildexchange.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/feedback")
@Tag(name = "Feedback & Ratings", description = "Endpoints for rating completed transactions and viewing seller reviews")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Submit rating and review for an eligible completed order")
    public ResponseEntity<ApiResponse<FeedbackResponseDTO>> submitFeedback(
            @Valid @RequestBody FeedbackRequestDTO req,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        FeedbackResponseDTO response = feedbackService.submitFeedback(req, currentUser.getId());
        return new ResponseEntity<>(ApiResponse.success("Feedback submitted successfully", response), HttpStatus.CREATED);
    }

    @GetMapping("/seller/{sellerId}")
    @Operation(summary = "Get all reviews and average star rating for a seller")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSellerFeedback(@PathVariable Long sellerId) {
        List<FeedbackResponseDTO> reviews = feedbackService.getSellerFeedback(sellerId);
        Double averageRating = feedbackService.getSellerAverageRating(sellerId);

        Map<String, Object> data = new HashMap<>();
        data.put("sellerId", sellerId);
        data.put("averageRating", averageRating);
        data.put("totalReviews", reviews.size());
        data.put("reviews", reviews);

        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
