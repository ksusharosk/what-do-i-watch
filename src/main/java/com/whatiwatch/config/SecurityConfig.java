package com.whatiwatch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.http.HttpStatus;

/**
 * Spring Security configuration.
 *
 * Guest-friendly: getting recommendations is open to everyone. Saving and
 * personalisation require login via Google OAuth.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            CustomOAuth2UserService customOAuth2UserService) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/health", "/error").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/recommendations").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/movies/search").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .oidcUserService(customOAuth2UserService)
                )
            )   
            // Unauthenticated requests get a 401 instead of a redirect to Google.
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            );

        return http.build();
    }
}
