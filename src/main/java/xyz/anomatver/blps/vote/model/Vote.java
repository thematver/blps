package xyz.anomatver.blps.vote.model;

import lombok.*;
import xyz.anomatver.blps.review.model.Review;
import xyz.anomatver.blps.user.model.User;

import javax.persistence.*;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class Vote {

    public enum VoteType {
        POSITIVE, NEGATIVE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Enumerated(EnumType.STRING)
    private VoteType voteType;

    // Each vote is associated with one user
    @ManyToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name="user_id")
    private User user;
}