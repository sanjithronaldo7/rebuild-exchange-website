package com.rebuildexchange.repository;

import com.rebuildexchange.entity.Material;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long>, JpaSpecificationExecutor<Material> {

    Page<Material> findBySellerId(Long sellerId, Pageable pageable);

    List<Material> findBySellerIdAndAvailabilityStatus(Long sellerId, String status);

    long countByAvailabilityStatus(String status);

    long countByCategoryId(Long categoryId);

    @Query("SELECT COALESCE(SUM(m.estimatedWeightKg), 0) FROM Material m WHERE m.availabilityStatus = 'REHOMED'")
    BigDecimal sumDivertedWeightKg();

    @Query("SELECT COALESCE(SUM(m.estimatedWeightKg), 0) FROM Material m WHERE m.seller.id = :sellerId AND m.availabilityStatus = 'REHOMED'")
    BigDecimal sumDivertedWeightKgBySeller(Long sellerId);
}
