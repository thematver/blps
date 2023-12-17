package xyz.anomatver.blps.auth;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.anomatver.blps.auth.service.AuthService;

@Component
public class AuthJavaDelegate implements JavaDelegate {

    @Autowired
    AuthService authService;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        authService.camundaAuth(delegateExecution);
    }
}
