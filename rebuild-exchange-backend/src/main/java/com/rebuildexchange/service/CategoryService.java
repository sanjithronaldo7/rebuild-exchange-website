package com.rebuildexchange.service;

import com.rebuildexchange.dto.CategoryDTO;
import com.rebuildexchange.entity.MaterialCategory;
import com.rebuildexchange.exception.BadRequestException;
import com.rebuildexchange.exception.ResourceNotFoundException;
import com.rebuildexchange.repository.MaterialCategoryRepository;
import com.rebuildexchange.repository.MaterialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final MaterialCategoryRepository categoryRepository;
    private final MaterialRepository materialRepository;

    public CategoryService(MaterialCategoryRepository categoryRepository, MaterialRepository materialRepository) {
        this.categoryRepository = categoryRepository;
        this.materialRepository = materialRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllActiveCategories() {
        return categoryRepository.findAllByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(CategoryDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(Long id) {
        MaterialCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return new CategoryDTO(category);
    }

    @Transactional
    public CategoryDTO createCategory(CategoryDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new BadRequestException("Category name is required");
        }

        String slug = dto.getSlug() != null && !dto.getSlug().trim().isEmpty()
                ? dto.getSlug().trim().toLowerCase()
                : dto.getName().trim().toLowerCase().replace(" ", "-");

        if (categoryRepository.findBySlug(slug).isPresent()) {
            throw new BadRequestException("Category with slug '" + slug + "' already exists");
        }

        MaterialCategory category = new MaterialCategory(
                dto.getName().trim(),
                slug,
                dto.getIconSymbol() != null ? dto.getIconSymbol() : "＋",
                dto.getDescription(),
                dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 0
        );
        MaterialCategory saved = categoryRepository.save(category);
        return new CategoryDTO(saved);
    }

    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryDTO dto) {
        MaterialCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
            category.setName(dto.getName().trim());
        }
        if (dto.getSlug() != null && !dto.getSlug().trim().isEmpty()) {
            category.setSlug(dto.getSlug().trim().toLowerCase());
        }
        if (dto.getIconSymbol() != null) {
            category.setIconSymbol(dto.getIconSymbol());
        }
        if (dto.getDescription() != null) {
            category.setDescription(dto.getDescription());
        }
        if (dto.getDisplayOrder() != null) {
            category.setDisplayOrder(dto.getDisplayOrder());
        }

        MaterialCategory updated = categoryRepository.save(category);
        return new CategoryDTO(updated);
    }

    @Transactional
    public void deleteCategory(Long id) {
        MaterialCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        // Enforce relational integrity constraint: Cannot delete if active materials exist under this category
        long count = materialRepository.countByCategoryId(id);
        if (count > 0) {
            throw new BadRequestException("Cannot delete category '" + category.getName()
                    + "' because it is referenced by " + count + " active material listings. Reassign or delete those listings first.");
        }

        categoryRepository.delete(category);
    }
}
