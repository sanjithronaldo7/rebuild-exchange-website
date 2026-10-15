package com.rebuildexchange.service;

import com.rebuildexchange.dto.FeedbackRequestDTO;
import com.rebuildexchange.dto.FeedbackResponseDTO;
import com.rebuildexchange.entity.Feedback;
import com.rebuildexchange.entity.Order;
import com.rebuildexchange.entity.User;
import com.rebuildexchange.exception.BadRequestException;
import com.rebuildexchange.exception.ResourceNotFoundException;
import com.rebuildexchange.exception.UnauthorizedException;
import com.rebuildexchange.repository.FeedbackRepository;
import com.rebuildexchange.repository.OrderRepository;
import com.rebuildexchange.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public FeedbackService(FeedbackRepository feedbackRepository,
                           OrderRepository orderRepository,
                           UserRepository userRepository) {
        this.feedbackRepository = feedbackRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public FeedbackResponseDTO submitFeedback(FeedbackRequestDTO req, Long authenticatedBuyerId) {
        Order order = orderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + req.getOrderId()));

        // Verification 1: Order must be COMPLETED
        if (!"COMPLETED".equalsIgnoreCase(order.getOrderStatus())) {
            throw new BadRequestException("Feedback can only be submitted after an order has been completed");
        }

        // Verification 2: Only the buyer who completed the order can rate it
        if (!order.getBuyer().getId().equals(authenticatedBuyerId)) {
            throw new UnauthorizedException("Only the verified buyer of this completed transaction can submit feedback");
        }

        // Verification 3: Prevent duplicate ratings for the same order
        if (feedbackRepository.findByOrderId(order.getId()).isPresent()) {
            throw new BadRequestException("Feedback has already been submitted for this order");
        }

        Feedback feedback = new Feedback(
                order,
                order.getBuyer(),
                order.getSeller(),
                req.getRating(),
                req.getComment()
        );

        Feedback saved = feedbackRepository.save(feedback);
        return new FeedbackResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<FeedbackResponseDTO> getSellerFeedback(Long sellerId) {
        return feedbackRepository.findBySellerIdOrderByCreatedAtDesc(sellerId)
                .stream()
                .map(FeedbackResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Double getSellerAverageRating(Long sellerId) {
        Double avg = feedbackRepository.getAverageRatingForSeller(sellerId);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 5.0;
    }
}
