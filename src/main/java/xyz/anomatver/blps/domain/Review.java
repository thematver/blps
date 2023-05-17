package xyz.anomatver.blps.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status;

    @Column(nullable = false)
    private int approveVotes;

    @Column(nullable = false)
    private int rejectVotes;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    public ReviewStatus getStatus() {
        return status;
    }

    public void setStatus(ReviewStatus status) {
        this.status = status;
    }

    public int getApproveVotes() {
        return approveVotes;
    }

    public void setApproveVotes(int approveVotes) {
        this.approveVotes = approveVotes;
    }

    public int getRejectVotes() {
        return rejectVotes;
    }

    public void setRejectVotes(int rejectVotes) {
        this.rejectVotes = rejectVotes;
    }
}


