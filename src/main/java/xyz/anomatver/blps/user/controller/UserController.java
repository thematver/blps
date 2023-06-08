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

@RestController
@RequestMapping(value = "/api/users", produces = "application/json")
public class UserController {

    @Autowired
    private UserService userService;




//    @PostMapping("/permissions/{username}")
//    public ResponseEntity<Boolean> perms(@PathVariable String username) {
//        User user = userService.findByUsername(username);
//        moderatorService.grantRole(user);
//        return ResponseEntity.ok(true);
//    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        User user = userService.findById(id);
        UserResponse response = UserResponse.builder().username(user.getUsername()).reviews(userService.getApprovedReviews(user)).build();

        return ResponseEntity.ok().body(response);
    }

}
