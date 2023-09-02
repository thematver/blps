package xyz.anomatver.blps.review.model;

import lombok.*;
import xyz.anomatver.blps.user.model.User;
import xyz.anomatver.blps.vote.model.Vote;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "review")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @ManyToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "author_id")
    private User author;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status;

    @OneToMany(cascade = {CascadeType.ALL})
    private Set<Vote> votes = new HashSet<Vote>();


}


