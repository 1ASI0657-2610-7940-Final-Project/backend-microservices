package com.gigu.accessprofile.interfaces.rest;

import com.gigu.accessprofile.application.dto.*;
import com.gigu.accessprofile.application.service.AccessProfileApplicationService;
import com.gigu.accessprofile.domain.model.User;
import com.gigu.accessprofile.interfaces.rest.dto.*;
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
@RequestMapping("/api/v1/access")
public class AccessController {
    private final AccessProfileApplicationService service;

    public AccessController(AccessProfileApplicationService service) { this.service = service; }

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
}
