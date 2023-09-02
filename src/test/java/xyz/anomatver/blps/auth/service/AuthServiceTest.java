package xyz.anomatver.blps.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import xyz.anomatver.blps.auth.dto.LoginDTO;
import xyz.anomatver.blps.auth.exceptions.UsernameAlreadyTakenException;
import xyz.anomatver.blps.auth.repository.JwtTokenProvider;
import xyz.anomatver.blps.auth.service.AuthService;
import xyz.anomatver.blps.user.repository.UserRepository;

@SpringBootTest
public class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void authenticateUser_ValidCredentials_ReturnsToken() throws Exception {
        LoginDTO sampleLoginDto = new LoginDTO();
        sampleLoginDto.setUsername("sampleUsername");
        sampleLoginDto.setPassword("samplePassword");

        Authentication mockAuth = new UsernamePasswordAuthenticationToken(
                sampleLoginDto.getUsername(), sampleLoginDto.getPassword());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);
        when(jwtTokenRepository.createToken(sampleLoginDto.getUsername())).thenReturn("someToken");

        String resultToken = authService.login(sampleLoginDto);

        assertEquals("someToken", resultToken);
        assertEquals(mockAuth, SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    public void authenticateUser_InvalidCredentials_ThrowsError()  {
        LoginDTO sampleLoginDto = new LoginDTO();
        sampleLoginDto.setUsername("sampleUsername");
        sampleLoginDto.setPassword("samplePassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(BadCredentialsException.class);

        String resultToken = authService.login(sampleLoginDto);

        assertNull(resultToken);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }



}
