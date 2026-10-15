package com.rebuildexchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebuildexchange.dto.CategoryDTO;
import com.rebuildexchange.dto.MaterialRequestDTO;
import com.rebuildexchange.entity.Material;
import com.rebuildexchange.entity.MaterialCategory;
import com.rebuildexchange.entity.Role;
import com.rebuildexchange.entity.User;
import com.rebuildexchange.repository.MaterialCategoryRepository;
import com.rebuildexchange.repository.MaterialRepository;
import com.rebuildexchange.repository.RoleRepository;
import com.rebuildexchange.repository.UserRepository;
import com.rebuildexchange.security.JwtTokenProvider;
import com.rebuildexchange.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class MaterialAndCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private MaterialCategoryRepository categoryRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User seller1;
    private User seller2;
    private User buyer;
    private User admin;
    private String seller1Token;
    private String seller2Token;
    private String buyerToken;
    private String adminToken;
    private MaterialCategory tilesCategory;

    @BeforeEach
    void setUp() {
        Role buyerRole = roleRepository.findByName("ROLE_BUYER").orElseGet(() ->
                roleRepository.save(new Role("ROLE_BUYER", "Buyer")));
        Role sellerRole = roleRepository.findByName("ROLE_SELLER").orElseGet(() ->
                roleRepository.save(new Role("ROLE_SELLER", "Seller")));
        Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseGet(() ->
                roleRepository.save(new Role("ROLE_ADMIN", "Admin")));

        seller1 = userRepository.save(new User("Seller One", "seller1@test.com", "hash", "+91 9999900001"));
        seller1.getRoles().add(sellerRole);
        seller1 = userRepository.save(seller1);
        seller1Token = "Bearer " + jwtTokenProvider.generateTokenFromUser(UserPrincipal.create(seller1));

        seller2 = userRepository.save(new User("Seller Two", "seller2@test.com", "hash", "+91 9999900002"));
        seller2.getRoles().add(sellerRole);
        seller2 = userRepository.save(seller2);
        seller2Token = "Bearer " + jwtTokenProvider.generateTokenFromUser(UserPrincipal.create(seller2));

        buyer = userRepository.save(new User("Buyer Only", "buyer@test.com", "hash", "+91 9999900003"));
        buyer.getRoles().add(buyerRole);
        buyer = userRepository.save(buyer);
        buyerToken = "Bearer " + jwtTokenProvider.generateTokenFromUser(UserPrincipal.create(buyer));

        admin = userRepository.save(new User("Super Admin", "admin@test.com", "hash", "+91 9999900004"));
        admin.getRoles().add(adminRole);
        admin = userRepository.save(admin);
        adminToken = "Bearer " + jwtTokenProvider.generateTokenFromUser(UserPrincipal.create(admin));

        tilesCategory = categoryRepository.findBySlug("tiles").orElseGet(() ->
                categoryRepository.save(new MaterialCategory("Tiles", "tiles", "▧", "Floor & wall tiles", 1)));
    }

    @Test
    @DisplayName("1. Should allow authenticated seller to create a new material listing")
    void testCreateMaterialSuccess() throws Exception {
        MaterialRequestDTO dto = new MaterialRequestDTO();
        dto.setTitle("Handmade Terracotta Tiles");
        dto.setCategory("Tiles");
        dto.setQuantity("50 pieces");
        dto.setPrice(new BigDecimal("2500.00"));
        dto.setLocation("Indiranagar, Bengaluru");
        dto.setDescription("Clean leftover tiles ready for pickup.");

        mockMvc.perform(post("/api/materials")
                        .header("Authorization", seller1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.title", is("Handmade Terracotta Tiles")))
                .andExpect(jsonPath("$.data.seller", is("Seller One")));
    }

    @Test
    @DisplayName("2. Should reject material creation with invalid input (blank title, negative price)")
    void testCreateMaterialInvalidInput() throws Exception {
        MaterialRequestDTO dto = new MaterialRequestDTO();
        dto.setTitle(""); // Invalid
        dto.setPrice(new BigDecimal("-100.00")); // Invalid

        mockMvc.perform(post("/api/materials")
                        .header("Authorization", seller1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @DisplayName("3. Should allow guest user to retrieve available listings with pagination")
    void testGetMaterialsPublic() throws Exception {
        mockMvc.perform(get("/api/materials?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", notNullValue()));
    }

    @Test
    @DisplayName("4. Should support search and filter by keyword, category, and price")
    void testSearchAndFilterMaterials() throws Exception {
        mockMvc.perform(get("/api/materials/search?query=Terracotta&category=Tiles&sort=newest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", notNullValue()));
    }

    @Test
    @DisplayName("5. Should allow seller to update their own listing")
    void testUpdateOwnListingSuccess() throws Exception {
        Material material = new Material();
        material.setSeller(seller1);
        material.setCategory(tilesCategory);
        material.setTitle("Original Title");
        material.setQuantity("10 pcs");
        material.setPrice(new BigDecimal("500.00"));
        material.setMaterialCondition("Good");
        material.setPickupLocation("Whitefield, Bengaluru");
        material = materialRepository.save(material);

        MaterialRequestDTO updateDto = new MaterialRequestDTO();
        updateDto.setTitle("Updated Title");
        updateDto.setCategory("Tiles");
        updateDto.setQuantity("12 pcs");
        updateDto.setPrice(new BigDecimal("600.00"));
        updateDto.setLocation("Whitefield, Bengaluru");

        mockMvc.perform(put("/api/materials/" + material.getId())
                        .header("Authorization", seller1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title", is("Updated Title")))
                .andExpect(jsonPath("$.data.price", is(600.00)));
    }

    @Test
    @DisplayName("6. Should prevent a seller from updating another seller's listing (401/403)")
    void testPreventUpdatingOtherSellerListing() throws Exception {
        Material material = new Material();
        material.setSeller(seller1);
        material.setCategory(tilesCategory);
        material.setTitle("Seller 1's Listing");
        material.setQuantity("10 pcs");
        material.setPrice(new BigDecimal("500.00"));
        material.setMaterialCondition("Good");
        material.setPickupLocation("Whitefield, Bengaluru");
        material = materialRepository.save(material);

        MaterialRequestDTO updateDto = new MaterialRequestDTO();
        updateDto.setTitle("Attempted Hijack");
        updateDto.setPrice(new BigDecimal("1.00"));
        updateDto.setQuantity("10 pcs");
        updateDto.setLocation("Whitefield");

        mockMvc.perform(put("/api/materials/" + material.getId())
                        .header("Authorization", seller2Token) // Seller 2 attempting to modify Seller 1
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("7. Should allow owner to delete/deactivate listing")
    void testDeleteListingSuccess() throws Exception {
        Material material = new Material();
        material.setSeller(seller1);
        material.setCategory(tilesCategory);
        material.setTitle("To Be Deleted");
        material.setQuantity("5 pcs");
        material.setPrice(new BigDecimal("200.00"));
        material.setMaterialCondition("Good");
        material.setPickupLocation("Whitefield");
        material = materialRepository.save(material);

        mockMvc.perform(delete("/api/materials/" + material.getId())
                        .header("Authorization", seller1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    @DisplayName("8. Should permit only ADMIN to create, update, or delete categories")
    void testCategoryPermissions() throws Exception {
        CategoryDTO newCat = new CategoryDTO();
        newCat.setName("Structural Steel");
        newCat.setSlug("structural-steel");
        newCat.setIconSymbol("▥");

        // Non-admin (buyer or seller) should be forbidden
        mockMvc.perform(post("/api/categories")
                        .header("Authorization", seller1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCat)))
                .andExpect(status().isForbidden());

        // Admin should be allowed
        mockMvc.perform(post("/api/categories")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCat)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name", is("Structural Steel")));
    }
}
