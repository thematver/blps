package xyz.anomatver.blps.vote.processes;

import org.camunda.bpm.engine.delegate.DelegateExecution;

import java.util.Objects;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import xyz.anomatver.blps.auth.AuthJavaDelegate;
import xyz.anomatver.blps.auth.model.ERole;
import xyz.anomatver.blps.auth.service.AuthService;
import xyz.anomatver.blps.auth.service.CustomUserDetailsService;
import xyz.anomatver.blps.user.model.User;
import xyz.anomatver.blps.vote.model.Vote;
import xyz.anomatver.blps.vote.service.VoteService;

@Component
public class VoteProcess extends AuthJavaDelegate {

    private final VoteService voteService;
    private final CustomUserDetailsService userDetailsService;


    @Override
    public void execute(DelegateExecution execution) throws Exception {
        super.execute(execution);
        User user = userDetailsService.getUser();

        if (!user.hasRole(ERole.MODERATOR) && !user.hasRole(ERole.ADMIN)) {
            throw new AccessDeniedException("Недостаточно прав");
        }
            String reviewId = (String) execution.getVariable("selected_review");
            if (Objects.equals(execution.getVariable("vote_type"), "true")) {
                voteService.vote(user, Long.valueOf(reviewId), Vote.VoteType.POSITIVE);
            } else {
                voteService.vote(user, Long.valueOf(reviewId), Vote.VoteType.NEGATIVE);
            }

        execution.setVariable("shouldSkipDecision", voteService.shouldSkipDecision(Long.valueOf(reviewId)));
        execution.setVariable("shouldApprove", voteService.hasTotalMajorityOfVotes(Long.valueOf(reviewId)));
        execution.setVariable("reviewId", Long.valueOf(reviewId));
    }


    public VoteProcess(AuthService authService, VoteService voteService, CustomUserDetailsService userDetailsService) {
        super(authService);
        this.voteService = voteService;
        this.userDetailsService = userDetailsService;
    }


}
