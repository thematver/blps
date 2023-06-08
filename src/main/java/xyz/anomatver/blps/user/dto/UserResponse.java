package xyz.anomatver.blps.user.dto;

import lombok.Builder;
import lombok.Data;
import xyz.anomatver.blps.review.model.Review;

import java.util.List;

@Data
@Builder
public class UserResponse {
    private String username;
    private List<Review> reviews;
}