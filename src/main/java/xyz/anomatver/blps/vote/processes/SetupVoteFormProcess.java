package xyz.anomatver.blps.vote.processes;

import org.camunda.bpm.engine.delegate.DelegateExecution;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import xyz.anomatver.blps.auth.AuthJavaDelegate;
import xyz.anomatver.blps.auth.model.ERole;
import xyz.anomatver.blps.auth.service.CustomUserDetailsService;
import xyz.anomatver.blps.review.model.Review;
import xyz.anomatver.blps.user.model.User;
import xyz.anomatver.blps.vote.service.VoteService;

@Component
public class SetupVoteFormProcess extends AuthJavaDelegate {

    private final VoteService voteService;
    private final CustomUserDetailsService userDetailsService;

    private String buildReviews(){
        List<Review> reviews = voteService.findReviewsForModeration(userDetailsService.getUser());
        List<String> internals = new ArrayList<>();
        for (Review review: reviews){
            internals.add(
                    String.format(
                            "{\"label\": \"%s\", \"value\": \"%s\"}",
                            review.getContent(), review.getId()
                    )
            );
        }
        return "[" + String.join(",", internals) + "]";
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        super.execute(execution);
        User user = userDetailsService.getUser();

        if (!user.hasRole(ERole.MODERATOR) && !user.hasRole(ERole.ADMIN)) {
            throw new AccessDeniedException("Недостаточно прав");
        }
        execution.setVariable("reviews_form_input", buildReviews());
    }
    @Autowired
    public SetupVoteFormProcess(VoteService voteService, CustomUserDetailsService userDetailsService) {
        this.voteService = voteService;
        this.userDetailsService = userDetailsService;
    }


}
