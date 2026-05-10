package com.gigu.accessprofile.infrastructure.persistence.adapter;

import com.gigu.accessprofile.application.port.out.UserRepositoryPort;
import com.gigu.accessprofile.domain.model.User;
import com.gigu.accessprofile.domain.valueobject.RoleName;
import com.gigu.accessprofile.infrastructure.persistence.entity.RoleEntity;
import com.gigu.accessprofile.infrastructure.persistence.entity.UserEntity;
import com.gigu.accessprofile.infrastructure.persistence.repository.RoleJpaRepository;
import com.gigu.accessprofile.infrastructure.persistence.repository.UserJpaRepository;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class UserRepositoryAdapter implements UserRepositoryPort {
    private final UserJpaRepository userRepository;
    private final RoleJpaRepository roleRepository;

    public UserRepositoryAdapter(UserJpaRepository userRepository, RoleJpaRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public boolean existsByEmail(String email) { return userRepository.existsByEmail(email); }

    public User save(User user) {
        UserEntity e = new UserEntity();
        e.id = user.id(); e.firstName = user.firstName(); e.lastName = user.lastName(); e.email = user.email(); e.passwordHash = user.passwordHash(); e.createdAt = user.createdAt();
        e.roles = user.roles().stream().map(role -> roleRepository.findByName(role.name()).orElseGet(() -> {
            RoleEntity r = new RoleEntity(); r.id = UUID.randomUUID(); r.name = role.name(); return roleRepository.save(r);
        })).collect(Collectors.toSet());
        return toDomain(userRepository.save(e));
    }

    public Optional<User> findByEmail(String email) { return userRepository.findByEmail(email).map(this::toDomain); }
    public Optional<User> findById(UUID id) { return userRepository.findById(id).map(this::toDomain); }

    private User toDomain(UserEntity e) {
        Set<RoleName> roles = e.roles.stream().map(r -> RoleName.valueOf(r.name)).collect(Collectors.toSet());
        return new User(e.id, e.firstName, e.lastName, e.email, e.passwordHash, roles, e.createdAt);
    }
}
