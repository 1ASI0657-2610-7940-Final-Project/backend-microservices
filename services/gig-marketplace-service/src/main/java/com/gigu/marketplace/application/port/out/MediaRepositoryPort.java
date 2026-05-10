package com.gigu.marketplace.application.port.out;
import com.gigu.marketplace.domain.model.ServiceMedia;
import java.util.*;
public interface MediaRepositoryPort {
    ServiceMedia save(ServiceMedia media);
    List<ServiceMedia> findByServiceId(UUID serviceId);
    Optional<ServiceMedia> findMediaById(UUID id);
    void delete(UUID id);
    void clearPrimary(UUID serviceId);
}
