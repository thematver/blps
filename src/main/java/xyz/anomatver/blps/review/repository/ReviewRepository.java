package xyz.anomatver.blps.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import xyz.anomatver.blps.review.model.Review;
import xyz.anomatver.blps.review.model.ReviewStatus;
import xyz.anomatver.blps.user.model.User;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findAllByStatus(ReviewStatus status);

    List<Review> getReviewsByAuthor(User user);

    @Query("SELECT r FROM Review r WHERE r.created >= :yesterday AND r.created < :today")
    List<Review> findAllReviewsFromYesterday(Date today, Date yesterday);
}
