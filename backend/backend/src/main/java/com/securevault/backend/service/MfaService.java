package com.securevault.backend.service;

import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.springframework.stereotype.Service;

@Service
public class MfaService {

    private final SecretGenerator secretGenerator;
    private final CodeVerifier codeVerifier;

    public MfaService() {

        this.secretGenerator =
                new DefaultSecretGenerator();

        TimeProvider timeProvider =
                new SystemTimeProvider();

        DefaultCodeVerifier verifier =
                new DefaultCodeVerifier(
                        new DefaultCodeGenerator(),
                        timeProvider
                );

        // Allow small clock difference between
        // server and authenticator app.
        verifier.setTimePeriod(30);
        verifier.setAllowedTimePeriodDiscrepancy(2);

        this.codeVerifier = verifier;
    }

    public String generateSecret() {

        return secretGenerator.generate();
    }

    public boolean verifyCode(
            String secret,
            String code) {

        if (secret == null || secret.isBlank()) {
            return false;
        }

        if (code == null || !code.matches("\\d{6}")) {
            return false;
        }

        return codeVerifier.isValidCode(
                secret,
                code
        );
    }

    public String generateOtpUri(
            String secret,
            String email) {

        return "otpauth://totp/SecureVault:"
                + email
                + "?secret="
                + secret
                + "&issuer=SecureVault";
    }
}