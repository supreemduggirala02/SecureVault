package com.securevault.backend.service;

import com.securevault.backend.entity.Role;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // =========================
    // REGISTER USER
    // =========================

    public User registerUser(User user) {

        if (user == null) {
            throw new RuntimeException("User data is required");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            throw new RuntimeException("Name is required");
        }

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new RuntimeException("Email is required");
        }

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new RuntimeException("Password is required");
        }

        if (user.getPassword().length() < 8) {
            throw new RuntimeException(
                    "Password must contain at least 8 characters"
            );
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Never trust role coming from frontend.
        // Every normal registration starts as USER.
        user.setRole(Role.USER);

        String hashedPassword =
                passwordEncoder.encode(user.getPassword());

        user.setPassword(hashedPassword);

        // Security defaults
        user.setMfaEnabled(false);
        user.setMfaSecret(null);
        user.setResetToken(null);
        user.setResetTokenExpiry(null);

        return userRepository.save(user);
    }

    // =========================
    // LOGIN
    // =========================

    public User loginUser(String email, String password) {

        if (email == null
                || email.isBlank()
                || password == null
                || password.isBlank()) {

            throw new RuntimeException(
                    "Invalid email or password"
            );
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Invalid email or password"
                        )
                );

        if (!passwordEncoder.matches(
                password,
                user.getPassword())) {

            throw new RuntimeException(
                    "Invalid email or password"
            );
        }

        // Safety for old database records
        // that may not have a role yet.
        if (user.getRole() == null) {
            user.setRole(Role.USER);
            userRepository.save(user);
        }

        return user;
    }

    // =========================
    // GET USER
    // =========================

    public User getUserByEmail(String email) {

        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email is required");
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );
    }

    // =========================
    // SAVE USER
    // =========================

    public User saveUser(User user) {

        if (user == null) {
            throw new RuntimeException("User data is required");
        }

        return userRepository.save(user);
    }

    // =========================
    // PASSWORD RESET TOKEN
    // =========================

    public User getUserByResetToken(String resetToken) {

        if (resetToken == null || resetToken.isBlank()) {
            throw new RuntimeException("Invalid reset token");
        }

        return userRepository.findAll()
                .stream()
                .filter(user ->
                        resetToken.equals(user.getResetToken()))
                .findFirst()
                .orElse(null);
    }

    // =========================
    // UPDATE PASSWORD
    // =========================

    public void updatePassword(
            User user,
            String newPassword) {

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (newPassword == null || newPassword.isBlank()) {
            throw new RuntimeException(
                    "New password is required"
            );
        }

        if (newPassword.length() < 8) {
            throw new RuntimeException(
                    "Password must contain at least 8 characters"
            );
        }

        String hashedPassword =
                passwordEncoder.encode(newPassword);

        user.setPassword(hashedPassword);

        userRepository.save(user);
    }

    // =========================
    // GOOGLE USER
    // =========================

    public User findOrCreateGoogleUser(
            String email,
            String name) {

        User existingUser =
                userRepository
                        .findByEmailIgnoreCase(email)
                        .orElse(null);

        if (existingUser != null) {

            // Safety for existing users
            if (existingUser.getRole() == null) {
                existingUser.setRole(Role.USER);
                userRepository.save(existingUser);
            }

            return existingUser;
        }

        User newUser = new User();

        newUser.setName(
                name == null || name.isBlank()
                        ? "Google User"
                        : name
        );

        newUser.setEmail(email);

        String randomPassword =
                UUID.randomUUID().toString();

        newUser.setPassword(
                passwordEncoder.encode(randomPassword)
        );

        newUser.setMfaEnabled(false);

        // Google users also start as USER.
        newUser.setRole(Role.USER);

        return userRepository.save(newUser);
    }

    // =========================
    // CREATE PASSWORD RESET TOKEN
    // =========================

    public String createPasswordResetToken(String email) {

        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email is required");
        }

        User user =
                userRepository
                        .findByEmailIgnoreCase(email)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No account found with this email"
                                )
                        );

        String token =
                UUID.randomUUID().toString();

        LocalDateTime expiry =
                LocalDateTime.now().plusMinutes(15);

        user.setResetToken(token);
        user.setResetTokenExpiry(expiry);

        userRepository.save(user);

        return token;
    }

    // =========================
    // RESET PASSWORD
    // =========================

    public void resetPassword(
            String token,
            String newPassword) {

        if (token == null || token.isBlank()) {
            throw new RuntimeException(
                    "Invalid reset token"
            );
        }

        User user =
                getUserByResetToken(token);

        if (user == null) {
            throw new RuntimeException(
                    "Invalid reset token"
            );
        }

        if (user.getResetTokenExpiry() == null
                || LocalDateTime.now()
                .isAfter(user.getResetTokenExpiry())) {

            user.setResetToken(null);
            user.setResetTokenExpiry(null);

            userRepository.save(user);

            throw new RuntimeException(
                    "Reset token has expired"
            );
        }

        updatePassword(user, newPassword);

        user.setResetToken(null);
        user.setResetTokenExpiry(null);

        userRepository.save(user);
    }
}