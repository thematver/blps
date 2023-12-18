package xyz.anomatver.blps.vote.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xyz.anomatver.blps.vote.model.Vote;


@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

}