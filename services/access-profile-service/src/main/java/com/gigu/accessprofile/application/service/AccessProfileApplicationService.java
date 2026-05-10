package com.gigu.accessprofile.application.service;

import com.gigu.accessprofile.application.dto.*;
import com.gigu.accessprofile.application.exception.DuplicatedResourceException;
import com.gigu.accessprofile.application.port.out.*;
import com.gigu.accessprofile.domain.model.FreelancerProfile;
import com.gigu.accessprofile.domain.model.PortfolioItem;
import com.gigu.accessprofile.domain.model.User;
import com.gigu.accessprofile.domain.valueobject.RoleName;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AccessProfileApplicationService {
    public record LoginResult(String token, User user) {}
    private final UserRepositoryPort userRepository;
    private final ProfileRepositoryPort profileRepository;
    private final PasswordHasherPort passwordHasher;
    private final TokenProviderPort tokenProvider;
    private final StoragePort storagePort;

    public AccessProfileApplicationService(UserRepositoryPort userRepository, ProfileRepositoryPort profileRepository, PasswordHasherPort passwordHasher, TokenProviderPort tokenProvider, StoragePort storagePort) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
        this.storagePort = storagePort;
    }

    public User signUp(SignUpCommand command) {
        if (userRepository.existsByEmail(command.email())) throw new DuplicatedResourceException("email already exists");
        if (command.role() == RoleName.ADMIN) throw new IllegalArgumentException("invalid role");
        User user = new User(UUID.randomUUID(), command.firstName(), command.lastName(), command.email().toLowerCase(), passwordHasher.hash(command.password()), Set.of(command.role()), Instant.now());
        User saved = userRepository.save(user);
        if (command.role() == RoleName.FREELANCER) profileRepository.createEmpty(saved.id(), saved.firstName() + " " + saved.lastName());
        return saved;
    }

    public LoginResult login(LoginCommand command) {
        User user = userRepository.findByEmail(command.email().toLowerCase()).orElseThrow(() -> new IllegalArgumentException("invalid credentials"));
        if (!passwordHasher.matches(command.password(), user.passwordHash())) throw new IllegalArgumentException("invalid credentials");
        return new LoginResult(tokenProvider.generate(user), user);
    }

    @Transactional(readOnly = true)
    public User me(UUID userId) { return userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("user not found")); }

    @Transactional(readOnly = true)
    public FreelancerProfile publicFreelancerProfile(UUID userId) { return profileRepository.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("profile not found")); }

    public FreelancerProfile updateMyFreelancerProfile(UUID userId, Set<RoleName> roles, UpdateProfileCommand command) {
        if (!roles.contains(RoleName.FREELANCER)) throw new SecurityException("forbidden");
        return profileRepository.update(userId, command.bio(), command.skills());
    }

    public PortfolioItem addMyPortfolioItem(UUID userId, Set<RoleName> roles, UploadPortfolioCommand command) {
        if (!roles.contains(RoleName.FREELANCER)) throw new SecurityException("forbidden");
        StoragePort.StoredFile file = storagePort.store(userId.toString(), command.contentType(), command.originalFileName(), command.bytes());
        return profileRepository.addPortfolioItem(userId, command.title(), command.description(), file.bucket(), file.path(), file.publicUrl(), file.contentType(), file.sizeBytes());
    }

    public void deleteMyPortfolioItem(UUID userId, Set<RoleName> roles, UUID itemId) {
        if (!roles.contains(RoleName.FREELANCER)) throw new SecurityException("forbidden");
        profileRepository.deletePortfolioItem(userId, itemId);
    }
}
