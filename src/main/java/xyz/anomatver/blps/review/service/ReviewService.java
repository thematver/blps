package xyz.anomatver.blps.review.service;

import org.springframework.stereotype.Service;
import xyz.anomatver.blps.exception.NotFoundException;
import xyz.anomatver.blps.mqtt.MessageSenderService;
import xyz.anomatver.blps.review.model.Review;
import xyz.anomatver.blps.review.model.ReviewStatus;
import xyz.anomatver.blps.review.repository.ReviewRepository;
import xyz.anomatver.blps.vote.service.SpamDetectionService;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class ReviewService {

    private final SpamDetectionService spamDetectionService;
    private final ReviewRepository reviewRepository;

    private final MessageSenderService messageSenderService;

    public ReviewService(SpamDetectionService spamDetectionService, ReviewRepository reviewRepository, MessageSenderService messageSenderService) {
        this.spamDetectionService = spamDetectionService;
        this.reviewRepository = reviewRepository;
        this.messageSenderService = messageSenderService;
    }

    @Transactional
    public Review submitReview(Review review) {
        validateReviewContent(review);
        setReviewStatusBasedOnSpamDetection(review);
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
        updateReviewContentAndStatus(existingReview, updatedReview);
        return reviewRepository.save(existingReview);
    }

    public List<Review> getYesterdayReviews() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        Date todayDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date yesterdayDate = Date.from(yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant());

        return reviewRepository.findAllReviewsFromYesterday(todayDate, yesterdayDate);
    }

    private void validateReviewContent(Review review) {
        if (review.getContent() == null || review.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Review content cannot be empty");
        }
    }

    private void setReviewStatusBasedOnSpamDetection(Review review) {
        if (spamDetectionService.isSpam(review, "test", "test")) {
            review.setStatus(ReviewStatus.PENDING);
        } else {
            review.setStatus(ReviewStatus.APPROVED);
        }
    }

    private void updateReviewContentAndStatus(Review existingReview, Review updatedReview) {
        existingReview.setContent(updatedReview.getContent());
        existingReview.setStatus(updatedReview.getStatus());
    }

}
