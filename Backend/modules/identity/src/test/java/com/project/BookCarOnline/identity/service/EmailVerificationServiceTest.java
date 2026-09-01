package com.project.BookCarOnline.identity.service;

import com.project.BookCarOnline.identity.entity.Account;
import com.project.BookCarOnline.identity.repository.AccountRepository;
import com.project.BookCarOnline.shared.exception.AppException;
import com.project.BookCarOnline.shared.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    RedisTemplate<String, Object> redisTemplate;
    @Mock
    ValueOperations<String, Object> valueOperations;
    @Mock
    AccountRepository accountRepository;
    @Mock
    JavaMailSender mailSender;

    EmailVerificationService service;

    @BeforeEach
    void setUp() {
        service = new EmailVerificationService(redisTemplate, accountRepository, mailSender);
        ReflectionTestUtils.setField(service, "verificationTtl", Duration.ofMinutes(15));
        ReflectionTestUtils.setField(service, "frontendUrl", "https://ridebook.example.com");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void sendVerificationStoresOnlyHashedTokenAndSendsLink() {
        Account account = Account.builder().accountId("account-id").build();

        service.sendVerificationEmail(account, "customer@example.com");

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(
                keyCaptor.capture(),
                eq("account-id"),
                eq(15L),
                eq(TimeUnit.MINUTES));

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        String redisKey = keyCaptor.getValue();
        String emailText = messageCaptor.getValue().getText();
        assertTrue(redisKey.startsWith("email_verification:"));
        assertEquals(83, redisKey.length());
        assertTrue(emailText.contains("https://ridebook.example.com/verify-email?token="));
        assertFalse(emailText.contains(redisKey.substring("email_verification:".length())));
    }

    @Test
    void verifyConsumesTokenAndMarksAccountVerified() throws Exception {
        String rawToken = "single-use-token";
        String redisKey = "email_verification:" + HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        Account account = Account.builder()
                .accountId("account-id")
                .emailVerified(false)
                .build();
        when(valueOperations.getAndDelete(redisKey)).thenReturn("account-id");
        when(accountRepository.findById("account-id")).thenReturn(Optional.of(account));

        service.verifyEmail(rawToken);

        assertTrue(account.isEmailVerified());
        verify(accountRepository).save(account);
    }

    @Test
    void verifyRejectsExpiredOrAlreadyConsumedToken() {
        AppException exception = assertThrows(
                AppException.class,
                () -> service.verifyEmail("expired-token"));

        assertEquals(ErrorCode.EMAIL_VERIFICATION_TOKEN_INVALID, exception.getErrorCode());
    }

    @Test
    void resendInvalidatesPreviouslyIssuedToken() {
        Account account = Account.builder().accountId("account-id").build();
        when(valueOperations.get("email_verification_account:account-id"))
                .thenReturn("email_verification:old-token-hash");

        service.sendVerificationEmail(account, "customer@example.com");

        verify(redisTemplate).delete("email_verification:old-token-hash");
        verify(valueOperations).set(
                eq("email_verification_account:account-id"),
                argThat(value -> value instanceof String key && key.startsWith("email_verification:")),
                eq(15L),
                eq(TimeUnit.MINUTES));
    }
}
