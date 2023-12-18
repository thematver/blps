package xyz.anomatver.blps.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class AuthResponse {

    private static final String BEARER = "Bearer";
    private String accessToken;
    private String refreshToken;
    private String error;
}