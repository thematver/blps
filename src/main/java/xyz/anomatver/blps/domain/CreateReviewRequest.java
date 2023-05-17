package xyz.anomatver.blps.domain;

import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateReviewRequest {
    String title;
    String content;
    String username;
}
