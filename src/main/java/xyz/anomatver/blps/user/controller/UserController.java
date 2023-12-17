package xyz.anomatver.blps.user.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.anomatver.blps.auth.service.CustomUserDetailsService;
import xyz.anomatver.blps.review.model.Review;
import xyz.anomatver.blps.user.dto.UserResponse;
import xyz.anomatver.blps.user.model.User;
import xyz.anomatver.blps.user.service.UserService;

import java.util.List;

@RestController
@RequestMapping(value = "/users", produces = "application/json")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final CustomUserDetailsService userDetailsService;
    public UserController(UserService userService, CustomUserDetailsService userDetailsService) {
        this.userService = userService;
        this.userDetailsService = userDetailsService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            User user = userService.findById(id);
            String username = user.getUsername();
            List<Review> approvedReviews = userService.getApprovedReviews(user);
            UserResponse response = UserResponse
                    .builder()
                    .username(username)
                    .reviews(approvedReviews)
                    .build();

            return ResponseEntity.ok().body(response);
        }  catch (Exception ex) {
            logger.error("Error while fetching user by ID: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching user by ID");
        }
    }




    @GetMapping("/link")
    public ResponseEntity<?> link(
            @RequestParam String hash
    ) {
        try {
            User currentUser = userDetailsService.getUser();
            User updatedUser = userService.link(currentUser, hash);

            return ResponseEntity.ok().body("UUID linked successfully for user: " + updatedUser.getUsername());
        } catch (Exception ex) {
            logger.error("Error while linking UUID: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error linking UUID");
        }
    }

}
