package com.gigu.accessprofile.infrastructure.bootstrap;

import com.gigu.accessprofile.infrastructure.persistence.entity.RoleEntity;
import com.gigu.accessprofile.infrastructure.persistence.repository.RoleJpaRepository;
import java.util.UUID;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AccessProfileDataInitializer implements ApplicationRunner {
    private final RoleJpaRepository roleRepository;

    public AccessProfileDataInitializer(RoleJpaRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        ensureRole("CLIENT");
        ensureRole("FREELANCER");
        ensureRole("ADMIN");
    }

    private void ensureRole(String roleName) {
        if (roleRepository.findByName(roleName).isPresent()) {
            return;
        }
        RoleEntity role = new RoleEntity();
        role.id = UUID.randomUUID();
        role.name = roleName;
        roleRepository.save(role);
    }
}
