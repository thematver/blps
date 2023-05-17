package xyz.anomatver.blps.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.anomatver.blps.domain.Review;
import xyz.anomatver.blps.domain.ReviewStatus;
import xyz.anomatver.blps.domain.User;
import xyz.anomatver.blps.domain.UserRole;
import xyz.anomatver.blps.repository.ModeratorRepository;
import xyz.anomatver.blps.repository.ReviewRepository;

import java.util.List;

@Service
public class ModeratorServiceImpl implements ModeratorService {

    @Autowired
    private ModeratorRepository moderatorRepository;

    @Autowired
    private ReviewRepository reviewRepository;


    @Override
    public User findByUsername(String username) {
       User user = moderatorRepository.findByUsername(username);
       return user;
    }

    @Override
    public Review reviewForModeration(User moderator, Review review, boolean approved) {
        // Увеличиваем счетчик голосов в зависимости от решения модератора
        if (approved) {
            review.setApproveVotes(review.getApproveVotes() + 1);
        } else {
            review.setRejectVotes(review.getRejectVotes() + 1);
        }

        // Определение статуса рецензии после голосования
        int totalVotes = 3;
        int majority = (int) Math.ceil(totalVotes / 2.0);

        if (review.getApproveVotes() > majority) {
            review.setStatus(ReviewStatus.APPROVED);
        } else if (review.getRejectVotes() > majority) {
            review.setStatus(ReviewStatus.REJECTED);
        }

        return reviewRepository.save(review);
    }

    public List<Review> findReviewsForModeration() {
        return reviewRepository.findAllByStatus(ReviewStatus.PENDING);
    }

    @Override
    public void grantRole(User user) {
        user.setRole("MODERATOR");
        moderatorRepository.save(user);
    }
}