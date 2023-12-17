package xyz.anomatver.blps.auth.filter;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import xyz.anomatver.blps.auth.repository.JwtTokenProvider;
import xyz.anomatver.blps.auth.service.CustomUserDetailsService;
import xyz.anomatver.blps.vote.controller.VoteController;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;


public class JwtTokenFilter extends OncePerRequestFilter {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(VoteController.class);


    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = jwtTokenProvider.resolveToken(request);

            if (token != null && jwtTokenProvider.validateToken(token)) {
                String username = jwtTokenProvider.getUsername(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            }
        } catch (Exception e) {
           logger.error("Error ocurred when filtering request" + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}