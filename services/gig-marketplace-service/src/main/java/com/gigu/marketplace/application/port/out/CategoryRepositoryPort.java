package com.gigu.marketplace.application.port.out;
import com.gigu.marketplace.domain.model.ServiceCategory;
import java.util.*;
public interface CategoryRepositoryPort { Optional<ServiceCategory> findCategoryById(UUID id); List<ServiceCategory> findAll(); }
