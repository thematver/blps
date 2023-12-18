package xyz.anomatver.blps.vote.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xyz.anomatver.blps.auth.model.ERole;
import xyz.anomatver.blps.review.model.Review;
import xyz.anomatver.blps.review.model.ReviewStatus;
import xyz.anomatver.blps.review.repository.ReviewRepository;
import xyz.anomatver.blps.review.service.ReviewService;
import xyz.anomatver.blps.user.model.User;
import xyz.anomatver.blps.user.repository.UserRepository;
import xyz.anomatver.blps.vote.error.ReviewProcessException;
import xyz.anomatver.blps.vote.error.VoteNotFoundException;
import xyz.anomatver.blps.vote.model.Vote;
import xyz.anomatver.blps.vote.repository.VoteRepository;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class VoteService {
    private static final Logger logger = LoggerFactory.getLogger(VoteService.class);

    private VoteRepository voteRepository;
    private  UserRepository userRepository;
    private ReviewRepository reviewRepository;
    private ReviewService reviewService;

    public VoteService(VoteRepository voteRepository, UserRepository userRepository, ReviewRepository reviewRepository, ReviewService reviewService) {
        this.voteRepository = voteRepository;
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.reviewService = reviewService;
    }


    public Review vote(User moderator, Review review, Vote.VoteType type) {
        try {
            addVoteIfNotPresent(moderator, review, type);
            determineReviewStatus(review, type);
            return reviewRepository.save(review);
        } catch (Exception ex) {
            logger.error("Error while processing vote: {}", ex.getMessage());
            throw new ReviewProcessException("Error processing the vote for the review.");
        }
    }

    public void vote(User moderator, Long reviewId, Vote.VoteType type) {
        try {
            Review review = reviewService.findById(reviewId);
            if (review == null) {
                throw new VoteNotFoundException("Review not found for id: " + reviewId);
            }
            addVoteIfNotPresent(moderator, review, type);
            reviewRepository.save(review);
        } catch (Exception ex) {
            logger.error("Error while voting for a review: {}", ex.getMessage());
            throw new ReviewProcessException("Error occurred during voting for the review.");
        }
    }



    public boolean shouldSkipDecision(Long reviewId) {
        long totalVotes = reviewService.findById(reviewId).getVotes().size();
        long majority = userRepository.countUsersByRolesContains(ERole.MODERATOR);
        return totalVotes < majority / 2;
    }

    public boolean hasTotalMajorityOfVotes(Long reviewId) {
        Review review = reviewService.findById(reviewId);
        long positiveVotes = review.getVotes().stream().filter(vote -> vote.getVoteType() == Vote.VoteType.POSITIVE).count();

        long majority = userRepository.countUsersByRolesContains(ERole.MODERATOR);

        return positiveVotes >= majority / 2;
    }



    public List<Review> findReviewsForModeration(User user) {
        return reviewRepository.findAllByStatus(ReviewStatus.PENDING).stream()
                .filter(review -> hasNotVoted(review, user))
                .toList();
    }

    private void addVoteIfNotPresent(User moderator, Review review, Vote.VoteType type) {
        try {
            if (review.getVotes().stream().noneMatch(vote -> Objects.equals(vote.getUser().getId(), moderator.getId()))) {
                Vote vote = Vote.builder().voteType(type).user(moderator).build();
                review.getVotes().add(vote);
                reviewRepository.save(review);
            }
        }  catch (Exception ex) {
            logger.error("Error while adding vote: {}", ex.getMessage());
            throw ex;
        }
    }

    private void determineReviewStatus(Review review, Vote.VoteType type) {
        try {
            long totalVotes = review.getVotes().size();
            long positiveVotes = review.getVotes().stream().filter(vote -> vote.getVoteType() == Vote.VoteType.POSITIVE).count();
            long negativeVotes = totalVotes - positiveVotes;

            if (type == Vote.VoteType.POSITIVE) positiveVotes++;
            else negativeVotes++;

            long majority = userRepository.countUsersByRolesContains(ERole.MODERATOR);

            if (positiveVotes > majority) {
                review.setStatus(ReviewStatus.APPROVED);
            } else if (negativeVotes > majority) {
                review.setStatus(ReviewStatus.REJECTED);
            }
        } catch (Exception ex) {
            logger.error("Error while determining review status: {}", ex.getMessage());
            throw ex;
        }
    }

    private boolean hasNotVoted(Review review, User user) {
        try {
            return review.getVotes().stream().noneMatch(vote -> Objects.equals(vote.getUser().getId(), user.getId()));
        } catch (Exception ex) {
            logger.error("Error while checking if user has voted: {}", ex.getMessage());
            throw ex;
        }
    }
}
