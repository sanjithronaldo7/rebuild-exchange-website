package com.rebuildexchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebuildexchange.dto.*;
import com.rebuildexchange.entity.*;
import com.rebuildexchange.repository.*;
import com.rebuildexchange.security.JwtTokenProvider;
import com.rebuildexchange.security.UserPrincipal;
import com.rebuildexchange.service.PaymentService;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class OrderPaymentAndFeedbackTest {

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
    private OrderRepository orderRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PaymentService paymentService;

    private User buyer;
    private User seller;
    private Material material;
    private String buyerToken;
    private String sellerToken;

    @BeforeEach
    void setUp() {
        Role buyerRole = roleRepository.findByName("ROLE_BUYER").orElseGet(() ->
                roleRepository.save(new Role("ROLE_BUYER", "Buyer")));
        Role sellerRole = roleRepository.findByName("ROLE_SELLER").orElseGet(() ->
                roleRepository.save(new Role("ROLE_SELLER", "Seller")));

        buyer = userRepository.save(new User("Test Buyer", "testbuyer@test.com", "hash", "+91 9999000001"));
        buyer.getRoles().add(buyerRole);
        buyer = userRepository.save(buyer);
        buyerToken = "Bearer " + jwtTokenProvider.generateTokenFromUser(UserPrincipal.create(buyer));

        seller = userRepository.save(new User("Test Seller", "testseller@test.com", "hash", "+91 9999000002"));
        seller.getRoles().add(sellerRole);
        seller = userRepository.save(seller);
        sellerToken = "Bearer " + jwtTokenProvider.generateTokenFromUser(UserPrincipal.create(seller));

        MaterialCategory cat = categoryRepository.findBySlug("wood").orElseGet(() ->
                categoryRepository.save(new MaterialCategory("Wood", "wood", "⌁", "Timber & Wood", 2)));

        material = new Material();
        material.setSeller(seller);
        material.setCategory(cat);
        material.setTitle("Teak Planks");
        material.setQuantity("12 boards");
        material.setPrice(new BigDecimal("3500.00"));
        material.setMaterialCondition("Good");
        material.setPickupLocation("Indiranagar, Bengaluru");
        material.setAvailabilityStatus("AVAILABLE");
        material = materialRepository.save(material);
    }

    @Test
    @DisplayName("1. Should create order and generate order tracking history")
    void testCreateOrder() throws Exception {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setMaterialId(material.getId());
        req.setAgreedPrice(new BigDecimal("3500.00"));
        req.setPickupNotes("Picking up via small van on Friday");

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.orderNumber", startsWith("RBX-")))
                .andExpect(jsonPath("$.data.orderStatus", is("PAYMENT_PENDING")));
    }

    @Test
    @DisplayName("2. Should allow seller to update fulfillment status to READY_FOR_PICKUP")
    void testUpdateOrderStatus() throws Exception {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setMaterialId(material.getId());
        req.setAgreedPrice(BigDecimal.ZERO); // Free item starts as READY_FOR_PICKUP

        Order order = new Order();
        order.setOrderNumber("RBX-TEST-001");
        order.setMaterial(material);
        order.setBuyer(buyer);
        order.setSeller(seller);
        order.setAgreedPrice(BigDecimal.ZERO);
        order.setOrderStatus("READY_FOR_PICKUP");
        order = orderRepository.save(order);

        UpdateOrderStatusRequest updateReq = new UpdateOrderStatusRequest("COMPLETED", "Picked up by buyer successfully");

        mockMvc.perform(patch("/api/orders/" + order.getId() + "/status")
                        .header("Authorization", sellerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderStatus", is("COMPLETED")));

        // Verify material availability was transitioned to REHOMED
        Material updatedMat = materialRepository.findById(material.getId()).orElseThrow();
        assertEquals("REHOMED", updatedMat.getAvailabilityStatus());
    }

    @Test
    @DisplayName("3. Should reject fake or tampered Razorpay signatures with 400 Bad Request")
    void testRejectInvalidPaymentSignature() throws Exception {
        Order order = new Order();
        order.setOrderNumber("RBX-TEST-002");
        order.setMaterial(material);
        order.setBuyer(buyer);
        order.setSeller(seller);
        order.setAgreedPrice(new BigDecimal("1000.00"));
        order.setOrderStatus("PAYMENT_PENDING");
        order = orderRepository.save(order);

        // Initiate payment order
        PaymentResponseDTO paymentOrder = paymentService.createPaymentOrder(order.getId(), buyer.getId());

        VerifyPaymentRequest fakeVerify = new VerifyPaymentRequest();
        fakeVerify.setOrderId(order.getId());
        fakeVerify.setRazorpayOrderId(paymentOrder.getGatewayOrderId());
        fakeVerify.setRazorpayPaymentId("pay_fake_12345");
        fakeVerify.setRazorpaySignature("invalid_forged_signature_hash");

        mockMvc.perform(post("/api/payments/verify")
                        .header("Authorization", buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fakeVerify)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Invalid signature")));
    }

    @Test
    @DisplayName("4. Should submit feedback for completed order and prevent duplicate submissions")
    void testFeedbackSubmissionAndDuplicatePrevention() throws Exception {
        Order order = new Order();
        order.setOrderNumber("RBX-TEST-003");
        order.setMaterial(material);
        order.setBuyer(buyer);
        order.setSeller(seller);
        order.setAgreedPrice(new BigDecimal("2000.00"));
        order.setOrderStatus("COMPLETED");
        order = orderRepository.save(order);

        FeedbackRequestDTO feedbackReq = new FeedbackRequestDTO();
        feedbackReq.setOrderId(order.getId());
        feedbackReq.setRating(5);
        feedbackReq.setComment("High quality timber planks, very helpful seller!");

        // First submission succeeds
        mockMvc.perform(post("/api/feedback")
                        .header("Authorization", buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(feedbackReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.rating", is(5)));

        // Duplicate submission is rejected
        mockMvc.perform(post("/api/feedback")
                        .header("Authorization", buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(feedbackReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("already been submitted")));
    }

    @Test
    @DisplayName("5. Should process chatbot query and return informative response")
    void testChatbotEndpoint() throws Exception {
        ChatMessageRequest chatReq = new ChatMessageRequest("How do I list materials?", null);

        mockMvc.perform(post("/api/chatbot/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chatReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.reply", notNullValue()))
                .andExpect(jsonPath("$.data.sessionId", notNullValue()));
    }
}
