package com.securevault.backend.Service;

import com.securevault.backend.entity.Credential;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.CredentialRepository;
import com.securevault.backend.repository.CredentialShareRepository;
import com.securevault.backend.repository.UserRepository;
import com.securevault.backend.service.AuditLogService;
import com.securevault.backend.service.CredentialService;
import com.securevault.backend.service.EncryptionService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CredentialServiceTest {

    @Mock
    private CredentialRepository credentialRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CredentialShareRepository shareRepository;

    @Mock
    private EncryptionService encryptionService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private CredentialService credentialService;


    // =========================================================
    // 1. ADD CREDENTIAL - SUCCESS
    // =========================================================

    @Test
    void addCredential_success() {

        User user = new User();
        user.setEmail("user1@gmail.com");

        Credential credential = new Credential();
        credential.setTitle("Gmail");
        credential.setUsername("user1@gmail.com");
        credential.setPassword("password123");

        when(userRepository.findByEmailIgnoreCase("user1@gmail.com"))
                .thenReturn(Optional.of(user));

        when(encryptionService.encrypt("password123"))
                .thenReturn("encryptedPassword");

        when(credentialRepository.save(credential))
                .thenReturn(credential);

        when(encryptionService.decrypt("encryptedPassword"))
                .thenReturn("password123");

        Credential result =
                credentialService.addCredential(
                        credential,
                        "user1@gmail.com"
                );

        assertNotNull(result);
        assertEquals("Gmail", result.getTitle());
        assertEquals("user1@gmail.com", result.getUsername());
        assertEquals("password123", result.getPassword());

        verify(userRepository)
                .findByEmailIgnoreCase("user1@gmail.com");

        verify(encryptionService)
                .encrypt("password123");

        verify(credentialRepository)
                .save(credential);

        verify(auditLogService)
                .log(
                        eq("user1@gmail.com"),
                        eq("CREATE_CREDENTIAL"),
                        anyString()
                );
    }


    // =========================================================
    // 2. ADD CREDENTIAL - USER NOT FOUND
    // =========================================================

    @Test
    void addCredential_userNotFound() {

        Credential credential = new Credential();

        credential.setTitle("Gmail");
        credential.setUsername("user1@gmail.com");
        credential.setPassword("password123");

        when(userRepository.findByEmailIgnoreCase("unknown@gmail.com"))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> credentialService.addCredential(
                                credential,
                                "unknown@gmail.com"
                        )
                );

        assertEquals(
                "User not found",
                exception.getMessage()
        );

        verify(credentialRepository, never())
                .save(any());

        verify(encryptionService, never())
                .encrypt(anyString());
    }


    // =========================================================
    // 3. ADD CREDENTIAL - EMPTY PASSWORD
    // =========================================================

    @Test
    void addCredential_emptyPassword() {

        User user = new User();
        user.setEmail("user1@gmail.com");

        Credential credential = new Credential();

        credential.setTitle("Gmail");
        credential.setUsername("user1@gmail.com");
        credential.setPassword("");

        when(userRepository.findByEmailIgnoreCase("user1@gmail.com"))
                .thenReturn(Optional.of(user));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> credentialService.addCredential(
                                credential,
                                "user1@gmail.com"
                        )
                );

        assertEquals(
                "Password cannot be empty",
                exception.getMessage()
        );

        verify(credentialRepository, never())
                .save(any());
    }


    // =========================================================
    // 4. ADD CREDENTIAL - EMPTY TITLE
    // =========================================================

    @Test
    void addCredential_emptyTitle() {

        User user = new User();
        user.setEmail("user1@gmail.com");

        Credential credential = new Credential();

        credential.setTitle("");
        credential.setUsername("user1@gmail.com");
        credential.setPassword("password123");

        when(userRepository.findByEmailIgnoreCase("user1@gmail.com"))
                .thenReturn(Optional.of(user));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> credentialService.addCredential(
                                credential,
                                "user1@gmail.com"
                        )
                );

        assertEquals(
                "Title cannot be empty",
                exception.getMessage()
        );

        verify(credentialRepository, never())
                .save(any());
    }


    // =========================================================
    // 5. GET MY CREDENTIALS - SUCCESS
    // =========================================================

    @Test
    void getMyCredentials_success() {

        User user = new User();
        user.setEmail("user1@gmail.com");

        Credential credential = new Credential();

        credential.setTitle("Gmail");
        credential.setUsername("user1@gmail.com");
        credential.setPassword("encryptedPassword");

        when(userRepository.findByEmailIgnoreCase("user1@gmail.com"))
                .thenReturn(Optional.of(user));

        when(credentialRepository.findByUser(user))
                .thenReturn(List.of(credential));

        when(encryptionService.decrypt("encryptedPassword"))
                .thenReturn("password123");

        List<Credential> result =
                credentialService.getMyCredentials(
                        "user1@gmail.com"
                );

        assertNotNull(result);
        assertEquals(1, result.size());

        assertEquals(
                "Gmail",
                result.get(0).getTitle()
        );

        assertEquals(
                "password123",
                result.get(0).getPassword()
        );

        verify(credentialRepository)
                .findByUser(user);

        verify(encryptionService)
                .decrypt("encryptedPassword");
    }


    // =========================================================
    // 6. GET MY CREDENTIALS - USER NOT FOUND
    // =========================================================

    @Test
    void getMyCredentials_userNotFound() {

        when(userRepository.findByEmailIgnoreCase("unknown@gmail.com"))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> credentialService.getMyCredentials(
                                "unknown@gmail.com"
                        )
                );

        assertEquals(
                "User not found",
                exception.getMessage()
        );

        verify(credentialRepository, never())
                .findByUser(any());
    }


    // =========================================================
    // 7. GET SINGLE CREDENTIAL - NOT FOUND
    // =========================================================

    @Test
    void getCredential_notFound() {

        User user = new User();
        user.setEmail("user1@gmail.com");

        when(userRepository.findByEmailIgnoreCase("user1@gmail.com"))
                .thenReturn(Optional.of(user));

        when(credentialRepository.findById(100L))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> credentialService.getCredential(
                                100L,
                                "user1@gmail.com"
                        )
                );

        assertEquals(
                "Credential not found",
                exception.getMessage()
        );
    }


    // =========================================================
    // 8. UPDATE CREDENTIAL - NOT FOUND
    // =========================================================

    @Test
    void updateCredential_notFound() {

        User user = new User();
        user.setEmail("user1@gmail.com");

        Credential updated = new Credential();

        updated.setTitle("Updated Gmail");
        updated.setUsername("user1@gmail.com");
        updated.setPassword("newPassword");

        when(userRepository.findByEmailIgnoreCase("user1@gmail.com"))
                .thenReturn(Optional.of(user));

        when(credentialRepository.findById(100L))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> credentialService.updateCredential(
                                100L,
                                updated,
                                "user1@gmail.com"
                        )
                );

        assertEquals(
                "Credential not found",
                exception.getMessage()
        );
    }


    // =========================================================
    // 9. DELETE CREDENTIAL - NOT FOUND
    // =========================================================

    @Test
    void deleteCredential_notFound() {

        User user = new User();
        user.setEmail("user1@gmail.com");

        when(userRepository.findByEmailIgnoreCase("user1@gmail.com"))
                .thenReturn(Optional.of(user));

        when(credentialRepository.findById(100L))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> credentialService.deleteCredential(
                                100L,
                                "user1@gmail.com"
                        )
                );

        assertEquals(
                "Credential not found",
                exception.getMessage()
        );

        verify(credentialRepository, never())
                .delete(any());
    }


    // =========================================================
    // 10. ADD CREDENTIAL - EMPTY USERNAME
    // =========================================================

    @Test
    void addCredential_emptyUsername() {

        User user = new User();
        user.setEmail("user1@gmail.com");

        Credential credential = new Credential();

        credential.setTitle("Gmail");
        credential.setUsername("");
        credential.setPassword("password123");

        when(userRepository.findByEmailIgnoreCase("user1@gmail.com"))
                .thenReturn(Optional.of(user));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> credentialService.addCredential(
                                credential,
                                "user1@gmail.com"
                        )
                );

        assertEquals(
                "Username cannot be empty",
                exception.getMessage()
        );

        verify(credentialRepository, never())
                .save(any());
    }
}