package xyz.anomatver.blps.review.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.anomatver.blps.exception.NotFoundException;
import xyz.anomatver.blps.review.model.Review;
import xyz.anomatver.blps.review.model.ReviewStatus;
import xyz.anomatver.blps.review.repository.ReviewRepository;
import xyz.anomatver.blps.vote.service.SpamDetectionService;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private final SpamDetectionService spamDetectionService;
    @Autowired
    private ReviewRepository reviewRepository;

    public ReviewService(SpamDetectionService spamDetectionService) {
        this.spamDetectionService = spamDetectionService;
    }


    @Transactional
    public Review submitReview(Review review) {
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
