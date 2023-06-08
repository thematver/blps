package xyz.anomatver.blps.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.anomatver.blps.auth.dto.AuthResponse;
import xyz.anomatver.blps.auth.dto.LoginDTO;
import xyz.anomatver.blps.auth.dto.SignUpDTO;
import xyz.anomatver.blps.auth.model.ERole;
import xyz.anomatver.blps.auth.repository.JwtTokenProvider;
import xyz.anomatver.blps.auth.service.CustomUserDetailsService;
import xyz.anomatver.blps.user.model.User;
import xyz.anomatver.blps.user.repository.UserRepository;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Set;

@RestController
@RequestMapping(value = "/api/auth", produces = "application/json")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenRepository;

    @Transactional
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@RequestBody LoginDTO loginDto, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginDto.getUsername(), loginDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate the CSRF JWT token and add it to the response
        String token = jwtTokenRepository.createToken(loginDto.getUsername());

        return ResponseEntity.ok(AuthResponse.builder().accessToken(token).build());
    }

    @Transactional
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignUpDTO signUpDto, HttpServletResponse response) {

        // add check for username exists in a DB
        if (userRepository.existsByUsername(signUpDto.getUsername())) {
            return new ResponseEntity<>("Username is already taken!", HttpStatus.BAD_REQUEST);
        }


        // create user object
        User user = new User();
        user.setUsername(signUpDto.getUsername());

        user.setPassword(passwordEncoder.encode(signUpDto.getPassword()));

        ArrayList<ERole> roles = new ArrayList<>();

        if (signUpDto.getUsername().startsWith("bogdan")) { //потому что богом дан
            roles.add(ERole.ADMIN);
        } else if (signUpDto.getUsername().startsWith("genady")) {
            roles.add(ERole.MODERATOR);
        } else {
            roles.add(ERole.USER);
        }
        user.setRoles(roles);


        userRepository.save(user);
        userDetailsService.addAccount(user.getUsername(), user.getPassword());

        String token = jwtTokenRepository.createToken(user.getUsername());
        return ResponseEntity.ok(AuthResponse.builder().accessToken(token).build());

    }
}
