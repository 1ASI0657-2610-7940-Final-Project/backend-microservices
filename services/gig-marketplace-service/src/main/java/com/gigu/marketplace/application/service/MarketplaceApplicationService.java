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
    public ServiceMedia uploadMedia(UUID serviceId, String actorId, String actorRole, String contentType, byte[] bytes, boolean primary) {
        ServiceOffering s = serviceRepo.findById(serviceId).orElseThrow(() -> new IllegalArgumentException("service not found"));
        if (!"FREELANCER".equals(actorRole) || !s.freelancerId().toString().equals(actorId)) throw new SecurityException("forbidden");
        if (primary) mediaRepo.clearPrimary(serviceId);
        StoragePort.Stored stored = storage.store(serviceId.toString(), contentType, bytes);
        return mediaRepo.save(new ServiceMedia(UUID.randomUUID(), serviceId, stored.publicUrl(), "IMAGE", primary, stored.bucket(), stored.path(), stored.contentType(), stored.sizeBytes(), Instant.now()));
    }
    public void deleteMedia(UUID serviceId, UUID mediaId, String actorId, String actorRole) {
        ServiceOffering s = serviceRepo.findById(serviceId).orElseThrow(() -> new IllegalArgumentException("service not found"));
        if (!"FREELANCER".equals(actorRole) || !s.freelancerId().toString().equals(actorId)) throw new SecurityException("forbidden");
        mediaRepo.findMediaById(mediaId).orElseThrow(() -> new IllegalArgumentException("media not found"));
        mediaRepo.delete(mediaId);
    }
    @Transactional(readOnly = true) public PagedResult search(SearchQuery q){ var r=serviceRepo.searchPublished(q); return new PagedResult(r.items(), r.total(), q.page(), q.pageSize());}
    @Transactional(readOnly = true) public ServiceOffering detail(UUID serviceId){ return serviceRepo.findById(serviceId).orElseThrow(() -> new IllegalArgumentException("service not found")); }
    @Transactional(readOnly = true) public List<ServiceOffering> mine(UUID freelancerId){ return serviceRepo.findByFreelancerId(freelancerId); }
    @Transactional(readOnly = true) public List<ServiceCategory> categories(){ return categoryRepo.findAll(); }
}
