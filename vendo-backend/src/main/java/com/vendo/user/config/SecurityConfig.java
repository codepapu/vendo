package com.vendo.user.config;

import com.vendo.user.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Spring Security configuration with OAuth2 login support.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    @Autowired(required = false)
    private CustomOAuth2UserService customOAuth2UserService;
    
    @Autowired(required = false)
    private OAuth2SuccessHandler oauth2SuccessHandler;
    
    @Autowired(required = false)
    private ClientRegistrationRepository clientRegistrationRepository;
    
    @Autowired
    private CorsConfigurationSource corsConfigurationSource;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Allow CORS preflight requests (OPTIONS)
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Public endpoints
                .requestMatchers("/api").permitAll() // API info endpoint
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/oauth2/**").permitAll()
                .requestMatchers("/login/oauth2/**").permitAll()
                // OAuth2 endpoints
                .requestMatchers("/oauth2/authorization/**").permitAll()
                // Actuator endpoints (health checks, metrics)
                .requestMatchers("/actuator/**").permitAll()
                // WebSocket endpoints - allow for handshake, authentication handled in WebSocket interceptor
                .requestMatchers("/ws/**", "/ws").permitAll()
                // Swagger/OpenAPI documentation endpoints
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**").permitAll()
                // Public product endpoints (for browsing)
                .requestMatchers("/api/v1/products/**").permitAll()
                .requestMatchers("/api/v1/categories/**").permitAll()
                // Public content endpoints
                .requestMatchers("/api/v1/content/**").permitAll()
                .requestMatchers("/api/v1/blog/**").permitAll()
                .requestMatchers("/api/v1/pages/**").permitAll()
                // Public storefront endpoints
                .requestMatchers("/api/v1/storefronts/**").permitAll()
                .requestMatchers("/api/v1/retailers").permitAll() // GET all retailers (public)
                // All other requests require authentication
                .anyRequest().authenticated()
            );
        
        // Only configure OAuth2 login if ClientRegistrationRepository is available
        if (clientRegistrationRepository != null && customOAuth2UserService != null && oauth2SuccessHandler != null) {
            http.oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService))
                .successHandler(oauth2SuccessHandler)
                .failureUrl("/api/v1/auth/oauth/error")
            );
        }
        
        return http.build();
    }
}
