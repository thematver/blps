package xyz.anomatver.blps.service;

import xyz.anomatver.blps.domain.Review;
import xyz.anomatver.blps.domain.User;

import java.util.List;

public interface ModeratorService {
    User findByUsername(String username);
    Review reviewForModeration(User moderator, Review review, boolean approved);
   List<Review> findReviewsForModeration();

   void grantRole(User user);

}