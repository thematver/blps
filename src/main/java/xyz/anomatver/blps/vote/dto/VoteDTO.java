package xyz.anomatver.blps.vote.dto;

import lombok.Data;
import xyz.anomatver.blps.vote.model.Vote;

@Data
public class VoteDTO {
    private long reviewId;
    private Vote.VoteType voteType;
}
