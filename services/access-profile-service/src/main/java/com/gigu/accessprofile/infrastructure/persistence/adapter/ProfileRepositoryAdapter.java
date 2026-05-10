package com.gigu.accessprofile.infrastructure.persistence.adapter;

import com.gigu.accessprofile.application.port.out.ProfileRepositoryPort;
import com.gigu.accessprofile.domain.model.FreelancerProfile;
import com.gigu.accessprofile.domain.model.PortfolioItem;
import com.gigu.accessprofile.infrastructure.persistence.entity.*;
import com.gigu.accessprofile.infrastructure.persistence.repository.*;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Component;

@Component
public class ProfileRepositoryAdapter implements ProfileRepositoryPort {
    private final FreelancerProfileJpaRepository profileRepo;
    private final UserJpaRepository userRepo;
    private final SkillJpaRepository skillRepo;
    private final PortfolioItemJpaRepository itemRepo;

    public ProfileRepositoryAdapter(FreelancerProfileJpaRepository profileRepo, UserJpaRepository userRepo, SkillJpaRepository skillRepo, PortfolioItemJpaRepository itemRepo) {
        this.profileRepo = profileRepo; this.userRepo = userRepo; this.skillRepo = skillRepo; this.itemRepo = itemRepo;
    }

    public FreelancerProfile createEmpty(UUID userId, String displayName) {
        UserEntity user = userRepo.findById(userId).orElseThrow();
        FreelancerProfileEntity p = new FreelancerProfileEntity();
        p.id = UUID.randomUUID(); p.user = user; p.displayName = displayName; p.bio = ""; p.academicVerificationStatus = "PENDING"; p.updatedAt = Instant.now();
        return toDomain(profileRepo.save(p));
    }

    public Optional<FreelancerProfile> findByUserId(UUID userId) { return profileRepo.findByUser_Id(userId).map(this::toDomain); }

    public FreelancerProfile update(UUID userId, String bio, List<String> skills) {
        FreelancerProfileEntity p = profileRepo.findByUser_Id(userId).orElseThrow(() -> new IllegalArgumentException("profile not found"));
        p.bio = bio == null ? "" : bio;
        p.skills.clear();
        if (skills != null) {
            for (String name : skills) {
                SkillEntity s = skillRepo.findByNameIgnoreCase(name).orElseGet(() -> { SkillEntity n = new SkillEntity(); n.id = UUID.randomUUID(); n.name = name; return skillRepo.save(n); });
                p.skills.add(s);
            }
        }
        p.updatedAt = Instant.now();
        return toDomain(profileRepo.save(p));
    }

    public PortfolioItem addPortfolioItem(UUID userId, String title, String description, String bucket, String path, String publicUrl, String contentType, long sizeBytes) {
        FreelancerProfileEntity p = profileRepo.findByUser_Id(userId).orElseThrow(() -> new IllegalArgumentException("profile not found"));
        PortfolioItemEntity i = new PortfolioItemEntity();
        i.id = UUID.randomUUID(); i.profile = p; i.title = title; i.description = description; i.bucket = bucket; i.path = path; i.publicUrl = publicUrl; i.contentType = contentType; i.sizeBytes = sizeBytes; i.createdAt = Instant.now();
        PortfolioItemEntity saved = itemRepo.save(i);
        return new PortfolioItem(saved.id, p.id, saved.title, saved.description, saved.bucket, saved.path, saved.publicUrl, saved.contentType, saved.sizeBytes, saved.createdAt);
    }

    public void deletePortfolioItem(UUID userId, UUID itemId) {
        PortfolioItemEntity item = itemRepo.findByIdAndProfile_User_Id(itemId, userId).orElseThrow(() -> new SecurityException("forbidden"));
        itemRepo.delete(item);
    }

    private FreelancerProfile toDomain(FreelancerProfileEntity p) {
        List<String> skills = p.skills.stream().map(s -> s.name).sorted().toList();
        List<PortfolioItem> items = itemRepo.findByProfileId(p.id).stream().map(i -> new PortfolioItem(i.id, p.id, i.title, i.description, i.bucket, i.path, i.publicUrl, i.contentType, i.sizeBytes, i.createdAt)).toList();
        return new FreelancerProfile(p.id, p.user.id, p.displayName, p.bio, p.academicVerificationStatus, skills, items, p.updatedAt);
    }
}
