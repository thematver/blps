package xyz.anomatver.blps.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.anomatver.blps.domain.Review;
import xyz.anomatver.blps.domain.User;
import xyz.anomatver.blps.service.ModeratorService;
import xyz.anomatver.blps.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/api/moderators")
public class ModeratorController {

    @Autowired
    private ModeratorService moderatorService;

    @Autowired
    private ReviewService reviewService;


//    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    @GetMapping("/reviews")
    public ResponseEntity<List<Review>> getReviewsForModeration() {
        List<Review> reviews = moderatorService.findReviewsForModeration();
        return ResponseEntity.ok(reviews);
    }

//    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    @PostMapping("/reviews/{reviewId}/vote")
    public ResponseEntity<Review> voteOnReview(
            @PathVariable Long reviewId,
            @RequestParam("moderator") String moderatorName,
            @RequestParam("approved") boolean approved) {

        User moderator = moderatorService.findByUsername(moderatorName);
        Review review = reviewService.findById(reviewId);
        Review updatedReview = moderatorService.reviewForModeration(moderator, review, approved);
        return ResponseEntity.ok(updatedReview);
    }
}