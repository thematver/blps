package xyz.anomatver.blps.vote.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.anomatver.blps.auth.service.CustomUserDetailsService;
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

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @GetMapping("/reviews")
    public ResponseEntity<List<Review>> getReviewsForModeration() {
        User user = userDetailsService.getUser();
        List<Review> reviews = voteService.findReviewsForModeration(user);
        return ResponseEntity.ok(reviews);
    }

    @PostMapping("")
    public ResponseEntity<?> vote(
            @RequestBody VoteDTO voteDTO) {
        User user = userDetailsService.getUser();
        Review review = reviewService.findById(voteDTO.getReviewId());
        Review updatedReview = voteService.vote(user, review, voteDTO.getVoteType());
        if (review == updatedReview) {
            return ResponseEntity.badRequest().body("Вы уже проголосовали за этот опрос.");
        }
        return ResponseEntity.ok(updatedReview);
    }
}