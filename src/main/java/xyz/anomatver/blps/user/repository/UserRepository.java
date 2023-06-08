package xyz.anomatver.blps.user.repository;

import xyz.anomatver.blps.auth.model.ERole;
import xyz.anomatver.blps.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);

    int countUsersByRolesContains(ERole role);
}