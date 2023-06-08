package xyz.anomatver.blps.vote.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import xyz.anomatver.blps.auth.model.ERole;
import xyz.anomatver.blps.review.model.Review;
import xyz.anomatver.blps.review.model.ReviewStatus;
import xyz.anomatver.blps.user.model.User;
import xyz.anomatver.blps.review.repository.ReviewRepository;
import xyz.anomatver.blps.user.repository.UserRepository;
import xyz.anomatver.blps.vote.model.Vote;
import xyz.anomatver.blps.vote.repository.VoteRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Set;


@Service
@AllArgsConstructor
public class VoteService {

    private VoteRepository voteRepository;

    private UserRepository userRepository;

    private ReviewRepository reviewRepository;


    public Review vote(User moderator, Review review, Vote.VoteType type) {
            Vote vote = Vote.builder().voteType(type).user(moderator).build();
            if (review.getVotes().stream().noneMatch(votes -> Objects.equals(votes.getUser().getId(), moderator.getId()))) {
                review.getVotes().add(vote);
                reviewRepository.save(review);
            }
            else {
                return review;
            }

        // Определение статуса рецензии после голосования

        int totalVotes = review.getVotes().size() + 1;
        int positiveVotes = (int) review.getVotes().stream().filter(i -> i.getVoteType() == Vote.VoteType.POSITIVE).count();
        int negativeVotes = (int) review.getVotes().stream().filter(i -> i.getVoteType() == Vote.VoteType.NEGATIVE).count();

        if (type == Vote.VoteType.POSITIVE) {
            positiveVotes += 1;
        } else {
            negativeVotes +=1;
        }

        int majority = userRepository.countUsersByRolesContains(ERole.MODERATOR);

        if (positiveVotes > majority) {
            review.setStatus(ReviewStatus.APPROVED);
        } else if (negativeVotes > majority) {
            review.setStatus(ReviewStatus.REJECTED);
        }

        return reviewRepository.save(review);
    }

    public List<Review> findReviewsForModeration(User user) {
        return reviewRepository.findAllByStatus(ReviewStatus.PENDING).stream()
                .filter(review -> review.getVotes().stream()
                        .noneMatch(vote -> Objects.equals(vote.getUser().getId(), user.getId()))).toList();
    }

}