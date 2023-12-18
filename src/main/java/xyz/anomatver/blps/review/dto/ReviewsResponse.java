package xyz.anomatver.blps.review.dto;

import lombok.Builder;
import xyz.anomatver.blps.review.model.Review;

import java.util.List;

@Builder
public class ReviewsResponse {
    List<Review> reviews;
    String error;
}
