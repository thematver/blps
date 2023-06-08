package xyz.anomatver.blps.auth.dto;

import lombok.Data;

@Data
public class SignUpDTO {
    private String name;
    private String username;
    private String password;
}