package xyz.anomatver.blps.review.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import xyz.anomatver.blps.auth.service.CustomUserDetailsService;
import xyz.anomatver.blps.review.dto.CreateReviewDTO;
import xyz.anomatver.blps.review.model.Review;
import xyz.anomatver.blps.review.model.ReviewStatus;
import xyz.anomatver.blps.review.service.ReviewService;
import xyz.anomatver.blps.user.model.User;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);

    private final ReviewService reviewService;
    private final CustomUserDetailsService userDetailsService;

    @Autowired
    public ReviewController(ReviewService reviewService, CustomUserDetailsService userDetailsService) {
        this.reviewService = reviewService;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody CreateReviewDTO body) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String userIp = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        try {
            User user = userDetailsService.getUser();
            Review review = Review.builder()
                    .title(body.getTitle())
                    .content(body.getContent())
                    .author(user)
                    .build();
            reviewService.submitReview(review, userIp, userAgent);
            logger.info("Review submitted successfully");
            return new ResponseEntity<>("Review submitted successfully", HttpStatus.CREATED);
        } catch (Exception ex) {
            logger.error("Failed to submit review: {}", ex.getMessage());
            return new ResponseEntity<>("Failed to submit review", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getReviewById(@PathVariable Long id) {
        try {
        Review review = reviewService.findById(id);
        if (review != null) {
            return new ResponseEntity<>(review, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Review not found", HttpStatus.NOT_FOUND);
        }
        } catch (Exception ex) {
            logger.error("Error occurred while fetching review: {}", ex.getMessage());
            return new ResponseEntity<>("Error occurred while fetching review", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews() {
        try {
            List<Review> reviews = reviewService.findAll().stream()
                    .filter(review -> review.getStatus() == ReviewStatus.APPROVED)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(reviews, HttpStatus.OK);
        } catch (Exception ex) {
        logger.error("Error occurred while fetching reviews: {}", ex.getMessage());
        return new ResponseEntity("Error occurred while fetching reviews", HttpStatus.INTERNAL_SERVER_ERROR);
    }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateReview(@PathVariable Long id, @RequestBody Review reviewDTO) {
        try {
            Review updatedReview = reviewService.updateReview(id, reviewDTO);
            return new ResponseEntity<>(updatedReview, HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("Failed to update review: {}", ex.getMessage());
            return new ResponseEntity<>("Failed to update review", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable Long id) {
        try {
            reviewService.deleteReview(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception ex) {
            logger.error("Failed to delete review: {}", ex.getMessage());
            return new ResponseEntity<>("Failed to delete review", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
