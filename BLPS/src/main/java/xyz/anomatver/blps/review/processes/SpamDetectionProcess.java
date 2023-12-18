package xyz.anomatver.blps.review.processes;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;
import xyz.anomatver.blps.auth.AuthJavaDelegate;
import xyz.anomatver.blps.auth.service.AuthService;
import xyz.anomatver.blps.review.model.Review;
import xyz.anomatver.blps.review.service.ReviewService;

@Component
public class SpamDetectionProcess extends AuthJavaDelegate {

    private final ReviewService reviewService;

    public SpamDetectionProcess(AuthService authService, ReviewService reviewService) {
        super(authService);
        this.reviewService = reviewService;
    }


    @Override
    public void execute(DelegateExecution execution) throws Exception {
        super.execute(execution);
        long reviewId = (Long) execution.getVariable("reviewId");
        Review review = reviewService.findById(reviewId);
        execution.setVariable("result", reviewService.checkForSpam(review, "0.0.0.0", "Camunda/1.0"));
    }
}