package xyz.anomatver.blps.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import xyz.anomatver.blps.exception.NotFoundException;
import xyz.anomatver.blps.review.model.Review;
import xyz.anomatver.blps.review.repository.ReviewRepository;
import xyz.anomatver.blps.review.service.ReviewService;
import xyz.anomatver.blps.user.dto.UserResponse;
import xyz.anomatver.blps.user.model.User;
import xyz.anomatver.blps.user.service.UserService;

import java.util.List;

@RestController
@RequestMapping(value = "/api/users", produces = "application/json")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        User user = userService.findById(id);
        String username = user.getUsername();
        List<Review> approvedReviews = userService.getApprovedReviews(user);
        UserResponse response = UserResponse
                                .builder()
                                .username(username)
                                .reviews(approvedReviews)
                                .build();

        return ResponseEntity.ok().body(response);
    }

}
