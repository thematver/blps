package xyz.anomatver.blps.vote.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import xyz.anomatver.blps.review.model.Review;
import xyz.anomatver.blps.review.service.ReviewService;
import xyz.anomatver.blps.user.model.User;
import xyz.anomatver.blps.vote.dto.VoteDTO;
import xyz.anomatver.blps.vote.service.VoteService;

import java.util.List;

@RestController
@RequestMapping(value = "/api/vote", produces = "application/json")
public class VoteController {

    @Autowired
    private VoteService voteService;

    @Autowired
    private ReviewService reviewService;


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/reviews")
    public ResponseEntity<List<Review>> getReviewsForModeration(@AuthenticationPrincipal User user) {
        List<Review> reviews = voteService.findReviewsForModeration();
        return ResponseEntity.ok(reviews);
    }
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/{reviewId}")
    public ResponseEntity<Review> voteOnReview(
            @AuthenticationPrincipal User user,
            @RequestBody VoteDTO voteDTO) {

        Review review = reviewService.findById(voteDTO.getReviewId());
        Review updatedReview = voteService.vote(user, review, voteDTO.getVoteType());
        return ResponseEntity.ok(updatedReview);
    }
}