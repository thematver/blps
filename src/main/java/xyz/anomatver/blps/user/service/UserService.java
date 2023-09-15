package xyz.anomatver.blps.user.service;

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

    private final UserRepository userRepository;

    private final ReviewRepository reviewRepository;

    private final MessageSenderService messageService;

    public UserService(UserRepository userRepository, ReviewRepository reviewRepository, MessageSenderService messageService) {
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.messageService = messageService;
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found with id: " + id));
    }

    public List<Review> getApprovedReviews(User user) {
        return reviewRepository.getReviewsByAuthor(user).stream().filter(review -> review.getStatus() == ReviewStatus.APPROVED).toList();
    }

    public boolean checkByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public User link(User user, String uuid) {
        messageService.sendLinkingMessage(String.valueOf(user.getId()), uuid);
        return user;
    }
}
