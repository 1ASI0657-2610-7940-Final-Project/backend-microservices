package com.gigu.accessprofile.interfaces.rest;

import com.gigu.accessprofile.application.dto.*;
import com.gigu.accessprofile.application.service.AccessProfileApplicationService;
import com.gigu.accessprofile.domain.model.User;
import com.gigu.accessprofile.interfaces.rest.dto.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/access")
public class AccessController {
    private final AccessProfileApplicationService service;
    private final String serviceToken;

    public AccessController(AccessProfileApplicationService service, @Value("${SERVICE_TOKEN:internal-token}") String serviceToken) {
        this.service = service;
        this.serviceToken = serviceToken;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<Map<String, Object>> signUp(@Valid @RequestBody SignUpRequest request) {
        User user = service.signUp(new SignUpCommand(request.firstName(), request.lastName(), request.email(), request.password(), request.role()));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", user.id(), "email", user.email(), "roles", user.roles().stream().map(Enum::name).toList(), "createdAt", DateTimeFormatter.ISO_INSTANT.format(user.createdAt())));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        var login = service.login(new LoginCommand(request.email(), request.password()));
        User user = login.user();
        return ResponseEntity.ok(Map.of("accessToken", login.token(), "tokenType", "Bearer", "expiresIn", 3600, "user", Map.of("id", user.id(), "email", user.email(), "roles", user.roles().stream().map(Enum::name).toList())));
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, Object>> me(Authentication authentication) {
        User authUser = (User) authentication.getPrincipal();
        User user = service.me(authUser.id());
        return ResponseEntity.ok(Map.of("id", user.id(), "firstName", user.firstName(), "lastName", user.lastName(), "email", user.email(), "roles", user.roles().stream().map(Enum::name).toList()));
    }

    @GetMapping("/internal/users/{userId}")
    public ResponseEntity<Map<String, Object>> internalUserSummary(
            @RequestHeader(name = "X-Service-Token", required = false) String token,
            @PathVariable UUID userId) {
        if (token == null || !token.equals(serviceToken)) {
            throw new SecurityException("forbidden");
        }

        User user = service.me(userId);
        String fullName = Stream.of(user.firstName(), user.lastName())
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .reduce((left, right) -> left + " " + right)
                .orElse("");

        String displayName = fullName;
        if (user.roles().contains(com.gigu.accessprofile.domain.valueobject.RoleName.FREELANCER)) {
            try {
                String freelancerDisplayName = service.publicFreelancerProfile(userId).displayName();
                if (freelancerDisplayName != null && !freelancerDisplayName.isBlank()) {
                    displayName = freelancerDisplayName.trim();
                }
            } catch (IllegalArgumentException ignored) {
                // Fall back to the user's own identity fields when a freelancer profile is missing.
            }
        }

        if (displayName == null || displayName.isBlank()) {
            displayName = fullName;
        }
        if (displayName == null || displayName.isBlank()) {
            displayName = user.email() != null && user.email().contains("@")
                    ? user.email().substring(0, user.email().indexOf('@'))
                    : "User";
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", user.id());
        response.put("displayName", displayName);
        response.put("firstName", user.firstName());
        response.put("lastName", user.lastName());
        response.put("email", user.email());
        response.put("roles", user.roles().stream().map(Enum::name).toList());
        return ResponseEntity.ok(response);
    }
}
