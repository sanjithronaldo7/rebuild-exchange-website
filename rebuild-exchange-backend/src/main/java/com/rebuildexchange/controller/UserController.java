package com.rebuildexchange.controller;

import com.rebuildexchange.dto.ApiResponse;
import com.rebuildexchange.dto.UserProfileDTO;
import com.rebuildexchange.entity.User;
import com.rebuildexchange.exception.ResourceNotFoundException;
import com.rebuildexchange.repository.UserRepository;
import com.rebuildexchange.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "Endpoints for managing user accounts and profiles")
@SecurityRequirement(name = "BearerAuth")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get the authenticated user's profile")
    public ResponseEntity<ApiResponse<UserProfileDTO>> getCurrentUser(@AuthenticationPrincipal UserPrincipal currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUser.getId()));
        return ResponseEntity.ok(ApiResponse.success(new UserProfileDTO(user)));
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update the authenticated user's profile details")
    public ResponseEntity<ApiResponse<UserProfileDTO>> updateCurrentUser(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody UserProfileDTO updateDto) {

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUser.getId()));

        if (updateDto.getFullName() != null) {
            user.setFullName(updateDto.getFullName().trim());
        }
        if (updateDto.getPhoneNumber() != null) {
            user.setPhoneNumber(updateDto.getPhoneNumber().trim());
        }
        if (updateDto.getBio() != null) {
            user.setBio(updateDto.getBio());
        }
        if (updateDto.getAvatarUrl() != null) {
            user.setAvatarUrl(updateDto.getAvatarUrl());
        }

        User saved = userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", new UserProfileDTO(saved)));
    }
}
