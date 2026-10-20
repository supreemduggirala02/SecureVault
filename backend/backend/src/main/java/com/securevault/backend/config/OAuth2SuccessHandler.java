package com.securevault.backend.config;

import com.securevault.backend.entity.User;
import com.securevault.backend.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2SuccessHandler
        extends SimpleUrlAuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtService jwtService;

    public OAuth2SuccessHandler(
            UserService userService,
            JwtService jwtService) {

        this.userService = userService;
        this.jwtService = jwtService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oauth2User =
                (OAuth2User) authentication.getPrincipal();

        String email =
                oauth2User.getAttribute("email");

        String name =
                oauth2User.getAttribute("name");

        if (email == null || email.isBlank()) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Google account email not found"
            );

            return;
        }

        User user =
                userService.findOrCreateGoogleUser(
                        email,
                        name
                );

        String token =
                jwtService.generateToken(
                        user.getEmail()
                );

        String encodedToken =
                URLEncoder.encode(
                        token,
                        StandardCharsets.UTF_8
                );

        String encodedEmail =
                URLEncoder.encode(
                        user.getEmail(),
                        StandardCharsets.UTF_8
                );

        String redirectUrl =
                "http://localhost:5176/oauth-success"
                        + "?token="
                        + encodedToken
                        + "&email="
                        + encodedEmail;

        response.sendRedirect(redirectUrl);
    }
}