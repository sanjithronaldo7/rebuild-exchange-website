package com.rebuildexchange.service;

import com.rebuildexchange.dto.MaterialRequestDTO;
import com.rebuildexchange.dto.MaterialResponseDTO;
import com.rebuildexchange.entity.Material;
import com.rebuildexchange.entity.MaterialCategory;
import com.rebuildexchange.entity.MaterialImage;
import com.rebuildexchange.entity.Role;
import com.rebuildexchange.entity.User;
import com.rebuildexchange.exception.BadRequestException;
import com.rebuildexchange.exception.ResourceNotFoundException;
import com.rebuildexchange.exception.UnauthorizedException;
import com.rebuildexchange.repository.MaterialCategoryRepository;
import com.rebuildexchange.repository.MaterialImageRepository;
import com.rebuildexchange.repository.MaterialRepository;
import com.rebuildexchange.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final MaterialCategoryRepository categoryRepository;
    private final MaterialImageRepository imageRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public MaterialService(MaterialRepository materialRepository,
                           MaterialCategoryRepository categoryRepository,
                           MaterialImageRepository imageRepository,
                           UserRepository userRepository,
                           FileStorageService fileStorageService) {
        this.materialRepository = materialRepository;
        this.categoryRepository = categoryRepository;
        this.imageRepository = imageRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional(readOnly = true)
    public Page<MaterialResponseDTO> searchMaterials(
            String query,
            String category,
            String location,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean freeOnly,
            String condition,
            String sort,
            int page,
            int size) {

        Sort sortObj = Sort.by(Sort.Direction.DESC, "createdAt");
        if ("price-low".equalsIgnoreCase(sort)) {
            sortObj = Sort.by(Sort.Direction.ASC, "price");
        } else if ("price-high".equalsIgnoreCase(sort)) {
            sortObj = Sort.by(Sort.Direction.DESC, "price");
        } else if ("newest".equalsIgnoreCase(sort)) {
            sortObj = Sort.by(Sort.Direction.DESC, "createdAt");
        }

        Pageable pageable = PageRequest.of(page, size, sortObj);

        Specification<Material> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Only show available listings to public
            predicates.add(cb.equal(root.get("availabilityStatus"), "AVAILABLE"));

            // Keyword query across title, description, and location
            if (StringUtils.hasText(query)) {
                String searchLike = "%" + query.trim().toLowerCase() + "%";
                Predicate titleLike = cb.like(cb.lower(root.get("title")), searchLike);
                Predicate descLike = cb.like(cb.lower(root.get("description")), searchLike);
                Predicate locLike = cb.like(cb.lower(root.get("pickupLocation")), searchLike);
                Predicate catLike = cb.like(cb.lower(root.get("category").get("name")), searchLike);
                predicates.add(cb.or(titleLike, descLike, locLike, catLike));
            }

            // Category filter
            if (StringUtils.hasText(category) && !"All".equalsIgnoreCase(category)) {
                predicates.add(cb.equal(cb.lower(root.get("category").get("name")), category.trim().toLowerCase()));
            }

            // Location filter
            if (StringUtils.hasText(location)) {
                predicates.add(cb.like(cb.lower(root.get("pickupLocation")), "%" + location.trim().toLowerCase() + "%"));
            }

            // Free only filter
            if (Boolean.TRUE.equals(freeOnly)) {
                predicates.add(cb.equal(root.get("price"), BigDecimal.ZERO));
            } else {
                if (minPrice != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
                }
                if (maxPrice != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
                }
            }

            // Material condition
            if (StringUtils.hasText(condition)) {
                predicates.add(cb.equal(cb.lower(root.get("materialCondition")), condition.trim().toLowerCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return materialRepository.findAll(spec, pageable).map(MaterialResponseDTO::new);
    }

    @Transactional
    public MaterialResponseDTO getMaterialById(Long id) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found with id: " + id));

        // Increment view count
        material.setViewCount(material.getViewCount() + 1);
        materialRepository.save(material);

        return new MaterialResponseDTO(material);
    }

    @Transactional
    public MaterialResponseDTO createMaterial(MaterialRequestDTO dto, Long authenticatedUserId) {
        User seller = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + authenticatedUserId));

        // Enforce seller or admin permission
        boolean isSellerOrAdmin = seller.getRoles().stream()
                .map(Role::getName)
                .anyMatch(role -> role.equals("ROLE_SELLER") || role.equals("ROLE_ADMIN"));

        if (!isSellerOrAdmin) {
            throw new UnauthorizedException("Only registered sellers or administrators can publish material listings");
        }

        // Validate title, price, quantity, location
        if (!StringUtils.hasText(dto.getTitle())) {
            throw new BadRequestException("Material title is required");
        }
        if (dto.getPrice() == null || dto.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Material price must be zero or a positive amount");
        }
        if (!StringUtils.hasText(dto.getQuantity())) {
            throw new BadRequestException("Material quantity is required");
        }
        if (!StringUtils.hasText(dto.getLocation())) {
            throw new BadRequestException("Pickup location is required");
        }

        MaterialCategory category;
        if (dto.getCategoryId() != null) {
            category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new BadRequestException("Category not found with id: " + dto.getCategoryId()));
        } else if (StringUtils.hasText(dto.getCategory())) {
            category = categoryRepository.findByNameIgnoreCase(dto.getCategory().trim())
                    .orElseGet(() -> categoryRepository.findBySlug("other")
                            .orElseThrow(() -> new BadRequestException("Invalid category name: " + dto.getCategory())));
        } else {
            throw new BadRequestException("Category is required");
        }

        Material material = new Material();
        material.setSeller(seller);
        material.setCategory(category);
        material.setTitle(dto.getTitle().trim());
        material.setDescription(dto.getDescription());
        material.setQuantity(dto.getQuantity().trim());
        material.setUnit(dto.getUnit() != null ? dto.getUnit() : "pieces");
        material.setPrice(dto.getPrice());
        material.setIsNegotiable(dto.getIsNegotiable() != null ? dto.getIsNegotiable() : true);
        material.setMaterialCondition(dto.getCondition() != null ? dto.getCondition() : "Good");
        material.setPickupLocation(dto.getLocation().trim());
        material.setCity(dto.getCity() != null ? dto.getCity() : "Bengaluru");
        material.setPostalCode(dto.getPostalCode());
        material.setEstimatedWeightKg(dto.getEstimatedWeightKg() != null ? dto.getEstimatedWeightKg() : BigDecimal.ZERO);
        material.setAvailabilityStatus("AVAILABLE");

        // Primary Image
        String imageUrl = StringUtils.hasText(dto.getImageUrl())
                ? dto.getImageUrl()
                : "photo-1504307651254-35680f356dfd";
        material.addImage(new MaterialImage(imageUrl, true, 1));

        Material saved = materialRepository.save(material);
        return new MaterialResponseDTO(saved);
    }

    @Transactional
    public MaterialResponseDTO updateMaterial(Long id, MaterialRequestDTO dto, Long authenticatedUserId) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found with id: " + id));

        User currentUser = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + authenticatedUserId));

        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_ADMIN"));

        // Seller ownership check: prevent editing another seller's listing
        if (!material.getSeller().getId().equals(authenticatedUserId) && !isAdmin) {
            throw new UnauthorizedException("You are not authorized to edit another seller's listing");
        }

        if (dto.getCategoryId() != null) {
            MaterialCategory category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new BadRequestException("Category not found with id: " + dto.getCategoryId()));
            material.setCategory(category);
        } else if (StringUtils.hasText(dto.getCategory())) {
            MaterialCategory category = categoryRepository.findByNameIgnoreCase(dto.getCategory().trim())
                    .orElse(material.getCategory());
            material.setCategory(category);
        }

        if (StringUtils.hasText(dto.getTitle())) {
            material.setTitle(dto.getTitle().trim());
        }
        if (dto.getDescription() != null) {
            material.setDescription(dto.getDescription());
        }
        if (StringUtils.hasText(dto.getQuantity())) {
            material.setQuantity(dto.getQuantity().trim());
        }
        if (dto.getPrice() != null) {
            if (dto.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Price cannot be negative");
            }
            material.setPrice(dto.getPrice());
        }
        if (StringUtils.hasText(dto.getLocation())) {
            material.setPickupLocation(dto.getLocation().trim());
        }
        if (dto.getCondition() != null) {
            material.setMaterialCondition(dto.getCondition());
        }
        if (dto.getIsNegotiable() != null) {
            material.setIsNegotiable(dto.getIsNegotiable());
        }

        Material updated = materialRepository.save(material);
        return new MaterialResponseDTO(updated);
    }

    @Transactional
    public void deleteMaterial(Long id, Long authenticatedUserId) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found with id: " + id));

        User currentUser = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + authenticatedUserId));

        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_ADMIN"));

        // Seller ownership check: prevent deleting another seller's listing
        if (!material.getSeller().getId().equals(authenticatedUserId) && !isAdmin) {
            throw new UnauthorizedException("You are not authorized to delete another seller's listing");
        }

        material.setAvailabilityStatus("INACTIVE");
        materialRepository.save(material);
    }

    @Transactional
    public MaterialResponseDTO uploadMaterialImage(Long materialId, MultipartFile file, Long authenticatedUserId) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found with id: " + materialId));

        User currentUser = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + authenticatedUserId));

        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_ADMIN"));

        if (!material.getSeller().getId().equals(authenticatedUserId) && !isAdmin) {
            throw new UnauthorizedException("You are not authorized to upload images for this listing");
        }

        String storedPath = fileStorageService.storeFile(file);

        boolean isFirstImage = material.getImages().isEmpty();
        MaterialImage imageEntity = new MaterialImage(storedPath, isFirstImage, material.getImages().size() + 1);
        material.addImage(imageEntity);

        Material saved = materialRepository.save(material);
        return new MaterialResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public Page<MaterialResponseDTO> getMyListings(Long sellerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return materialRepository.findBySellerId(sellerId, pageable).map(MaterialResponseDTO::new);
    }
}
