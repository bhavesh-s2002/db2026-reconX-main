package com.dbtraining.reconx.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * ============================================================================
 * TICKET-ADV074 — Spring Security filter chain with RBAC rules
 *
 * Configures stateless JWT authentication and role-based authorization:
 * - Public: /auth/login, /actuator/health, /swagger-ui, /h2, etc.
 * - /v1/recon/**: RECON_ANALYST, ADMIN
 * - /v1/trades GET: VIEWER, TRADER, RECON_ANALYST, ADMIN
 * - /v1/trades POST/PUT/PATCH: TRADER, ADMIN
 * - /v1/trades DELETE: ADMIN
 * ============================================================================
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtAuthenticationFilter jwtFilter) throws Exception {
        http
          .csrf(AbstractHttpConfigurer::disable)
          .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .authorizeHttpRequests(auth -> auth
            .requestMatchers("/auth/login","/actuator/health/**","/actuator/info",
                             "/actuator/prometheus","/swagger-ui.html","/swagger-ui/**",
                             "/v3/api-docs/**","/h2/**").permitAll()
            .requestMatchers("/actuator/**").hasAnyRole("VIEWER","TRADER","RECON_ANALYST","ADMIN")
            .requestMatchers(HttpMethod.GET,    "/v1/trades/**").hasAnyRole("VIEWER","TRADER","RECON_ANALYST","ADMIN")
            .requestMatchers(HttpMethod.POST,   "/v1/trades").hasAnyRole("TRADER","ADMIN")
            .requestMatchers(HttpMethod.PUT,    "/v1/trades/**").hasAnyRole("TRADER","ADMIN")
            .requestMatchers(HttpMethod.PATCH,  "/v1/trades/**").hasAnyRole("TRADER","ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/v1/trades/**").hasRole("ADMIN")
            .requestMatchers("/v1/recon/**").hasAnyRole("RECON_ANALYST","ADMIN")
            .requestMatchers("/v1/audit/**").hasAnyRole("RECON_ANALYST","ADMIN")
            .anyRequest().authenticated())
          .headers(h -> h.frameOptions(f -> f.disable()))
          .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
