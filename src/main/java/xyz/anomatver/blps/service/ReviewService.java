package xyz.anomatver.blps.service;

import xyz.anomatver.blps.domain.Review;
import xyz.anomatver.blps.domain.ReviewStatus;
import xyz.anomatver.blps.domain.User;

import java.util.List;

public interface ReviewService {
    Review submitReview(User user, Review review);
    List<Review> getReviewsByStatus(ReviewStatus status);
    Review updateReviewStatus(Review review, ReviewStatus status);

    public Review findById(Long id);
    public List<Review> findAll();

    public void deleteReview(Long id);

    public Review updateReview(Long id, Review updatedReview);


}