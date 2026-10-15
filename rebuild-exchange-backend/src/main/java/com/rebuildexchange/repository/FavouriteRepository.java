package com.rebuildexchange.repository;

import com.rebuildexchange.entity.Favourite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavouriteRepository extends JpaRepository<Favourite, Long> {
    List<Favourite> findByUserId(Long userId);
    Optional<Favourite> findByUserIdAndMaterialId(Long userId, Long materialId);
    boolean existsByUserIdAndMaterialId(Long userId, Long materialId);
    void deleteByUserIdAndMaterialId(Long userId, Long materialId);
    long countByUserId(Long userId);
}
