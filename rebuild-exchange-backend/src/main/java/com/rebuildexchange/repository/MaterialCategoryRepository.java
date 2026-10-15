package com.rebuildexchange.repository;

import com.rebuildexchange.entity.MaterialCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialCategoryRepository extends JpaRepository<MaterialCategory, Long> {
    Optional<MaterialCategory> findBySlug(String slug);
    Optional<MaterialCategory> findByNameIgnoreCase(String name);
    List<MaterialCategory> findAllByIsActiveTrueOrderByDisplayOrderAsc();
}
