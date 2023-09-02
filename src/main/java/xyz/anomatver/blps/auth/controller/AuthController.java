package xyz.anomatver.blps.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.anomatver.blps.auth.dto.AuthResponse;
import xyz.anomatver.blps.auth.dto.LoginDTO;
import xyz.anomatver.blps.auth.dto.SignUpDTO;
import xyz.anomatver.blps.auth.exceptions.UsernameAlreadyTakenException;
import xyz.anomatver.blps.auth.service.AuthService;

@RestController
@RequestMapping(value = "/api/auth", produces = "application/json")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@RequestBody LoginDTO loginDto) {
        String token = authService.login(loginDto);
        if (token != null) {
            return ResponseEntity.ok(AuthResponse.builder().accessToken(token).build());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(AuthResponse.builder().error("Неверный логин или пароль.").build());
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignUpDTO signUpDto) {
        try {
            String token = authService.register(signUpDto);
            return ResponseEntity.ok(AuthResponse.builder().accessToken(token).build());
        } catch (UsernameAlreadyTakenException ex) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(AuthResponse.builder().error("Имя пользователя уже занято.").build());
        }
    }
}
