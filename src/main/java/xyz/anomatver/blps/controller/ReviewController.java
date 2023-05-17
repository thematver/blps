package xyz.anomatver.blps.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.anomatver.blps.domain.CreateReviewRequest;
import xyz.anomatver.blps.domain.Review;
import xyz.anomatver.blps.domain.User;
import xyz.anomatver.blps.service.ReviewService;
import xyz.anomatver.blps.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserService userService;
    @PostMapping("")
    public ResponseEntity<String> createReview(@RequestBody CreateReviewRequest request) {
        User user = userService.findByUsername(request.getUsername());
        reviewService.submitReview(user, Review.builder().title(request.getTitle())
                .content(request.getContent()).build());
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/{id}")
    public ResponseEntity<Review> getReviewById(@PathVariable Long id) {
        Review review = reviewService.findById(id);
        return ResponseEntity.ok(review);
    }

    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews() {
        List<Review> reviews = reviewService.findAll();
        return ResponseEntity.ok(reviews);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Review> updateReview(@PathVariable Long id, @RequestBody Review review) {
        Review updatedReview = reviewService.updateReview(id, review);
        return ResponseEntity.ok(updatedReview);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}
