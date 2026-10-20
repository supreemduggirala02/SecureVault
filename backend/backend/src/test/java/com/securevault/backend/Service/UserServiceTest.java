package com.securevault.backend.Service;

import com.securevault.backend.entity.User;
import com.securevault.backend.repository.UserRepository;
import com.securevault.backend.service.UserService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;


    // 1. Register - Success
    @Test
    void registerUser_success() {

        User user = new User();
        user.setName("User One");
        user.setEmail("user1@gmail.com");
        user.setPassword("password123");

        when(userRepository.existsByEmail("user1@gmail.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("hashedPassword");

        when(userRepository.save(user))
                .thenReturn(user);

        User result = userService.registerUser(user);

        assertNotNull(result);
        assertEquals("user1@gmail.com", result.getEmail());
        assertEquals("hashedPassword", result.getPassword());

        verify(userRepository)
                .existsByEmail("user1@gmail.com");

        verify(passwordEncoder)
                .encode("password123");

        verify(userRepository)
                .save(user);
    }


    // 2. Register - Duplicate Email
    @Test
    void registerUser_duplicateEmail_throwsException() {

        User user = new User();
        user.setEmail("user1@gmail.com");
        user.setPassword("password123");

        when(userRepository.existsByEmail("user1@gmail.com"))
                .thenReturn(true);

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> userService.registerUser(user)
                );

        assertEquals(
                "Email already exists",
                exception.getMessage()
        );

        verify(userRepository)
                .existsByEmail("user1@gmail.com");

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(userRepository, never())
                .save(any(User.class));
    }


    // 3. Login - Success
    @Test
    void loginUser_success() {

        User user = new User();
        user.setEmail("user1@gmail.com");
        user.setPassword("hashedPassword");

        when(userRepository.findByEmail("user1@gmail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "password123",
                "hashedPassword"))
                .thenReturn(true);

        User result =
                userService.loginUser(
                        "user1@gmail.com",
                        "password123"
                );

        assertNotNull(result);

        assertEquals(
                "user1@gmail.com",
                result.getEmail()
        );

        verify(userRepository)
                .findByEmail("user1@gmail.com");

        verify(passwordEncoder)
                .matches(
                        "password123",
                        "hashedPassword"
                );
    }


    // 4. Login - Wrong Password
    @Test
    void loginUser_wrongPassword_throwsException() {

        User user = new User();
        user.setEmail("user1@gmail.com");
        user.setPassword("hashedPassword");

        when(userRepository.findByEmail("user1@gmail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "wrongPassword",
                "hashedPassword"))
                .thenReturn(false);

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> userService.loginUser(
                                "user1@gmail.com",
                                "wrongPassword"
                        )
                );

        assertEquals(
                "Invalid email or password",
                exception.getMessage()
        );
    }


    // 5. Login - Unknown Email
    @Test
    void loginUser_unknownEmail_throwsException() {

        when(userRepository.findByEmail("unknown@gmail.com"))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> userService.loginUser(
                                "unknown@gmail.com",
                                "password123"
                        )
                );

        assertEquals(
                "Invalid email or password",
                exception.getMessage()
        );

        verify(passwordEncoder, never())
                .matches(anyString(), anyString());
    }


    // 6. Get User - Success
    @Test
    void getUserByEmail_success() {

        User user = new User();

        user.setEmail("user1@gmail.com");
        user.setName("User One");

        when(userRepository.findByEmail("user1@gmail.com"))
                .thenReturn(Optional.of(user));

        User result =
                userService.getUserByEmail(
                        "user1@gmail.com"
                );

        assertNotNull(result);

        assertEquals(
                "user1@gmail.com",
                result.getEmail()
        );

        assertEquals(
                "User One",
                result.getName()
        );
    }


    // 7. Get User - Not Found
    @Test
    void getUserByEmail_notFound_throwsException() {

        when(userRepository.findByEmail("unknown@gmail.com"))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> userService.getUserByEmail(
                                "unknown@gmail.com"
                        )
                );

        assertEquals(
                "User not found",
                exception.getMessage()
        );
    }
}