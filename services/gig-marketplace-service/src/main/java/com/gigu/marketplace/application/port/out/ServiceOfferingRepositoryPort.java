package com.gigu.marketplace.application.port.out;
import com.gigu.marketplace.application.dto.SearchQuery;
import com.gigu.marketplace.domain.model.*;
import java.util.*;
public interface ServiceOfferingRepositoryPort {
    ServiceOffering save(ServiceOffering offering);
    Optional<ServiceOffering> findById(UUID id);
    MarketplaceSearchResult searchPublished(SearchQuery query);
    List<ServiceOffering> findByFreelancerId(UUID freelancerId);
    ServiceOffering update(ServiceOffering offering);
    record MarketplaceSearchResult(List<ServiceOffering> items, long total) {}
}
