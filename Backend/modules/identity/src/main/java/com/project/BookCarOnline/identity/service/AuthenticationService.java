package com.project.BookCarOnline.identity.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.project.BookCarOnline.identity.dto.request.AuthenticationRequest;
import com.project.BookCarOnline.identity.dto.response.AccountResponse;
import com.project.BookCarOnline.identity.dto.response.AuthenticationResponse;
import com.project.BookCarOnline.identity.entity.*;
import com.project.BookCarOnline.identity.entity.enums.PredefinedRole;
import com.project.BookCarOnline.shared.exception.AppException;
import com.project.BookCarOnline.shared.exception.ErrorCode;
import com.project.BookCarOnline.identity.mapper.AccountMapper;
import com.project.BookCarOnline.identity.repository.CustomerRepository;
import com.project.BookCarOnline.identity.repository.DriverRepository;
import com.project.BookCarOnline.identity.repository.AccountRepository;
import com.project.BookCarOnline.shared.security.SecurityUtils;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class AuthenticationService {
    private static final String TOKEN_TYPE_CLAIM = "token_type";

    private enum TokenType {
        ACCESS,
        REFRESH
    }

    AccountRepository accountRepository;
    RedisTemplate<String, Object> redisTemplate;
    CustomerRepository customerRepository;
    DriverRepository driverRepository;
    AccountMapper accountMapper;

    PasswordEncoder encoder;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected  String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    public AuthenticationResponse authenticate(AuthenticationRequest request) {

        Account account = accountRepository.findByUserName(request.getUserName()).orElseThrow(()->new AppException(ErrorCode.USERNAME_OR_PASSWORD_INVALID));

        if (!encoder.matches(request.getPassWord(), account.getPassWord())) {
            throw new AppException(ErrorCode.USERNAME_OR_PASSWORD_INVALID);
        }


        if(!account.getRoleNo().getRoleName().getRoleName().equalsIgnoreCase(request.getRoleName()))
            throw  new AppException(ErrorCode.ROLE_NOT_FOUND);
        if(!account.getAccountStatus())
            throw  new AppException(ErrorCode.ACCOUNT_DISABLED);

        String token = generateAccessToken(account);
        String refreshToken = generateRefreshToken(account);
        AccountResponse accountResponse = accountMapper.toAccountResponse(account);
        accountResponse.setAccountStatus(account.getAccountStatus());

        return AuthenticationResponse.builder()
                .success(true)
                .token(token)
                .refreshToken(refreshToken)
                .account(accountResponse)
                .build();
    }

    String generateAccessToken(Account account) {
        return generateToken(account, VALID_DURATION, TokenType.ACCESS);
    }

    String generateRefreshToken(Account account) {
        return generateToken(account, REFRESHABLE_DURATION, TokenType.REFRESH);
    }

    private String generateToken(Account account, long durationInSeconds, TokenType tokenType) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        Customer customer = null;
        Driver driver = null;
        PredefinedRole roleName = account.getRoleNo().getRoleName();
        String userId = null;
        if(roleName.equals(PredefinedRole.CUSTOMER))
        {
             customer = customerRepository.findByAccountId(account.getAccountId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));
                userId = customer.getCustomerId();
        }
        else if(roleName.equals(PredefinedRole.DRIVER))
        {
             driver = driverRepository.findByAccountId(account.getAccountId()).orElseThrow(
                     () -> new AppException(ErrorCode.DRIVER_NOT_FOUND)
             );
                userId = driver.getDriverId();
        }
        else{
            customer = customerRepository.findByAccountId(account.getAccountId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));
            userId = customer.getCustomerId();
        }

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(account.getAccountId())
                .issuer("BookCarOnline")
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plus(durationInSeconds, ChronoUnit.SECONDS)))
                .claim("scope",buildScope(account))
                .claim("profile_id",userId)
                .claim(TOKEN_TYPE_CLAIM, tokenType.name())
                .jwtID(UUID.randomUUID().toString())
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject=new JWSObject(header,payload);
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }

    }
    private String buildScope(Account account) {
        Role role = account.getRoleNo();
        if (role == null || role.getRoleName() == null) {
            return "";
        }
        return role.getRoleName().getRoleName().toUpperCase();   // Chỉ "DRIVER" hoặc "CUSTOMER"
    }


    public boolean introspect(String token) throws JOSEException {
       try {
           verifyToken(token, TokenType.ACCESS);
           return true;
       } catch (AppException e) {
           return false;
       }
    }

    private JWTClaimsSet verifyToken(String token, TokenType expectedType) throws JOSEException {
        if (!StringUtils.hasText(token)) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes(StandardCharsets.UTF_8));
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            if (!JWSAlgorithm.HS512.equals(signedJWT.getHeader().getAlgorithm())) {
                throw new AppException(ErrorCode.INVALID_TOKEN);
            }

            boolean verified = signedJWT.verify(verifier);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            Date expiryTime = claims.getExpirationTime();

            if (!(verified
                    && expiryTime != null
                    && expiryTime.after(new Date())
                    && "BookCarOnline".equals(claims.getIssuer())
                    && StringUtils.hasText(claims.getSubject())
                    && StringUtils.hasText(claims.getJWTID())
                    && expectedType.name().equals(claims.getStringClaim(TOKEN_TYPE_CLAIM)))) {
                throw new AppException(ErrorCode.INVALID_TOKEN);
            }

            if (Boolean.TRUE.equals(redisTemplate.hasKey("invalid_token:" + claims.getJWTID()))) {
                throw new AppException(ErrorCode.TOKEN_BLACKLISTED);
            }
            return claims;
        } catch (ParseException | JOSEException e) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
    }

    public void logout(String refreshToken) throws JOSEException {
        String token = SecurityUtils.getCurrentToken().orElseThrow(()->new AppException(ErrorCode.TOKEN_NOT_FOUND));
        JWTClaimsSet refreshClaims = verifyToken(refreshToken, TokenType.REFRESH);
        JWTClaimsSet accessClaims = verifyToken(token, TokenType.ACCESS);


        long refreshExpiry = refreshClaims.getExpirationTime().getTime() - System.currentTimeMillis();
        if (refreshExpiry > 0) {
            redisTemplate.opsForValue().set("invalid_token:" + refreshClaims.getJWTID(), "Logout Refresh Token", refreshExpiry, TimeUnit.MILLISECONDS);
        }

        long accessExpiry = accessClaims.getExpirationTime().getTime() - System.currentTimeMillis();
        if (accessExpiry > 0) {
            redisTemplate.opsForValue().set("invalid_token:" + accessClaims.getJWTID(), "Logout Access Token", accessExpiry, TimeUnit.MILLISECONDS);
        }
    }


    public AuthenticationResponse refreshToken(String refreshToken) throws JOSEException {
        JWTClaimsSet refreshClaims = verifyToken(refreshToken, TokenType.REFRESH);

        long refreshExpiry = refreshClaims.getExpirationTime().getTime() - System.currentTimeMillis();
        if (refreshExpiry > 0) {
            redisTemplate.opsForValue().set("invalid_token:" + refreshClaims.getJWTID(), "Old Refresh Token after Refresh", refreshExpiry, TimeUnit.MILLISECONDS);
        }

        Account account = accountRepository.findById(refreshClaims.getSubject())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

        return AuthenticationResponse.builder()
                .token(generateAccessToken(account))
                .refreshToken(generateRefreshToken(account))
                .success(true)
                .build();
    }

    public boolean checkPhoneExist(String phone) {
        return accountRepository.findByUserName(phone).isPresent();
    }

    public void resetPassword(String firebaseToken, String newPassword) {
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(firebaseToken);
            String phoneNumber = decodedToken.getClaims().get("phone_number").toString();
            
            // Firebase trả về định dạng quốc tế +84..., chuyển về 0...
            if (phoneNumber.startsWith("+84")) {
                phoneNumber = "0" + phoneNumber.substring(3);
            }
            
            Account account = accountRepository.findByUserName(phoneNumber)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));
            
            account.setPassWord(encoder.encode(newPassword));
            accountRepository.save(account);
        } catch (Exception e) {
            log.error("Lỗi xác thực Firebase OTP: ", e);
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
    }

    public void changePassword(String oldPassword, String newPassword) {
        String phone = SecurityUtils.getCurrentAccountId()
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

        Account account = accountRepository.findByUserName(phone)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

        if (!encoder.matches(oldPassword, account.getPassWord())) {
            throw new AppException(ErrorCode.PASSWORD_INVALID);
        }

        account.setPassWord(encoder.encode(newPassword));
        accountRepository.save(account);
    }

}
