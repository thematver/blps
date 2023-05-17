package xyz.anomatver.blps.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.anomatver.blps.domain.Review;
import xyz.anomatver.blps.domain.ReviewStatus;
import xyz.anomatver.blps.domain.User;
import xyz.anomatver.blps.exception.NotFoundException;
import xyz.anomatver.blps.repository.ReviewRepository;

import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private final SpamDetectionService spamDetectionService;

    public ReviewServiceImpl(SpamDetectionService spamDetectionService) {
        this.spamDetectionService = spamDetectionService;
    }

    @Override
    public Review submitReview(User user, Review review) {
        // Проверка валидности рецензии
        if (review.getContent() == null || review.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Контент рецензии не может быть пустым");
        }

        if (spamDetectionService.isSpam(review, "test", "test")) {
            review.setStatus(ReviewStatus.PENDING); // Устанавливаем статус "Ожидает проверки"
        } else {
            review.setStatus(ReviewStatus.APPROVED); // Устанавливаем статус "Опубликовано"
        }

        return reviewRepository.save(review);
    }

    @Override
    public List<Review> getReviewsByStatus(ReviewStatus status) {
        return reviewRepository.findAllByStatus(status);
    }

    @Override
    public Review updateReviewStatus(Review review, ReviewStatus status) {
        review.setStatus(status);
        return reviewRepository.save(review);
    }

    public Review findById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Review not found with id: " + id));
    }

    public List<Review> findAll() {
        return reviewRepository.findAll();
    }

    public void deleteReview(Long id) {
        Review review = findById(id);
        reviewRepository.delete(review);
    }

    public Review updateReview(Long id, Review updatedReview) {
        Review existingReview = findById(id);
        existingReview.setContent(updatedReview.getContent());
        existingReview.setStatus(updatedReview.getStatus());
        return reviewRepository.save(existingReview);
    }


}
