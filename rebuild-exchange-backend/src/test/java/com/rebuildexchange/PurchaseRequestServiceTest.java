package com.rebuildexchange;

import com.rebuildexchange.dto.PurchaseRequestCreateDTO;
import com.rebuildexchange.dto.PurchaseRequestResponseDTO;
import com.rebuildexchange.dto.UpdateRequestStatusDTO;
import com.rebuildexchange.entity.Material;
import com.rebuildexchange.entity.MaterialCategory;
import com.rebuildexchange.entity.User;
import com.rebuildexchange.exception.BadRequestException;
import com.rebuildexchange.exception.UnauthorizedException;
import com.rebuildexchange.repository.MaterialCategoryRepository;
import com.rebuildexchange.repository.MaterialRepository;
import com.rebuildexchange.repository.UserRepository;
import com.rebuildexchange.service.PurchaseRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PurchaseRequestServiceTest {

    @Autowired
    private PurchaseRequestService purchaseRequestService;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private MaterialCategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private User seller;
    private User buyer;
    private Material material;

    @BeforeEach
    void setUp() {
        seller = userRepository.save(new User("Seller User", "seller.pr@test.com", "hash", "+91 9999911111"));
        buyer = userRepository.save(new User("Buyer User", "buyer.pr@test.com", "hash", "+91 9999922222"));

        MaterialCategory category = categoryRepository.findBySlug("tiles").orElseGet(() ->
                categoryRepository.save(new MaterialCategory("Tiles", "tiles", "▧", "Tiles desc", 1)));

        material = new Material();
        material.setSeller(seller);
        material.setCategory(category);
        material.setTitle("Ceramic Floor Tiles");
        material.setDescription("Good condition surplus");
        material.setQuantity("50 pieces");
        material.setPrice(new BigDecimal("1500.00"));
        material.setMaterialCondition("Good");
        material.setPickupLocation("Indiranagar, Bengaluru");
        material.setAvailabilityStatus("AVAILABLE");
        material = materialRepository.save(material);
    }

    @Test
    @DisplayName("Should successfully create purchase request when valid buyer requests available material")
    void testCreatePurchaseRequestSuccess() {
        PurchaseRequestCreateDTO dto = new PurchaseRequestCreateDTO();
        dto.setMaterialId(material.getId());
        dto.setRequestedQuantity("20 pieces");
        dto.setOfferedPrice(new BigDecimal("1500.00"));
        dto.setMessage("Can pick up this Saturday afternoon");

        PurchaseRequestResponseDTO res = purchaseRequestService.createPurchaseRequest(dto, buyer.getId());

        assertNotNull(res.getId());
        assertEquals("PENDING", res.getStatus());
        assertEquals("Ceramic Floor Tiles", res.getMaterialTitle());
        assertEquals(buyer.getId(), res.getBuyerId());
        assertEquals(seller.getId(), res.getSellerId());
    }

    @Test
    @DisplayName("Should prevent seller from requesting their own material")
    void testPreventSellerRequestingOwnMaterial() {
        PurchaseRequestCreateDTO dto = new PurchaseRequestCreateDTO();
        dto.setMaterialId(material.getId());
        dto.setRequestedQuantity("10 pieces");

        assertThrows(BadRequestException.class, () ->
                purchaseRequestService.createPurchaseRequest(dto, seller.getId()));
    }

    @Test
    @DisplayName("Should allow seller to accept incoming purchase request")
    void testSellerAcceptsRequest() {
        PurchaseRequestCreateDTO dto = new PurchaseRequestCreateDTO();
        dto.setMaterialId(material.getId());
        dto.setRequestedQuantity("20 pieces");
        PurchaseRequestResponseDTO created = purchaseRequestService.createPurchaseRequest(dto, buyer.getId());

        UpdateRequestStatusDTO updateDto = new UpdateRequestStatusDTO("ACCEPTED", "Agreed! You can pick up between 10am-2pm.");
        PurchaseRequestResponseDTO updated = purchaseRequestService.updateRequestStatus(created.getId(), updateDto, seller.getId());

        assertEquals("ACCEPTED", updated.getStatus());
        assertEquals("Agreed! You can pick up between 10am-2pm.", updated.getSellerResponseNotes());
    }

    @Test
    @DisplayName("Should prevent unauthorized third party from modifying purchase request")
    void testUnauthorizedUserCannotModifyRequest() {
        User thirdParty = userRepository.save(new User("Stranger", "stranger@test.com", "hash", "+91 9999933333"));

        PurchaseRequestCreateDTO dto = new PurchaseRequestCreateDTO();
        dto.setMaterialId(material.getId());
        dto.setRequestedQuantity("10 pieces");
        PurchaseRequestResponseDTO created = purchaseRequestService.createPurchaseRequest(dto, buyer.getId());

        UpdateRequestStatusDTO updateDto = new UpdateRequestStatusDTO("ACCEPTED", "Trying to accept");

        assertThrows(UnauthorizedException.class, () ->
                purchaseRequestService.updateRequestStatus(created.getId(), updateDto, thirdParty.getId()));
    }
}
