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
import xyz.anomatver.blps.review.dto.ReviewResponse;
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
    public ResponseEntity<ReviewResponse> createReview(@RequestBody CreateReviewDTO body) {
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
            return new ResponseEntity(ReviewResponse.builder().review(review).build(), HttpStatus.CREATED);
        } catch (Exception ex) {
            logger.error("Failed to submit review: {}", ex.getMessage());
            return new ResponseEntity(ReviewResponse.builder().error("Failed to submit review").build(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponse> getReviewById(@PathVariable Long id) {
        try {
            Review review = reviewService.findById(id);
            if (review != null) {
                return ResponseEntity.ok(ReviewResponse.builder().review(review).build());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ReviewResponse.builder().error("Review not found").build());
            }
        } catch (Exception ex) {
            logger.error("Error occurred while fetching review: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ReviewResponse.builder().error("Error occurred while fetching review").build());
        }
    }

    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews() {
        try {
            List<Review> reviews = reviewService.findAll().stream()
                    .filter(review -> review.getStatus() == ReviewStatus.APPROVED)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(reviews);
        } catch (Exception ex) {
            logger.error("Error occurred while fetching reviews: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewResponse> updateReview(@PathVariable Long id, @RequestBody Review reviewDTO) {
        try {
            Review updatedReview = reviewService.updateReview(id, reviewDTO);
            return ResponseEntity.ok(ReviewResponse.builder().review(updatedReview).build());
        } catch (Exception ex) {
            logger.error("Failed to update review: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ReviewResponse.builder().error("Failed to update review").build());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ReviewResponse> deleteReview(@PathVariable Long id) {
        try {
            reviewService.deleteReview(id);
            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            logger.error("Failed to delete review: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ReviewResponse.builder().error("Failed to delete review").build());
        }
    }
}