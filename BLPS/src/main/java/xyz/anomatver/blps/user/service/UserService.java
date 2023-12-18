package xyz.anomatver.blps.user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xyz.anomatver.blps.exception.NotFoundException;
import xyz.anomatver.blps.mqtt.MessageSenderService;
import xyz.anomatver.blps.review.model.Review;
import xyz.anomatver.blps.review.model.ReviewStatus;
import xyz.anomatver.blps.review.repository.ReviewRepository;
import xyz.anomatver.blps.user.model.User;
import xyz.anomatver.blps.user.repository.UserRepository;

import java.util.List;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;

    private final ReviewRepository reviewRepository;

    private final MessageSenderService messageService;

    public UserService(UserRepository userRepository, ReviewRepository reviewRepository, MessageSenderService messageService) {
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.messageService = messageService;
    }

    public User findById(Long id) {
        try {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found with id: " + id));
        } catch (Exception ex) {
            logger.error("Error while finding user by ID: {}", ex.getMessage());
            throw ex;
        }
    }

    public List<Review> getApprovedReviews(User user) {
        try {
            return reviewRepository.getReviewsByAuthor(user).stream().filter(review -> review.getStatus() == ReviewStatus.APPROVED).toList();
        } catch (Exception ex) {
            logger.error("Error while fetching approved reviews: {}", ex.getMessage());
            return List.of();
        }
    }

    public boolean checkByUsername(String username) {
        try {
            return userRepository.findByUsername(username).isPresent();
        } catch (Exception ex) {
            logger.error("Error while checking user by username: {}", ex.getMessage());
            throw ex;
        }
    }

    public User link(User user, String uuid) {
        try {
            messageService.sendLinkingMessage(String.valueOf(user.getId()), uuid);
            return user;
        }  catch (Exception ex) {
            logger.error("Error while linking user: {}", ex.getMessage());
            throw ex;
        }
    }
}
