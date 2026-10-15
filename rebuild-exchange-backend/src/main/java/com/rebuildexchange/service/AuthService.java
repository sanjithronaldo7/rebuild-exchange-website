package com.rebuildexchange.service;

import com.rebuildexchange.dto.AuthResponse;
import com.rebuildexchange.dto.LoginRequest;
import com.rebuildexchange.dto.RegisterRequest;
import com.rebuildexchange.dto.VerifyOtpRequest;
import com.rebuildexchange.entity.OtpVerification;
import com.rebuildexchange.entity.Role;
import com.rebuildexchange.entity.User;
import com.rebuildexchange.exception.BadRequestException;
import com.rebuildexchange.exception.ResourceNotFoundException;
import com.rebuildexchange.exception.UnauthorizedException;
import com.rebuildexchange.repository.OtpVerificationRepository;
import com.rebuildexchange.repository.RoleRepository;
import com.rebuildexchange.repository.UserRepository;
import com.rebuildexchange.security.JwtTokenProvider;
import com.rebuildexchange.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OtpVerificationRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final EmailService emailService;

    @Value("${app.otp.expiration-minutes:10}")
    private int otpExpirationMinutes;

    @Value("${app.otp.max-attempts:5}")
    private int maxAttempts;

    @Value("${app.otp.resend-cooldown-seconds:60}")
    private int resendCooldownSeconds;

    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       OtpVerificationRepository otpRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.otpRepository = otpRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.emailService = emailService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String email = req.getEmail().trim().toLowerCase();

        // 1. Duplicate email prevention
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("An account with this email address already exists");
        }

        // 2. Prevent self-assigning ADMIN role
        if (req.getRole() != null && req.getRole().toUpperCase().contains("ADMIN")) {
            throw new BadRequestException("Self-registration as administrator is strictly forbidden");
        }

        // 3. Resolve role (BUYER or SELLER)
        final String roleName = (req.getRole() != null && req.getRole().toUpperCase().contains("SELLER"))
                ? "ROLE_SELLER"
                : "ROLE_BUYER";

        Role assignedRole = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));

        // 4. Hash password with BCrypt
        String passwordHash = passwordEncoder.encode(req.getPassword());

        // 5. Create user entity
        User user = new User(req.getFullName().trim(), email, passwordHash, req.getPhoneNumber());
        user.getRoles().add(assignedRole);
        user.setAccountStatus("ACTIVE");
        user.setIsEmailVerified(false);
        userRepository.save(user);

        // 6. Generate and send email OTP
        generateAndSendOtp(email, "REGISTRATION");

        return AuthResponse.otpRequired(email, "Registration successful. A 6-digit OTP verification code has been dispatched to " + email);
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        String email = req.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password credentials"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password credentials");
        }

        if ("SUSPENDED".equalsIgnoreCase(user.getAccountStatus())) {
            throw new UnauthorizedException("Your account has been suspended. Please contact hello@rebuild.exchange");
        }

        UserPrincipal principal = UserPrincipal.create(user);
        String jwtToken = tokenProvider.generateTokenFromUser(principal);

        Set<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());

        return new AuthResponse(jwtToken, user.getId(), user.getFullName(), user.getEmail(), roles);
    }

    @Transactional
    public AuthResponse verifyOtp(VerifyOtpRequest req) {
        String email = req.getEmail().trim().toLowerCase();
        String purpose = req.getPurpose() != null ? req.getPurpose().toUpperCase() : "REGISTRATION";

        OtpVerification otp = otpRepository.findTopByEmailAndPurposeAndIsConsumedFalseOrderByCreatedAtDesc(email, purpose)
                .orElseThrow(() -> new BadRequestException("No active OTP found for this email. Please request a new code."));

        // Check if exceeded attempts
        if (otp.getAttemptCount() >= otp.getMaxAttempts()) {
            throw new BadRequestException("Maximum OTP verification attempts exceeded. Please request a new code.");
        }

        // Check expiration
        if (otp.isExpired()) {
            throw new BadRequestException("The OTP code has expired. Please request a new code.");
        }

        // Validate code
        if (!otp.getOtpCode().equals(req.getOtpCode().trim())) {
            otp.setAttemptCount(otp.getAttemptCount() + 1);
            otpRepository.save(otp);
            int remaining = otp.getMaxAttempts() - otp.getAttemptCount();
            throw new BadRequestException("Invalid OTP code. " + remaining + " attempts remaining.");
        }

        // Mark OTP consumed
        otp.setIsConsumed(true);
        otpRepository.save(otp);

        // Mark user email verified
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        user.setIsEmailVerified(true);
        userRepository.save(user);

        UserPrincipal principal = UserPrincipal.create(user);
        String jwtToken = tokenProvider.generateTokenFromUser(principal);

        Set<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());

        return new AuthResponse(jwtToken, user.getId(), user.getFullName(), user.getEmail(), roles);
    }

    @Transactional
    public String resendOtp(String email, String purpose) {
        email = email.trim().toLowerCase();
        purpose = purpose != null ? purpose.toUpperCase() : "REGISTRATION";

        Optional<OtpVerification> lastOtpOpt = otpRepository.findTopByEmailAndPurposeAndIsConsumedFalseOrderByCreatedAtDesc(email, purpose);
        if (lastOtpOpt.isPresent()) {
            OtpVerification lastOtp = lastOtpOpt.get();
            long secondsSinceCreation = Duration.between(lastOtp.getCreatedAt(), LocalDateTime.now()).getSeconds();
            if (secondsSinceCreation < resendCooldownSeconds) {
                long waitTime = resendCooldownSeconds - secondsSinceCreation;
                throw new BadRequestException("Please wait " + waitTime + " seconds before requesting a new verification code.");
            }
            // Invalidate prior code
            lastOtp.setIsConsumed(true);
            otpRepository.save(lastOtp);
        }

        generateAndSendOtp(email, purpose);
        return "A new verification code has been dispatched to " + email;
    }

    private void generateAndSendOtp(String email, String purpose) {
        // SecureRandom 6-digit code
        int code = 100000 + secureRandom.nextInt(900000);
        String otpCode = String.valueOf(code);

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(otpExpirationMinutes);

        OtpVerification otp = new OtpVerification(email, otpCode, purpose, expiresAt);
        otp.setMaxAttempts(maxAttempts);
        otpRepository.save(otp);

        emailService.sendOtpEmail(email, otpCode, purpose);
    }
}
