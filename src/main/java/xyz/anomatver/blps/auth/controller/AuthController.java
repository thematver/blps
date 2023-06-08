package xyz.anomatver.blps.auth.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.anomatver.blps.auth.dto.LoginDTO;
import xyz.anomatver.blps.auth.dto.SignUpDTO;
import xyz.anomatver.blps.auth.model.Role;
import xyz.anomatver.blps.auth.repository.JwtTokenRepository;
import xyz.anomatver.blps.auth.service.CustomUserDetailsService;
import xyz.anomatver.blps.user.model.User;
import xyz.anomatver.blps.auth.repository.RoleRepository;
import xyz.anomatver.blps.user.repository.UserRepository;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.util.Collections;

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
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenRepository jwtTokenRepository;

    @Transactional
    @PostMapping("/login")
    public ResponseEntity<String> authenticateUser(@RequestBody LoginDTO loginDto, HttpServletResponse response){
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginDto.getUsernameOrEmail(), loginDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate the CSRF JWT token and add it to the response
        CsrfToken csrfToken = jwtTokenRepository.generateToken(null);
        jwtTokenRepository.saveToken(csrfToken, null, response);

        return new ResponseEntity<>("User signed-in successfully!", HttpStatus.OK);
    }

    @Transactional
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignUpDTO signUpDto, HttpServletResponse response){

        // add check for username exists in a DB
        if(userRepository.existsByUsername(signUpDto.getUsername())){
            return new ResponseEntity<>("Username is already taken!", HttpStatus.BAD_REQUEST);
        }

        // add check for email exists in DB
        if(userRepository.existsByEmail(signUpDto.getEmail())){
            return new ResponseEntity<>("Email is already taken!", HttpStatus.BAD_REQUEST);
        }

        // create user object
        User user = new User();
        user.setUsername(signUpDto.getUsername());
        user.setEmail(signUpDto.getEmail());
        user.setPassword(passwordEncoder.encode(signUpDto.getPassword()));

        Role roles = roleRepository.findByName("ROLE_ADMIN").get();
        user.setRoles(Collections.singleton(roles));

        userRepository.save(user);
        userDetailsService.addAccount(user.getUsername(), user.getPassword());

        CsrfToken csrfToken = jwtTokenRepository.generateToken(null);
        jwtTokenRepository.saveToken(csrfToken, null, response);

        return new ResponseEntity<>("User registered successfully", HttpStatus.OK);

    }
}
