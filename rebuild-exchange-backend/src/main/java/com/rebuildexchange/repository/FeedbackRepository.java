package com.rebuildexchange.repository;

import com.rebuildexchange.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findBySellerIdOrderByCreatedAtDesc(Long sellerId);
    Optional<Feedback> findByOrderId(Long orderId);

    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.seller.id = :sellerId")
    Double getAverageRatingForSeller(Long sellerId);

    long countBySellerId(Long sellerId);
}
