package xyz.anomatver.blps.review.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);
    private final SpamDetectionService spamDetectionService;
    private final ReviewRepository reviewRepository;


    public ReviewService(SpamDetectionService spamDetectionService, ReviewRepository reviewRepository) {
        this.spamDetectionService = spamDetectionService;
        this.reviewRepository = reviewRepository;
    }

    @Transactional
    public Review submitReview(Review review, String userIp, String userAgent) {
        try {
            validate(review);
            checkForSpam(review, userIp, userAgent);
            return reviewRepository.save(review);
        } catch (Exception ex) {
            logger.error("Failed to submit review: {}", ex.getMessage());
            throw ex;
        }
    }

    public boolean validate(Review review)   {
        try {
            return validateReviewContent(review);
        } catch (Exception ex) {
            logger.error("Error while validating review: {}", ex.getMessage());
            return false;
        }
    }


    public long save(Review review) {
        try {
            return reviewRepository.save(review).getId();
        } catch (Exception ex) {
            logger.error("Error while saving review: {}", ex.getMessage());
            throw ex;
        }
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

    private boolean validateReviewContent(Review review) {
        return review.getContent() != null && !review.getContent().trim().isEmpty();
    }

    public boolean checkForSpam(Review review, String userIp, String userAgent) {
        return spamDetectionService.isSpam(review, userIp, userAgent);
    }

    public void setStatus(ReviewStatus status, Review review) {
        try {
            review.setStatus(status);
            updateReview(review.getId(), review);
        } catch (Exception ex) {
            logger.error("Error while setting status of review: {}", ex.getMessage());
            throw ex;
        }
    }

    private void updateReviewContentAndStatus(Review existingReview, Review updatedReview) {
        existingReview.setContent(updatedReview.getContent());
        existingReview.setStatus(updatedReview.getStatus());
    }

}
