package com.gigu.marketplace.application.service;
import com.gigu.marketplace.application.dto.*;
import com.gigu.marketplace.application.port.in.command.MarketplaceCommandUseCase;
import com.gigu.marketplace.application.port.in.query.MarketplaceQueryUseCase;
import com.gigu.marketplace.application.port.out.*;
import com.gigu.marketplace.domain.model.*;
import com.gigu.marketplace.domain.valueobject.ServiceStatus;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MarketplaceApplicationService implements MarketplaceCommandUseCase, MarketplaceQueryUseCase {
    private static final long MAX_MEDIA_SIZE_BYTES = 5L * 1024 * 1024;
    private static final int MAX_MEDIA_PER_SERVICE = 5;
    private static final Set<String> ALLOWED_MEDIA_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private final ServiceOfferingRepositoryPort serviceRepo; private final CategoryRepositoryPort categoryRepo; private final MediaRepositoryPort mediaRepo; private final StoragePort storage;
    public MarketplaceApplicationService(ServiceOfferingRepositoryPort serviceRepo, CategoryRepositoryPort categoryRepo, MediaRepositoryPort mediaRepo, StoragePort storage) { this.serviceRepo = serviceRepo; this.categoryRepo = categoryRepo; this.mediaRepo = mediaRepo; this.storage = storage; }

    public ServiceOffering createService(CreateServiceCommand c) {
        if (!c.freelancerRole()) throw new SecurityException("forbidden");
        if (c.basePrice() == null || c.basePrice().doubleValue() <= 0) throw new IllegalArgumentException("invalid price");
        ServiceCategory cat = categoryRepo.findCategoryById(c.categoryId()).orElseThrow(() -> new IllegalArgumentException("category not found"));
        return serviceRepo.save(new ServiceOffering(UUID.randomUUID(), c.freelancerId(), c.freelancerDisplayName(), c.title(), c.description(), c.basePrice(), c.currency(), c.deliveryDays(), ServiceStatus.PUBLISHED, cat.id(), cat.name(), c.tags(), Instant.now(), Instant.now()));
    }
    public ServiceOffering updateService(UUID id, UpdateServiceCommand c) {
        ServiceOffering s = serviceRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("service not found"));
        if (!"FREELANCER".equals(c.actorRole()) || !s.freelancerId().toString().equals(c.actorId())) throw new SecurityException("forbidden");
        return serviceRepo.update(new ServiceOffering(s.id(), s.freelancerId(), s.freelancerDisplayName(), s.title(), c.description() == null ? s.description() : c.description(), c.basePrice() == null ? s.basePrice() : c.basePrice(), s.currency(), c.deliveryDays() == null ? s.deliveryDays() : c.deliveryDays(), s.status(), s.categoryId(), s.categoryName(), s.tags(), s.createdAt(), Instant.now()));
    }
    public void softDeleteService(UUID id, String actorId, String actorRole) {
        ServiceOffering s = serviceRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("service not found"));
        if (!"FREELANCER".equals(actorRole) || !s.freelancerId().toString().equals(actorId)) throw new SecurityException("forbidden");
        serviceRepo.update(new ServiceOffering(s.id(), s.freelancerId(), s.freelancerDisplayName(), s.title(), s.description(), s.basePrice(), s.currency(), s.deliveryDays(), ServiceStatus.UNPUBLISHED, s.categoryId(), s.categoryName(), s.tags(), s.createdAt(), Instant.now()));
    }
    public ServiceMedia uploadMedia(UUID serviceId, String actorId, String actorRole, String contentType, String originalFileName, byte[] bytes, boolean primary) {
        ServiceOffering s = serviceRepo.findById(serviceId).orElseThrow(() -> new IllegalArgumentException("service not found"));
        if (!"FREELANCER".equals(actorRole) || !s.freelancerId().toString().equals(actorId)) throw new SecurityException("forbidden");
        validateMedia(contentType, bytes);
        var currentMedia = mediaRepo.findByServiceId(serviceId);
        if (currentMedia.size() >= MAX_MEDIA_PER_SERVICE) throw new IllegalArgumentException("maximum number of media files reached");
        boolean shouldBePrimary = primary || currentMedia.isEmpty();
        if (shouldBePrimary) mediaRepo.clearPrimary(serviceId);
        StoragePort.Stored stored = storage.store(serviceId.toString(), contentType, originalFileName, bytes);
        int sortOrder = currentMedia.size();
        return mediaRepo.save(new ServiceMedia(UUID.randomUUID(), serviceId, stored.publicUrl(), "IMAGE", shouldBePrimary, stored.bucket(), stored.objectPath(), stored.contentType(), stored.sizeBytes(), sortOrder, Instant.now()));
    }
    public void deleteMedia(UUID serviceId, UUID mediaId, String actorId, String actorRole) {
        ServiceOffering s = serviceRepo.findById(serviceId).orElseThrow(() -> new IllegalArgumentException("service not found"));
        if (!"FREELANCER".equals(actorRole) || !s.freelancerId().toString().equals(actorId)) throw new SecurityException("forbidden");
        ServiceMedia media = mediaRepo.findMediaById(mediaId).orElseThrow(() -> new IllegalArgumentException("media not found"));
        if (!media.serviceId().equals(serviceId)) throw new IllegalArgumentException("media not found");
        boolean deletedWasPrimary = media.primary();
        storage.delete(media.bucket(), media.objectPath());
        mediaRepo.delete(mediaId);
        if (deletedWasPrimary) {
            var remaining = mediaRepo.findByServiceId(serviceId);
            if (!remaining.isEmpty()) {
                var nextPrimary = remaining.stream().sorted(Comparator.comparingInt(ServiceMedia::sortOrder).thenComparing(ServiceMedia::createdAt)).findFirst().orElseThrow();
                mediaRepo.save(new ServiceMedia(nextPrimary.id(), nextPrimary.serviceId(), nextPrimary.url(), nextPrimary.type(), true, nextPrimary.bucket(), nextPrimary.objectPath(), nextPrimary.contentType(), nextPrimary.sizeBytes(), nextPrimary.sortOrder(), nextPrimary.createdAt()));
            }
        }
    }
    @Transactional(readOnly = true) public PagedResult search(SearchQuery q){ var r=serviceRepo.searchPublished(q); return new PagedResult(r.items(), r.total(), q.page(), q.pageSize());}
    @Transactional(readOnly = true) public ServiceOffering detail(UUID serviceId){ return serviceRepo.findById(serviceId).orElseThrow(() -> new IllegalArgumentException("service not found")); }
    @Transactional(readOnly = true) public List<ServiceOffering> mine(UUID freelancerId){ return serviceRepo.findByFreelancerId(freelancerId); }
    @Transactional(readOnly = true) public List<ServiceMedia> media(UUID serviceId){ return mediaRepo.findByServiceId(serviceId); }
    @Transactional(readOnly = true) public List<ServiceCategory> categories(){ return categoryRepo.findAll(); }

    private void validateMedia(String contentType, byte[] bytes) {
        if (contentType == null || !ALLOWED_MEDIA_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) throw new IllegalArgumentException("unsupported content type");
        if (bytes == null || bytes.length == 0) throw new IllegalArgumentException("file is empty");
        if (bytes.length > MAX_MEDIA_SIZE_BYTES) throw new IllegalArgumentException("file exceeds 5MB");
    }
}
