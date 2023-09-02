package xyz.anomatver.blps.auth.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import xyz.anomatver.blps.auth.exceptions.UsernameAlreadyTakenException;
import xyz.anomatver.blps.auth.service.AuthService;

@SpringBootTest
public class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private AuthService authService;

    private MockMvc mockMvc;
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    public void authenticateUser_ValidLogin_ReturnsToken() throws Exception {
        when(authService.login(any())).thenReturn("sampleToken");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"username\", \"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"accessToken\":\"sampleToken\"}"));

        verify(authService).login(any());
    }

    @Test
    public void authenticateUser_InvalidLogin_ReturnsError() throws Exception {
        when(authService.login(any())).thenReturn(null);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"username\", \"password\":\"password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"error\":\"Неверный логин или пароль.\"}"));

        verify(authService).login(any());
    }

    @Test
    public void signupUser_Success_ThenReturnToken() throws Exception {
        when(authService.register(any())).thenReturn("token");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"username\", \"password\":\"password\"}"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json("{\"accessToken\":\"token\"}"));

        verify(authService).register(any());
    }

    @Test
    public void signupUser_UsernameTaken_ThenReturnError() throws Exception {
        when(authService.register(any())).thenThrow(UsernameAlreadyTakenException.class);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"username\", \"password\":\"password\"}"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().json("{\"error\":\"Имя пользователя уже занято.\"}"));

        verify(authService).register(any());
    }

}
