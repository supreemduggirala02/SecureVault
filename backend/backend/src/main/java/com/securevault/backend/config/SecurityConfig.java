package com.securevault.backend.config;

import com.securevault.backend.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter) {

        this.jwtAuthenticationFilter =
                jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http)
            throws Exception {

        http
                // =========================
                // CORS
                // =========================
                .cors(Customizer.withDefaults())

                // =========================
                // CSRF
                // =========================
                .csrf(csrf -> csrf.disable())

                // =========================
                // SESSION
                // =========================
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.IF_REQUIRED
                        )
                )

                // =========================
                // AUTHORIZATION
                // =========================
                .authorizeHttpRequests(auth -> auth

                        // OPTIONS
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()

                        // =========================
                        // PUBLIC AUTH APIs
                        // =========================
                        .requestMatchers(
                                "/api/auth/**"
                        ).permitAll()

                        // MFA verification
                        .requestMatchers(
                                "/api/mfa/verify"
                        ).permitAll()

                        // OAuth2
                        .requestMatchers(
                                "/oauth2/**",
                                "/login/**"
                        ).permitAll()

                        // =========================
                        // ADMIN ONLY
                        // =========================
                        .requestMatchers(
                                "/api/admin/**"
                        ).hasRole("ADMINISTRATOR")

                        // =========================
                        // TEAM + ADMIN
                        // =========================
                        .requestMatchers(
                                "/api/team/**"
                        ).hasAnyRole(
                                "TEAM_MEMBER",
                                "ADMINISTRATOR"
                        )

                        // =========================
                        // EVERYTHING ELSE
                        // =========================
                        .anyRequest().authenticated()
                )

                // =========================
                // 401 HANDLER
                // =========================
                .exceptionHandling(exception -> exception

                        .authenticationEntryPoint(
                                authenticationEntryPoint()
                        )

                        .accessDeniedHandler(
                                accessDeniedHandler()
                        )
                )

                // =========================
                // OAUTH2
                // =========================
                .oauth2Login(oauth2 ->
                        oauth2.loginPage(
                                "/oauth2/authorization/google"
                        )
                )

                // =========================
                // JWT FILTER
                // =========================
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    // ==========================================
    // AUTHENTICATION ENTRY POINT
    // ==========================================

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {

        return (request, response, authException) -> {

            response.setStatus(
                    HttpStatus.UNAUTHORIZED.value()
            );

            response.setContentType(
                    MediaType.APPLICATION_JSON_VALUE
            );

            response.getWriter().write(
                    "{\"message\":\"Authentication required\"}"
            );
        };
    }

    // ==========================================
    // ACCESS DENIED HANDLER
    // ==========================================

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {

        return (request, response, accessDeniedException) -> {

            response.setStatus(
                    HttpStatus.FORBIDDEN.value()
            );

            response.setContentType(
                    MediaType.APPLICATION_JSON_VALUE
            );

            response.getWriter().write(
                    "{\"message\":\"Access denied\"}"
            );
        };
    }

    // ==========================================
    // CORS
    // ==========================================

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of(
                        "http://localhost:5173",
                        "http://localhost:5174",
                        "http://localhost:5175",
                        "http://localhost:5176",
                        "http://localhost:5177",
                        "http://localhost:5178",
                        "http://localhost:5179",
                        "http://localhost:5180",
                        "http://localhost:5181",
                        "http://localhost:5182",
                        "http://localhost:5183",
                        "http://localhost:5184",
                        "http://localhost:5185",
                        "http://localhost:5186"
                )
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type",
                        "Accept",
                        "X-Device-Id"
                )
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }

    // ==========================================
    // PASSWORD ENCODER
    // ==========================================

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }
}