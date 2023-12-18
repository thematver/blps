package xyz.anomatver.blps.user.processes;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;
import xyz.anomatver.blps.auth.AuthJavaDelegate;
import xyz.anomatver.blps.auth.service.AuthService;
import xyz.anomatver.blps.auth.service.CustomUserDetailsService;
import xyz.anomatver.blps.user.model.User;
import xyz.anomatver.blps.user.service.UserService;


@Component
public class LinkTelegramProcess extends AuthJavaDelegate {

    private final UserService userService;

    private final CustomUserDetailsService userDetailsService;

    public LinkTelegramProcess(AuthService authService, UserService userService, CustomUserDetailsService userDetailsService) {
        super(authService);
        this.userService = userService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        super.execute(execution);
        String telegramCode = (String) execution.getVariable("telegram_code");
        User user = userDetailsService.getUser();
        userService.link(user, telegramCode);
    }
}