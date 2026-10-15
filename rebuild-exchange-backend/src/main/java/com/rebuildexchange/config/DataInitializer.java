package com.rebuildexchange.config;

import com.rebuildexchange.entity.*;
import com.rebuildexchange.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MaterialCategoryRepository categoryRepository;
    private final MaterialRepository materialRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           RoleRepository roleRepository,
                           MaterialCategoryRepository categoryRepository,
                           MaterialRepository materialRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.categoryRepository = categoryRepository;
        this.materialRepository = materialRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // Ensure default roles exist
        Role buyerRole = roleRepository.findByName("ROLE_BUYER").orElseGet(() ->
                roleRepository.save(new Role("ROLE_BUYER", "Registered Buyer")));
        Role sellerRole = roleRepository.findByName("ROLE_SELLER").orElseGet(() ->
                roleRepository.save(new Role("ROLE_SELLER", "Verified Seller")));
        Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseGet(() ->
                roleRepository.save(new Role("ROLE_ADMIN", "Platform Administrator")));

        // Ensure default admin & demo seller exist
        User demoSeller = userRepository.findByEmail("seller@rebuild.exchange").orElseGet(() -> {
            User seller = new User("Maya Studio Build", "seller@rebuild.exchange", passwordEncoder.encode("Password@123"), "+91 98765 43210");
            seller.getRoles().add(sellerRole);
            seller.getRoles().add(buyerRole);
            seller.setIsEmailVerified(true);
            seller.setBio("Thoughtful local builders specialising in adaptive reuse.");
            return userRepository.save(seller);
        });

        userRepository.findByEmail("admin@rebuild.exchange").orElseGet(() -> {
            User admin = new User("Platform Administrator", "admin@rebuild.exchange", passwordEncoder.encode("Admin@12345"), "+91 98765 00000");
            admin.getRoles().add(adminRole);
            admin.getRoles().add(sellerRole);
            admin.getRoles().add(buyerRole);
            admin.setIsEmailVerified(true);
            return userRepository.save(admin);
        });

        // Seed 16 marketplace materials if repository is empty
        if (materialRepository.count() == 0) {
            logger.info("Seeding initial 16 construction materials from frontend catalog into MySQL...");

            seedItem(demoSeller, "Tiles", "Handmade terracotta floor tiles",
                    "Warm terracotta tiles left over from a thoughtful renovation. Clean, carefully stored and ready for a smaller space, patio or feature wall.",
                    "48 pieces", "pieces", new BigDecimal("120.00"), new BigDecimal("2400.00"), "Excellent",
                    "Indiranagar, Bengaluru", "photo-1600607687920-4e2a09cf159d");

            seedItem(demoSeller, "Wood", "Reclaimed teak wood planks",
                    "Beautiful aged teak recovered from a home renovation. A few characterful nail marks, otherwise sound and dry. Great for shelving or furniture.",
                    "12 boards · 18 sq ft", "boards", new BigDecimal("85.00"), new BigDecimal("6800.00"), "Very good",
                    "Koramangala, Bengaluru", "photo-1519710164239-da123dc03ef4");

            seedItem(demoSeller, "Paint", "Leftover exterior wall paint",
                    "Three unopened tins of weatherproof exterior paint in a soft sage green. Free to collect — would love to see it put to use.",
                    "3 × 10 litre tins", "tins", new BigDecimal("35.00"), BigDecimal.ZERO, "New · sealed",
                    "Jayanagar, Bengaluru", "photo-1562259949-e8e7689d7828");

            seedItem(demoSeller, "Bricks", "Exposed red clay bricks",
                    "Solid red clay bricks from an unused garden wall. Stack is on a pallet and accessible for pickup. Some mortar on a few pieces.",
                    "Approx. 180 bricks", "bricks", new BigDecimal("540.00"), new BigDecimal("1200.00"), "Good",
                    "Whitefield, Bengaluru", "photo-1590725140246-20acdee442be");

            seedItem(demoSeller, "Electrical", "Brushed brass wall sconces",
                    "Six elegant brushed brass wall lights, unused after a design change. Includes mounting plates. Warm LED compatible.",
                    "6 fixtures", "fixtures", new BigDecimal("12.00"), new BigDecimal("5400.00"), "Like new",
                    "HSR Layout, Bengaluru", "photo-1507473885765-e6ed057f782c");

            seedItem(demoSeller, "Tiles", "Natural stone paving offcuts",
                    "Mixed natural stone offcuts in earthy tones. Perfect for a small path, garden border or mosaic project. Collection only.",
                    "22 pieces · 9 sq ft", "pieces", new BigDecimal("95.00"), new BigDecimal("900.00"), "Good",
                    "Basavanagudi, Bengaluru", "photo-1515263487990-61b07816b324");

            seedItem(demoSeller, "Plumbing", "Copper plumbing pipe lengths",
                    "Clean copper pipe offcuts from a commercial job. Multiple lengths between 1m and 2m. Stored indoors.",
                    "15 lengths · 24 m total", "lengths", new BigDecimal("45.00"), new BigDecimal("3100.00"), "Very good",
                    "Electronic City, Bengaluru", "photo-1621905251189-08b45d6a269e");

            seedItem(demoSeller, "Cement", "Cement bags — unopened",
                    "Eight recently purchased bags of OPC cement, stored off the floor in a dry garage. Best used soon. Pickup with a suitable vehicle.",
                    "8 × 50 kg bags", "bags", new BigDecimal("400.00"), new BigDecimal("1680.00"), "New · sealed",
                    "Yelahanka, Bengaluru", "photo-1513467655676-561b7d489a88");

            seedItem(demoSeller, "Wood", "Salvaged vintage teak door",
                    "A solid teak door with original brass hardware and a lovely patina. Removed carefully during a renovation and ready for restoration or reuse.",
                    "1 door · 210 × 90 cm", "unit", new BigDecimal("45.00"), new BigDecimal("4500.00"), "Good",
                    "Malleshwaram, Bengaluru", "photo-1600210492486-724fe5c67fb0");

            seedItem(demoSeller, "Plumbing", "Porcelain bathroom basins",
                    "Two white countertop basins, both in excellent condition. Includes chrome mixer taps and drain fittings. Collection preferred.",
                    "2 basins with taps", "sets", new BigDecimal("30.00"), new BigDecimal("2800.00"), "Like new",
                    "Marathahalli, Bengaluru", "photo-1620626011761-996317b8d101");

            seedItem(demoSeller, "Other", "Acoustic ceiling panels",
                    "Neutral felt acoustic panels removed from a meeting room fit-out. Clean, undamaged and easy to mount. Great for studios or home offices.",
                    "18 panels · 12 sq m", "panels", new BigDecimal("36.00"), new BigDecimal("3600.00"), "Very good",
                    "Rajajinagar, Bengaluru", "photo-1497366754035-f200968a6e72");

            seedItem(demoSeller, "Other", "Unused matte black door handles",
                    "Ten boxed lever handle sets from a design change. Includes matching latch plates and screws. All new and still in packaging.",
                    "10 sets", "sets", new BigDecimal("15.00"), new BigDecimal("1500.00"), "New · boxed",
                    "Bellandur, Bengaluru", "photo-1505693416388-ac5ce068fe85");

            seedItem(demoSeller, "Tiles", "Reclaimed granite countertop",
                    "Polished dark granite offcut from a kitchen remodel. One finished edge and a cutout for a small sink. A useful workshop or utility counter.",
                    "1 slab · 2.1 m", "slab", new BigDecimal("90.00"), new BigDecimal("2200.00"), "Good",
                    "Banashankari, Bengaluru", "photo-1600607687920-4e2a09cf159d");

            seedItem(demoSeller, "Electrical", "Weatherproof electrical conduit",
                    "Surplus grey PVC electrical conduit from a commercial project. Straight lengths, stored indoors. Suitable for a range of wiring projects.",
                    "32 lengths · 3 m each", "lengths", new BigDecimal("28.00"), new BigDecimal("1900.00"), "New · unused",
                    "Peenya, Bengaluru", "photo-1621905251189-08b45d6a269e");

            seedItem(demoSeller, "Tiles", "Terracotta roof tiles",
                    "Traditional curved roof tiles salvaged from a small extension. Many still have decades of use left. Pickup with a small truck.",
                    "Approx. 95 tiles", "tiles", new BigDecimal("240.00"), new BigDecimal("1700.00"), "Good",
                    "Vijayanagar, Bengaluru", "photo-1600607687939-ce8a6c25118c");

            seedItem(demoSeller, "Wood", "Hardwood flooring offcuts",
                    "Mixed lengths of solid hardwood flooring, clean and dry. Enough for a closet, landing or creative furniture project.",
                    "Approx. 14 sq ft", "sq ft", new BigDecimal("35.00"), new BigDecimal("3200.00"), "Very good",
                    "Frazer Town, Bengaluru", "photo-1519710164239-da123dc03ef4");

            logger.info("Successfully seeded 16 initial marketplace materials!");
        }
    }

    private void seedItem(User seller, String categoryName, String title, String description,
                          String quantity, String unit, BigDecimal weight, BigDecimal price,
                          String condition, String location, String unsplashId) {
        Optional<MaterialCategory> catOpt = categoryRepository.findByNameIgnoreCase(categoryName);
        if (catOpt.isEmpty()) return;

        Material m = new Material();
        m.setSeller(seller);
        m.setCategory(catOpt.get());
        m.setTitle(title);
        m.setDescription(description);
        m.setQuantity(quantity);
        m.setUnit(unit);
        m.setEstimatedWeightKg(weight);
        m.setPrice(price);
        m.setIsNegotiable(true);
        m.setMaterialCondition(condition);
        m.setPickupLocation(location);
        m.setCity("Bengaluru");
        m.setAvailabilityStatus("AVAILABLE");
        m.addImage(new MaterialImage(unsplashId, true, 1));
        materialRepository.save(m);
    }
}
