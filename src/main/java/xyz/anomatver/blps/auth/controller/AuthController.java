package xyz.anomatver.blps.auth.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequestMapping(value = "/auth", produces = "application/json")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@RequestBody LoginDTO loginDto) {
        logger.info("Received login request for user: {}", loginDto.getUsername());

        String token = authService.login(loginDto);
        if (token != null) {
            logger.info("User {} logged in successfully", loginDto.getUsername());
            return ResponseEntity.ok(AuthResponse.builder().accessToken(token).build());
        } else {
            logger.warn("Failed login attempt for user: {}", loginDto.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(AuthResponse.builder().error("Неверный логин или пароль.").build());
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> registerUser(@RequestBody SignUpDTO signUpDto) {
        logger.info("Received signup request for user: {}", signUpDto.getUsername());

        try {
            String token = authService.register(signUpDto);
            logger.info("User {} registered successfully", signUpDto.getUsername());
            return ResponseEntity.ok(AuthResponse.builder().accessToken(token).build());
        } catch (UsernameAlreadyTakenException ex) {
            logger.warn("Username {} is already taken", signUpDto.getUsername());
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(AuthResponse.builder().error("Имя пользователя уже занято.").build());
        }
    }
}