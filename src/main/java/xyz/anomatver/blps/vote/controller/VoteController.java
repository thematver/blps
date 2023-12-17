package xyz.anomatver.blps.vote.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.anomatver.blps.auth.service.CustomUserDetailsService;
import xyz.anomatver.blps.review.dto.ReviewResponse;
import xyz.anomatver.blps.review.model.Review;
import xyz.anomatver.blps.review.service.ReviewService;
import xyz.anomatver.blps.user.model.User;
import xyz.anomatver.blps.vote.dto.VoteDTO;
import xyz.anomatver.blps.vote.service.VoteService;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping(value = "/vote", produces = "application/json")
public class VoteController {
    private static final Logger logger = LoggerFactory.getLogger(VoteController.class);

    final private VoteService voteService;

    final private ReviewService reviewService;

    final private CustomUserDetailsService userDetailsService;

    public VoteController(VoteService voteService, ReviewService reviewService, CustomUserDetailsService userDetailsService) {
        this.voteService = voteService;
        this.reviewService = reviewService;
        this.userDetailsService = userDetailsService;
    }

    @GetMapping("/reviews")
    public ResponseEntity<List<Review>> getReviewsForModeration() {
        try {
            User user = userDetailsService.getUser();
            List<Review> reviews = voteService.findReviewsForModeration(user);
            return ResponseEntity.ok(reviews);
        } catch (Exception ex) {
            logger.error("Error while fetching reviews for moderation: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @PostMapping("")
    public ResponseEntity<ReviewResponse> vote(
            @RequestBody VoteDTO voteDTO) {
        try {
            User user = userDetailsService.getUser();
            Review review = reviewService.findById(voteDTO.getReviewId());
            Review updatedReview = voteService.vote(user, review, voteDTO.getVoteType());

            if (review == updatedReview) {
                return ResponseEntity.badRequest().body(ReviewResponse.builder().error("Вы уже голосовали за этот комментарий").build());
            }
            return ResponseEntity.ok(ReviewResponse.builder().review(updatedReview).build());
        } catch (Exception ex) {
            logger.error("Error while voting: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ReviewResponse.builder().error("Ошибка при голосовании").build());
        }
    }
}