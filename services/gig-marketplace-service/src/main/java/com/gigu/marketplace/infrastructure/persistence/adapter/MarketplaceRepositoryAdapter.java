package com.gigu.marketplace.infrastructure.persistence.adapter;

import com.gigu.marketplace.application.dto.SearchQuery;
import com.gigu.marketplace.application.port.out.*;
import com.gigu.marketplace.domain.model.*;
import com.gigu.marketplace.domain.valueobject.*;
import com.gigu.marketplace.infrastructure.persistence.entity.*;
import com.gigu.marketplace.infrastructure.persistence.repository.*;
import com.gigu.marketplace.infrastructure.persistence.specification.ServiceOfferingSpecifications;
import java.util.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class MarketplaceRepositoryAdapter implements ServiceOfferingRepositoryPort, CategoryRepositoryPort, MediaRepositoryPort {
    private final ServiceOfferingJpaRepository serviceRepo; private final ServiceCategoryJpaRepository categoryRepo; private final ServiceMediaJpaRepository mediaRepo;
    public MarketplaceRepositoryAdapter(ServiceOfferingJpaRepository serviceRepo, ServiceCategoryJpaRepository categoryRepo, ServiceMediaJpaRepository mediaRepo){this.serviceRepo=serviceRepo;this.categoryRepo=categoryRepo;this.mediaRepo=mediaRepo;}

    public ServiceOffering save(ServiceOffering s){ return toDomain(serviceRepo.save(toEntity(s))); }
    public Optional<ServiceOffering> findById(UUID id){ return serviceRepo.findById(id).map(this::toDomain); }
    public MarketplaceSearchResult searchPublished(SearchQuery query){
        var pageable= PageRequest.of(Math.max(0,query.page()-1), query.pageSize());
        var page=serviceRepo.findAll(ServiceOfferingSpecifications.build(query), pageable);
        return new MarketplaceSearchResult(page.getContent().stream().map(this::toDomain).toList(), page.getTotalElements());
    }
    public List<ServiceOffering> findByFreelancerId(UUID freelancerId){ return serviceRepo.findByFreelancerId(freelancerId).stream().map(this::toDomain).toList(); }
    public ServiceOffering update(ServiceOffering s){ return toDomain(serviceRepo.save(toEntity(s))); }

    public Optional<ServiceCategory> findCategoryById(UUID id){ return categoryRepo.findById(id).map(c->new ServiceCategory(c.id,c.name)); }
    public List<ServiceCategory> findAll(){ return categoryRepo.findAll().stream().map(c->new ServiceCategory(c.id,c.name)).toList(); }

    public ServiceMedia save(ServiceMedia m){ var e=new ServiceMediaEntity(); e.id=m.id();e.serviceId=m.serviceId();e.publicUrl=m.url();e.mediaType=m.type();e.isPrimary=m.primary();e.bucket=m.bucket();e.objectPath=m.objectPath();e.contentType=m.contentType();e.sizeBytes=m.sizeBytes();e.sortOrder=m.sortOrder();e.createdAt=m.createdAt(); var s=mediaRepo.save(e); return new ServiceMedia(s.id,s.serviceId,s.publicUrl,s.mediaType,s.isPrimary,s.bucket,s.objectPath,s.contentType,s.sizeBytes,s.sortOrder,s.createdAt); }
    public List<ServiceMedia> findByServiceId(UUID sid){ return mediaRepo.findByServiceId(sid).stream().sorted(Comparator.comparingInt((ServiceMediaEntity x) -> x.sortOrder).thenComparing(x -> x.createdAt)).map(x->new ServiceMedia(x.id,x.serviceId,x.publicUrl,x.mediaType,x.isPrimary,x.bucket,x.objectPath,x.contentType,x.sizeBytes,x.sortOrder,x.createdAt)).toList(); }
    public Optional<ServiceMedia> findMediaById(UUID id){ return mediaRepo.findById(id).map(x->new ServiceMedia(x.id,x.serviceId,x.publicUrl,x.mediaType,x.isPrimary,x.bucket,x.objectPath,x.contentType,x.sizeBytes,x.sortOrder,x.createdAt)); }
    public void delete(UUID id){ mediaRepo.deleteById(id); }
    public void clearPrimary(UUID serviceId){ mediaRepo.findByServiceId(serviceId).forEach(m->{m.isPrimary=false; mediaRepo.save(m);}); }

    private ServiceOfferingEntity toEntity(ServiceOffering s){
        var e=new ServiceOfferingEntity(); e.id=s.id();e.freelancerId=s.freelancerId();e.freelancerDisplayName=s.freelancerDisplayName();e.title=s.title();e.description=s.description();e.basePrice=s.basePrice();e.currency=s.currency().name();e.deliveryDays=s.deliveryDays();e.status=s.status().name();e.tags=String.join(",", s.tags()==null?List.of():s.tags());e.createdAt=s.createdAt();e.updatedAt=s.updatedAt();
        ServiceCategoryEntity c=categoryRepo.findById(s.categoryId()).orElseThrow(); e.category=c; return e;
    }
    private ServiceOffering toDomain(ServiceOfferingEntity e){ return new ServiceOffering(e.id,e.freelancerId,e.freelancerDisplayName,e.title,e.description,e.basePrice,CurrencyCode.valueOf(e.currency),e.deliveryDays,ServiceStatus.valueOf(e.status),e.category.id,e.category.name, e.tags==null||e.tags.isBlank()?List.of():Arrays.stream(e.tags.split(",")).map(String::trim).toList(),e.createdAt,e.updatedAt); }
}
