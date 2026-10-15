package com.rebuildexchange.repository;

import com.rebuildexchange.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    Page<Order> findByBuyerId(Long buyerId, Pageable pageable);
    Page<Order> findBySellerId(Long sellerId, Pageable pageable);
    long countByOrderStatus(String orderStatus);
    long countBySellerIdAndOrderStatus(Long sellerId, String orderStatus);
}
