package xyz.anomatver.blps.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.anomatver.blps.exception.NotFoundException;
import xyz.anomatver.blps.review.model.Review;
import xyz.anomatver.blps.review.model.ReviewStatus;
import xyz.anomatver.blps.review.repository.ReviewRepository;
import xyz.anomatver.blps.user.model.User;
import xyz.anomatver.blps.user.repository.UserRepository;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
    }

    public List<Review> getApprovedReviews(User user) {
         return reviewRepository.getReviewsByAuthor(user).stream().filter(review -> review.getStatus() == ReviewStatus.APPROVED).toList();
    }

    public boolean checkByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }
}
