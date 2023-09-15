package xyz.anomatver.blps.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import xyz.anomatver.blps.mqtt.MessageSenderService;
import xyz.anomatver.blps.review.service.ReviewService;


@EnableScheduling
@Configuration
public class SchedulerConfig {

    private final ReviewService reviewService;
    private final MessageSenderService messageSenderService;

    public SchedulerConfig(ReviewService reviewService, MessageSenderService messageSenderService) {
        this.reviewService = reviewService;
        this.messageSenderService = messageSenderService;
    }


    @Scheduled(cron = "* * * 10 * *")
    public void sendYesterdayStats() {
        messageSenderService.sendYesterdayNotificationMessage(String.valueOf(reviewService.getYesterdayReviews().size()));
    }
}
