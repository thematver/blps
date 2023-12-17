package xyz.anomatver.blps.vote.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import xyz.anomatver.blps.review.model.Review;

@Service
public class SpamDetectionService {

    @Value("${AKISMET_KEY}")
    String apiKey;

    @Value("${server.url}")
    String blogUrl;

    private static final Logger logger = LoggerFactory.getLogger(SpamDetectionService.class);

    public boolean isSpam(Review review, String userIP, String userAgent) {
        try {
            String akismetUrl = "https://" + apiKey + ".rest.akismet.com/1.1/comment-check";

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "BLPS/1.0");
            headers.set("Content-Type", "application/x-www-form-urlencoded");

            String requestBody = "blog=" + blogUrl + "&user_ip=" + userIP + "&user_agent=" + userAgent + "&comment_content=" + review.getContent();

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(akismetUrl, HttpMethod.POST, entity, String.class);

            return "true".equalsIgnoreCase(response.getBody());
        } catch (Exception ex) {
            logger.error("Error while checking for spam: {}", ex.getMessage());
            throw ex;
        }
    }
}