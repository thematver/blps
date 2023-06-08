package xyz.anomatver.blps.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.anomatver.blps.auth.model.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}