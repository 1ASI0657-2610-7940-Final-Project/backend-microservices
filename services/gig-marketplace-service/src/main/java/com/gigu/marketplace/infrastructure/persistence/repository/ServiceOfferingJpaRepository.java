package com.gigu.marketplace.infrastructure.persistence.repository;
import com.gigu.marketplace.infrastructure.persistence.entity.ServiceOfferingEntity;
import java.util.*;
import org.springframework.data.jpa.repository.*;
public interface ServiceOfferingJpaRepository extends JpaRepository<ServiceOfferingEntity, UUID>, JpaSpecificationExecutor<ServiceOfferingEntity> { List<ServiceOfferingEntity> findByFreelancerId(UUID freelancerId); }
