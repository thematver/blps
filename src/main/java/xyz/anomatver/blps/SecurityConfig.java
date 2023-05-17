package xyz.anomatver.blps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder authenticationMgr) throws Exception {
        authenticationMgr.inMemoryAuthentication()
                .withUser("user").password("{noop}password").authorities("ROLE_USER")
                .and()
                .withUser("moderator").password("{noop}password").authorities("ROLE_USER","ROLE_MODERATOR");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()

                .authorizeRequests()
                .antMatchers("/api/moderators/**").access("hasRole('ROLE_MODERATOR')")
                .antMatchers("/api/users/**").access("hasRole('ROLE_USER')")
                .antMatchers("/api/reviews/**").access("hasRole('ROLE_USER')")
                .and()
                .httpBasic()
                .and()
                .csrf().disable();

    }
}