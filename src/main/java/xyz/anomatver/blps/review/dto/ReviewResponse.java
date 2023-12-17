package xyz.anomatver.blps.review.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import xyz.anomatver.blps.review.model.Review;

@Data
@Builder
@Getter
@Setter
public class ReviewResponse {
    private Review review;
    private String error;
}
