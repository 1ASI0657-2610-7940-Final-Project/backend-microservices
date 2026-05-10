package com.gigu.marketplace.application.port.in.query;
import com.gigu.marketplace.application.dto.SearchQuery;
import com.gigu.marketplace.domain.model.ServiceCategory;
import com.gigu.marketplace.domain.model.ServiceOffering;
import java.util.List;
import java.util.UUID;
public interface MarketplaceQueryUseCase {
    record PagedResult(List<ServiceOffering> data, long total, int page, int pageSize) {}
    PagedResult search(SearchQuery query);
    ServiceOffering detail(UUID serviceId);
    List<ServiceOffering> mine(UUID freelancerId);
    List<ServiceCategory> categories();
}
