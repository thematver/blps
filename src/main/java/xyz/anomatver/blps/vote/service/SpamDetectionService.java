package xyz.anomatver.blps.vote.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import xyz.anomatver.blps.review.model.Review;

@Service
public class SpamDetectionService {
    public boolean isSpam(Review review, String userIP, String userAgent) {
        String akismetApiKey = "162b8b4bb01a";
        String akismetBlog = "https://yourwebsite.com"; // Replace with your website URL

        String akismetCheckUrl = "https://" + akismetApiKey + ".rest.akismet.com/1.1/comment-check";

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "YourAppName/1.0");
        headers.set("Content-Type", "application/x-www-form-urlencoded");

        String requestBody = "blog=" + akismetBlog + "&user_ip=" + userIP + "&user_agent=" + userAgent + "&comment_content=" + review.getContent();

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(akismetCheckUrl, HttpMethod.POST, entity, String.class);

        return "true".equalsIgnoreCase(response.getBody());
    }
}