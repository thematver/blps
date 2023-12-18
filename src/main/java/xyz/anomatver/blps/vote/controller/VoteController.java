package xyz.anomatver.blps.vote.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.anomatver.blps.auth.service.CustomUserDetailsService;
import xyz.anomatver.blps.review.model.Review;
import xyz.anomatver.blps.review.service.ReviewService;
import xyz.anomatver.blps.user.model.User;
import xyz.anomatver.blps.review.dto.ReviewsResponse;
import xyz.anomatver.blps.vote.dto.VoteDTO;
import xyz.anomatver.blps.vote.dto.VoteResponse;
import xyz.anomatver.blps.vote.service.VoteService;

import java.util.List;

@RestController
@RequestMapping(value = "/vote", produces = "application/json")
public class VoteController {
    private static final Logger logger = LoggerFactory.getLogger(VoteController.class);

    private final VoteService voteService;

    private final ReviewService reviewService;

    private final CustomUserDetailsService userDetailsService;

    public VoteController(VoteService voteService, ReviewService reviewService, CustomUserDetailsService userDetailsService) {
        this.voteService = voteService;
        this.reviewService = reviewService;
        this.userDetailsService = userDetailsService;
    }

    @GetMapping("/reviews")
    public ResponseEntity<ReviewsResponse> getReviewsForModeration() {
        try {
            User user = userDetailsService.getUser();
            List<Review> reviews = voteService.findReviewsForModeration(user);
            return ResponseEntity.ok(ReviewsResponse.builder().reviews(reviews).build());
        } catch (Exception ex) {
            logger.error("Error while fetching reviews for moderation: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ReviewsResponse.builder().error("Error while fetching reviews").build());
        }
    }

    @PostMapping("")
    public ResponseEntity<VoteResponse> vote(
            @RequestBody VoteDTO voteDTO) {
        try {
            User user = userDetailsService.getUser();
            Review review = reviewService.findById(voteDTO.getReviewId());
            Review updatedReview = voteService.vote(user, review, voteDTO.getVoteType());

            if (review == updatedReview) {
                return ResponseEntity.badRequest().body(VoteResponse.builder().error("Вы уже голосовали за этот комментарий").build());
            }
            return ResponseEntity.ok(VoteResponse.builder().review(updatedReview).build());
        } catch (Exception ex) {
            logger.error("Error while voting: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(VoteResponse.builder().error("Ошибка при голосовании").build());
        }
    }
}