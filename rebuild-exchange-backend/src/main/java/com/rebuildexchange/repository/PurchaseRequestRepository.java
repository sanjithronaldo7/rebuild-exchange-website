package com.rebuildexchange.repository;

import com.rebuildexchange.entity.PurchaseRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequest, Long> {
    Page<PurchaseRequest> findByBuyerId(Long buyerId, Pageable pageable);
    Page<PurchaseRequest> findBySellerId(Long sellerId, Pageable pageable);
    List<PurchaseRequest> findByMaterialId(Long materialId);
    long countBySellerIdAndStatus(Long sellerId, String status);
    long countByStatus(String status);
}
