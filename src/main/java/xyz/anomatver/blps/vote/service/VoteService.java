package xyz.anomatver.blps.vote.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.anomatver.blps.auth.model.Role;
import xyz.anomatver.blps.review.model.Review;
import xyz.anomatver.blps.review.model.ReviewStatus;
import xyz.anomatver.blps.user.model.User;
import xyz.anomatver.blps.review.repository.ReviewRepository;
import xyz.anomatver.blps.user.repository.UserRepository;
import xyz.anomatver.blps.vote.model.Vote;
import xyz.anomatver.blps.vote.repository.VoteRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;

@Service
public class VoteService{

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Transactional
    public Review vote(User moderator, Review review, Vote.VoteType type) {
            Vote vote = Vote.builder().voteType(type).user(moderator).review(review).build();
            voteRepository.save(vote);


        // Определение статуса рецензии после голосования

        int totalVotes = review.getVotes().size() + 1;
        int positiveVotes = (int) review.getVotes().stream().filter(i -> i.getVoteType() == Vote.VoteType.POSITIVE).count();
        int negativeVotes = (int) review.getVotes().stream().filter(i -> i.getVoteType() == Vote.VoteType.NEGATIVE).count();

        if (type == Vote.VoteType.POSITIVE) {
            positiveVotes += 1;
        } else {
            negativeVotes +=1;
        }

        int majority = userRepository.countUsersByRolesContains(Role.builder().name("ROLE_MODERATOR").build());

        if (positiveVotes > majority) {
            review.setStatus(ReviewStatus.APPROVED);
        } else if (negativeVotes > majority) {
            review.setStatus(ReviewStatus.REJECTED);
        }

        return reviewRepository.save(review);
    }

    public List<Review> findReviewsForModeration() {
        return reviewRepository.findAllByStatus(ReviewStatus.PENDING);
    }

}