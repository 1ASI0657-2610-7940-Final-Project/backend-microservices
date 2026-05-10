package com.gigu.accessprofile.application.service;

import com.gigu.accessprofile.application.dto.*;
import com.gigu.accessprofile.application.port.out.*;
import com.gigu.accessprofile.domain.model.User;
import com.gigu.accessprofile.domain.model.FreelancerProfile;
import com.gigu.accessprofile.domain.model.PortfolioItem;
import com.gigu.accessprofile.domain.valueobject.RoleName;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccessProfileApplicationServiceTest {
    @Mock UserRepositoryPort userRepository;
    @Mock ProfileRepositoryPort profileRepository;
    @Mock PasswordHasherPort passwordHasher;
    @Mock TokenProviderPort tokenProvider;
    @Mock StoragePort storagePort;
    @InjectMocks AccessProfileApplicationService service;

    @Test
    void signUpCreatesUser() {
        when(userRepository.existsByEmail("ana@test.com")).thenReturn(false);
        when(passwordHasher.hash("Password123!")).thenReturn("HASH");
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        var user = service.signUp(new SignUpCommand("Ana", "Rojas", "ana@test.com", "Password123!", RoleName.FREELANCER));
        assertEquals("ana@test.com", user.email());
        verify(profileRepository).createEmpty(any(), eq("Ana Rojas"));
    }

    @Test
    void signUpRejectsDuplicateEmail() {
        when(userRepository.existsByEmail("ana@test.com")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.signUp(new SignUpCommand("Ana", "Rojas", "ana@test.com", "x", RoleName.CLIENT)));
    }

    @Test
    void loginReturnsJwt() {
        User u = new User(UUID.randomUUID(), "Ana", "R", "ana@test.com", "HASH", Set.of(RoleName.CLIENT), Instant.now());
        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(u));
        when(passwordHasher.matches("Password123!", "HASH")).thenReturn(true);
        when(tokenProvider.generate(u)).thenReturn("jwt");
        assertEquals("jwt", service.login(new LoginCommand("ana@test.com", "Password123!")).token());
    }

    @Test
    void loginRejectsInvalidCredentials() {
        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.login(new LoginCommand("ana@test.com", "bad")));
    }

    @Test
    void portfolioUploadValidatesFreelancerRole() {
        assertThrows(SecurityException.class, () -> service.addMyPortfolioItem(UUID.randomUUID(), Set.of(RoleName.CLIENT), new UploadPortfolioCommand("t", "d", "image/png", 1, new byte[]{1})));
    }

    @Test
    void portfolioDeleteValidatesOwnerRole() {
        assertThrows(SecurityException.class, () -> service.deleteMyPortfolioItem(UUID.randomUUID(), Set.of(RoleName.CLIENT), UUID.randomUUID()));
    }

    @Test
    void updateFreelancerProfileValidatesOwner() {
        assertThrows(SecurityException.class, () -> service.updateMyFreelancerProfile(UUID.randomUUID(), Set.of(RoleName.CLIENT), new UpdateProfileCommand("bio", List.of("Vue"))));
    }

    @Test
    void meReturnsUser() {
        UUID id = UUID.randomUUID();
        User u = new User(id, "Ana", "R", "ana@test.com", "HASH", Set.of(RoleName.CLIENT), Instant.now());
        when(userRepository.findById(id)).thenReturn(Optional.of(u));
        assertEquals(id, service.me(id).id());
    }

    @Test
    void publicProfileReturnsData() {
        UUID userId = UUID.randomUUID();
        FreelancerProfile p = new FreelancerProfile(UUID.randomUUID(), userId, "Ana R", "bio", "PENDING", List.of("Vue"), List.of(), Instant.now());
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(p));
        assertEquals("Ana R", service.publicFreelancerProfile(userId).displayName());
    }

    @Test
    void updateProfileAsFreelancer() {
        UUID userId = UUID.randomUUID();
        FreelancerProfile p = new FreelancerProfile(UUID.randomUUID(), userId, "Ana R", "bio", "PENDING", List.of("Vue"), List.of(), Instant.now());
        when(profileRepository.update(eq(userId), any(), any())).thenReturn(p);
        assertEquals("Ana R", service.updateMyFreelancerProfile(userId, Set.of(RoleName.FREELANCER), new UpdateProfileCommand("bio", List.of("Vue"))).displayName());
    }

    @Test
    void addPortfolioAsFreelancer() {
        UUID userId = UUID.randomUUID();
        when(storagePort.store(eq(userId.toString()), any(), any())).thenReturn(new StoragePort.StoredFile("portfolio", "p", "url", "image/png", 10));
        when(profileRepository.addPortfolioItem(eq(userId), any(), any(), any(), any(), any(), any(), anyLong()))
                .thenReturn(new PortfolioItem(UUID.randomUUID(), UUID.randomUUID(), "t", "d", "portfolio", "p", "url", "image/png", 10, Instant.now()));
        assertEquals("t", service.addMyPortfolioItem(userId, Set.of(RoleName.FREELANCER), new UploadPortfolioCommand("t", "d", "image/png", 10, new byte[]{1})).title());
    }

    @Test
    void deletePortfolioAsFreelancer() {
        UUID userId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        service.deleteMyPortfolioItem(userId, Set.of(RoleName.FREELANCER), itemId);
        verify(profileRepository).deletePortfolioItem(userId, itemId);
    }
}
