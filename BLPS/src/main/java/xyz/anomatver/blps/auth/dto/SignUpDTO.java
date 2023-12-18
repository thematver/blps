package xyz.anomatver.blps.auth.dto;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class SignUpDTO {
    private String username;
    private String password;
}