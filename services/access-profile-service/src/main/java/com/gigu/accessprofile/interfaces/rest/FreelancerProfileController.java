package com.gigu.accessprofile.interfaces.rest;

import com.gigu.accessprofile.application.dto.UpdateProfileCommand;
import com.gigu.accessprofile.application.dto.UploadPortfolioCommand;
import com.gigu.accessprofile.application.service.AccessProfileApplicationService;
import com.gigu.accessprofile.domain.model.User;
import com.gigu.accessprofile.interfaces.rest.dto.UpdateProfileRequest;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/access/freelancer-profiles")
public class FreelancerProfileController {
    private final AccessProfileApplicationService service;

    public FreelancerProfileController(AccessProfileApplicationService service) { this.service = service; }

    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getPublic(@PathVariable UUID userId) {
        var p = service.publicFreelancerProfile(userId);
        var items = p.portfolioItems().stream().map(i -> Map.of("id", i.id(), "title", i.title(), "description", i.description(), "mediaUrl", i.publicUrl(), "createdAt", DateTimeFormatter.ISO_INSTANT.format(i.createdAt()))).toList();
        return ResponseEntity.ok(Map.of("profileId", p.profileId(), "userId", p.userId(), "displayName", p.displayName(), "bio", p.bio(), "skills", p.skills(), "academicVerificationStatus", p.academicVerificationStatus(), "portfolioItems", items));
    }

    @PatchMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, Object>> updateMe(Authentication authentication, @Valid @RequestBody UpdateProfileRequest request) {
        User user = (User) authentication.getPrincipal();
        var updated = service.updateMyFreelancerProfile(user.id(), user.roles(), new UpdateProfileCommand(request.bio(), request.skills()));
        return ResponseEntity.ok(Map.of("profileId", updated.profileId(), "updated", true, "updatedAt", DateTimeFormatter.ISO_INSTANT.format(updated.updatedAt())));
    }

    @PostMapping(value = "/me/portfolio-items", consumes = "multipart/form-data")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, Object>> addPortfolio(Authentication authentication, @RequestParam String title, @RequestParam String description, @RequestParam("file") MultipartFile file) throws Exception {
        User user = (User) authentication.getPrincipal();
        var item = service.addMyPortfolioItem(user.id(), user.roles(), new UploadPortfolioCommand(title, description, file.getContentType(), file.getSize(), file.getBytes()));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", item.id(), "title", item.title(), "description", item.description(), "mediaUrl", item.publicUrl(), "createdAt", DateTimeFormatter.ISO_INSTANT.format(item.createdAt())));
    }

    @DeleteMapping("/me/portfolio-items/{itemId}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deletePortfolio(Authentication authentication, @PathVariable UUID itemId) {
        User user = (User) authentication.getPrincipal();
        service.deleteMyPortfolioItem(user.id(), user.roles(), itemId);
        return ResponseEntity.noContent().build();
    }
}
