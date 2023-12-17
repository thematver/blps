package xyz.anomatver.blps.review.processes;



import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.anomatver.blps.auth.AuthJavaDelegate;
import xyz.anomatver.blps.auth.service.CustomUserDetailsService;
import xyz.anomatver.blps.review.model.Review;
import xyz.anomatver.blps.review.model.ReviewStatus;
import xyz.anomatver.blps.review.service.ReviewService;


@Component
public class ReviewValidationProcess extends AuthJavaDelegate {

    private final ReviewService reviewService;

    private final CustomUserDetailsService userDetailsService;
    @Autowired
    public ReviewValidationProcess(ReviewService reviewService, CustomUserDetailsService userService) {
        this.reviewService = reviewService;
        this.userDetailsService = userService;
    }


    @Override
    public void execute(DelegateExecution execution) throws Exception {
        super.execute(execution);
        String title = (String) execution.getVariable("review_title");
        String content = (String) execution.getVariable("review_content");

        Review review = Review.builder()
                .title(title)
                .content(content)
                .status(ReviewStatus.PENDING)
                .author(userDetailsService.getUser())
                .build();

        execution.setVariable("result", reviewService.validate(review));

        long reviewId = reviewService.save(review);
        execution.setVariable("reviewId", reviewId);
    }
}