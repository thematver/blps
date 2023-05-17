package xyz.anomatver.blps.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xyz.anomatver.blps.domain.User;

import java.util.List;


@Repository
public interface ModeratorRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

//    List<User> findAllBy(String role);
}