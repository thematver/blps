package xyz.anomatver.blps.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xyz.anomatver.blps.domain.Review;
import xyz.anomatver.blps.domain.ReviewStatus;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findAllByStatus(ReviewStatus status);

}