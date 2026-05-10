package com.gigu.marketplace.infrastructure.persistence.specification;

import com.gigu.marketplace.application.dto.SearchQuery;
import com.gigu.marketplace.infrastructure.persistence.entity.ServiceOfferingEntity;
import org.springframework.data.jpa.domain.Specification;

public final class ServiceOfferingSpecifications {
    private ServiceOfferingSpecifications() {}
    public static Specification<ServiceOfferingEntity> build(SearchQuery q){
        return (root, query, cb) -> {
            var p = cb.conjunction();
            p = cb.and(p, cb.equal(root.get("status"), "PUBLISHED"));
            if (q.category()!=null && !q.category().isBlank()) p = cb.and(p, cb.like(cb.lower(root.get("category").get("name")), "%"+q.category().toLowerCase()+"%"));
            if (q.priceMin()!=null) p = cb.and(p, cb.greaterThanOrEqualTo(root.get("basePrice"), java.math.BigDecimal.valueOf(q.priceMin())));
            if (q.priceMax()!=null) p = cb.and(p, cb.lessThanOrEqualTo(root.get("basePrice"), java.math.BigDecimal.valueOf(q.priceMax())));
            if (q.q()!=null && !q.q().isBlank()) p = cb.and(p, cb.or(cb.like(cb.lower(root.get("title")), "%"+q.q().toLowerCase()+"%"), cb.like(cb.lower(root.get("description")), "%"+q.q().toLowerCase()+"%")));
            return p;
        };
    }
}
