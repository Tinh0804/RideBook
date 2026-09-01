package com.project.BookCarOnline.identity.service;

import com.project.BookCarOnline.identity.entity.Account;
import com.project.BookCarOnline.identity.repository.AccountRepository;
import com.project.BookCarOnline.shared.exception.AppException;
import com.project.BookCarOnline.shared.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.HexFormat;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailVerificationService {

    static final String TOKEN_KEY_PREFIX = "email_verification:";
    static final String ACCOUNT_KEY_PREFIX = "email_verification_account:";

    RedisTemplate<String, Object> redisTemplate;
    AccountRepository accountRepository;
    JavaMailSender mailSender;
    SecureRandom secureRandom = new SecureRandom();

    @NonFinal
    @Value("${app.email-verification.ttl:15m}")
    Duration verificationTtl;

    @NonFinal
    @Value("${frontend.url}")
    String frontendUrl;

    public void sendVerificationEmail(Account account, String email) {
        if (account == null || !StringUtils.hasText(account.getAccountId()) || !StringUtils.hasText(email)) {
            throw new AppException(ErrorCode.EMAIL_NOT_FOUND);
        }
        if (account.isEmailVerified()) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_VERIFIED);
        }

        String token = newToken();
        String tokenKey = tokenKey(token);
        String accountKey = ACCOUNT_KEY_PREFIX + account.getAccountId();
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        Object previousTokenKey = values.get(accountKey);
        if (previousTokenKey instanceof String previousKey && StringUtils.hasText(previousKey)) {
            redisTemplate.delete(previousKey);
        }
        values.set(
                tokenKey,
                account.getAccountId(),
                verificationTtl.toMinutes(),
                TimeUnit.MINUTES);
        values.set(
                accountKey,
                tokenKey,
                verificationTtl.toMinutes(),
                TimeUnit.MINUTES);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Verify your RideBook email");
        message.setText("Verify your email by opening this link: "
                + frontendUrl + "/verify-email?token=" + token);
        try {
            mailSender.send(message);
        } catch (MailException exception) {
            redisTemplate.delete(tokenKey);
            redisTemplate.delete(accountKey);
            throw new AppException(ErrorCode.EMAIL_DELIVERY_FAILED);
        }
    }

    public void verifyEmail(String token) {
        if (!StringUtils.hasText(token)) {
            throw new AppException(ErrorCode.EMAIL_VERIFICATION_TOKEN_INVALID);
        }

        Object accountId = redisTemplate.opsForValue().getAndDelete(tokenKey(token));
        if (!(accountId instanceof String id) || !StringUtils.hasText(id)) {
            throw new AppException(ErrorCode.EMAIL_VERIFICATION_TOKEN_INVALID);
        }

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
        account.setEmailVerified(true);
        accountRepository.save(account);
        redisTemplate.delete(ACCOUNT_KEY_PREFIX + account.getAccountId());
    }

    private String newToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String tokenKey(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return TOKEN_KEY_PREFIX + HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
