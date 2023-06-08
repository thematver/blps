package xyz.anomatver.blps.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponse {

    private final String type = "Bearer";
    private String accessToken;
    private String refreshToken;

}