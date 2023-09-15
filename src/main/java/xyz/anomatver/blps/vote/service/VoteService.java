package xyz.anomatver.blps.vote.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xyz.anomatver.blps.auth.model.ERole;
import xyz.anomatver.blps.mqtt.MessageSenderService;
import xyz.anomatver.blps.review.model.Review;
import xyz.anomatver.blps.review.model.ReviewStatus;
import xyz.anomatver.blps.review.repository.ReviewRepository;
import xyz.anomatver.blps.user.model.User;
import xyz.anomatver.blps.user.repository.UserRepository;
import xyz.anomatver.blps.vote.model.Vote;
import xyz.anomatver.blps.vote.repository.VoteRepository;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final VoteRepository voteRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final MessageSenderService messageSenderService;

    public Review vote(User moderator, Review review, Vote.VoteType type) {
        addVoteIfNotPresent(moderator, review, type);

        determineReviewStatus(review, type);

        return reviewRepository.save(review);
    }

    public List<Review> findReviewsForModeration(User user) {
        return reviewRepository.findAllByStatus(ReviewStatus.PENDING).stream()
                .filter(review -> hasNotVoted(review, user))
                .toList();
    }

    private void addVoteIfNotPresent(User moderator, Review review, Vote.VoteType type) {
        if (review.getVotes().stream().noneMatch(vote -> Objects.equals(vote.getUser().getId(), moderator.getId()))) {
            Vote vote = Vote.builder().voteType(type).user(moderator).build();
            review.getVotes().add(vote);
            reviewRepository.save(review);
        }
    }

    private void determineReviewStatus(Review review, Vote.VoteType type) {
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
    }

    private boolean hasNotVoted(Review review, User user) {
        return review.getVotes().stream().noneMatch(vote -> Objects.equals(vote.getUser().getId(), user.getId()));
    }
}
