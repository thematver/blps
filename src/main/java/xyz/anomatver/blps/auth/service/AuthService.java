package xyz.anomatver.blps.auth.service;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.identity.Group;
import org.jboss.logging.Logger;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import xyz.anomatver.blps.auth.dto.LoginDTO;
import xyz.anomatver.blps.auth.dto.SignUpDTO;
import xyz.anomatver.blps.auth.exceptions.UsernameAlreadyTakenException;
import xyz.anomatver.blps.auth.model.ERole;
import xyz.anomatver.blps.auth.repository.JwtTokenProvider;
import xyz.anomatver.blps.user.model.User;
import xyz.anomatver.blps.user.repository.UserRepository;
import javax.transaction.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;

    private final JwtTokenProvider jwtTokenRepository;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final CustomUserDetailsService customUserDetailsService;

    public AuthService(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenRepository, UserRepository userRepository, PasswordEncoder passwordEncoder, CustomUserDetailsService customUserDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenRepository = jwtTokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Transactional
    public String login(LoginDTO loginDto) {
        try {
            authenticate(loginDto);
            return jwtTokenRepository.createToken(loginDto.getUsername());
        } catch (BadCredentialsException e) {
            Logger.getLogger("Авторизация").warn("Неверные данные при входе: " + loginDto.getUsername());
            return null;
        }
    }


    private void authenticate(LoginDTO loginDto) {
        Authentication authenticationRequest = new UsernamePasswordAuthenticationToken(
                loginDto.getUsername(), loginDto.getPassword());
        Authentication authenticatedResult = authenticationManager.authenticate(authenticationRequest);
        SecurityContextHolder.getContext().setAuthentication(authenticatedResult);
    }


    public void camundaAuth(DelegateExecution delegateExecution) {
        IdentityService identityService = delegateExecution.getProcessEngineServices().getIdentityService();
        String username = identityService.getCurrentAuthentication().getUserId();
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Transactional
    public String register(SignUpDTO signUpDto) {
        if (userRepository.existsByUsername(signUpDto.getUsername())) {
            throw new UsernameAlreadyTakenException("Выбранное имя пользователя уже занято.");
        }

        User user = new User();
        user.setUsername(signUpDto.getUsername());
        user.setPassword(passwordEncoder.encode(signUpDto.getPassword()));

        userRepository.save(user);

        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        IdentityService service = processEngine.getIdentityService();

        if (service.createUserQuery().userId(signUpDto.getUsername()).singleResult() == null) {
            org.camunda.bpm.engine.identity.User camundaUser = service.newUser(signUpDto.getUsername());
            camundaUser.setId(user.getUsername());
            camundaUser.setFirstName(signUpDto.getUsername());
            camundaUser.setPassword(signUpDto.getPassword());
            camundaUser.setLastName("springovich");
            service.saveUser(camundaUser);
        }

        for (ERole role : user.getRoles()) {
            Group group = service.createGroupQuery().groupName(role.toString()).singleResult();
            try {
                service.createMembership(user.getUsername(), group.getId());
            } catch (Exception ignored) {}
        }

        return jwtTokenRepository.createToken(user.getUsername());
    }
}
