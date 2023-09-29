package xyz.anomatver.blps.review.processes;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.anomatver.blps.auth.AuthJavaDelegate;
import xyz.anomatver.blps.review.model.Review;
import xyz.anomatver.blps.review.model.ReviewStatus;
import xyz.anomatver.blps.review.service.ReviewService;

@Component
public class SendToManualProcess extends AuthJavaDelegate {

    private final ReviewService reviewService;

    @Autowired
    public SendToManualProcess(ReviewService reviewService) {
        this.reviewService = reviewService;
    }


    @Override
    public void execute(DelegateExecution execution) throws Exception {
        super.execute(execution);
        long reviewId = (Long) execution.getVariable("reviewId");
        Review review = reviewService.findById(reviewId);

        reviewService.setStatus(ReviewStatus.PENDING, review);
    }
}