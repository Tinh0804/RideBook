package com.project.BookCarOnline.Service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.project.BookCarOnline.DTO.Request.AuthenticationRequest;
import com.project.BookCarOnline.DTO.Response.AccountResponse;
import com.project.BookCarOnline.DTO.Response.AuthenticationResponse;
import com.project.BookCarOnline.Entity.*;
import com.project.BookCarOnline.Entity.Enum.PredefinedRole;
import com.project.BookCarOnline.Exception.AppException;
import com.project.BookCarOnline.Exception.ErrorCode;
import com.project.BookCarOnline.Mapper.AccountMapper;
import com.project.BookCarOnline.Repository.CustomerRepository;
import com.project.BookCarOnline.Repository.DriverRepository;
import com.project.BookCarOnline.Repository.InvalidTokenRepository;
import com.project.BookCarOnline.Repository.AccountRepository;
import com.project.BookCarOnline.Utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class AuthenticationService {
    AccountRepository accountRepository;
    InvalidTokenRepository invalidTokenRepository;
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

        String token = generateToken(account, VALID_DURATION);
        String refreshToken = generateToken(account, REFRESHABLE_DURATION);
        AccountResponse accountResponse = accountMapper.toAccountResponse(account);
        accountResponse.setRole(account.getRoleNo());
        accountResponse.setAccountStatus(account.getAccountStatus());

        return AuthenticationResponse.builder()
                .success(true)
                .token(token)
                .refreshToken(refreshToken)
                .account(accountResponse)
                .build();
    }

    String generateToken(Account account,long durationInSeconds) {
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
            log.info(account.toString());
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


    public boolean introspect(String token) throws JOSEException, ParseException {
       try {
           verifyToken(token, false);
           return true;
       } catch (AppException e) {
           return false;
       }
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier=new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT=SignedJWT.parse(token);
        boolean verified= signedJWT.verify(verifier);
        Date expiryTime=(isRefresh) ?
                new Date(signedJWT.getJWTClaimsSet().getIssueTime().toInstant().plus(VALID_DURATION,ChronoUnit.SECONDS).toEpochMilli())//kiểm tra token refresh hết hạn
                :signedJWT.getJWTClaimsSet().getExpirationTime();//kiểm tra tokenVerify hết hạn

        if(!(verified && expiryTime.after(new Date())))
            throw  new AppException(ErrorCode.INVALID_TOKEN);

        if(invalidTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw  new AppException(ErrorCode.UNAUTHENTACATED);
        return  signedJWT;
    }

    public void logout(String refreshToken) throws ParseException, JOSEException {
        String token = SecurityUtils.getCurrentToken().orElseThrow(()->new AppException(ErrorCode.TOKEN_NOT_FOUND));
        SignedJWT signedRefresh = verifyToken(refreshToken, true);
        SignedJWT signedAccess = verifyToken(token, false);


        List<InvalidToken> blacklist = new ArrayList<>();
        blacklist.add(InvalidToken.builder()
                        .id(signedRefresh.getJWTClaimsSet().getJWTID())
                        .expiryTime(signedRefresh.getJWTClaimsSet().getExpirationTime())
                        .reason("Logout Refresh Token")
                .build());

        blacklist.add(InvalidToken.builder()
                        .id(signedAccess.getJWTClaimsSet().getJWTID())
                        .expiryTime(signedAccess.getJWTClaimsSet().getExpirationTime())
                        .reason("Logout Access Token")
                .build());

        invalidTokenRepository.saveAll(blacklist);
    }


    public AuthenticationResponse refreshToken(String refreshToken) throws ParseException, JOSEException {
        String token = SecurityUtils.getCurrentToken().orElseThrow(()->new AppException(ErrorCode.TOKEN_NOT_FOUND));

        SignedJWT signedRefresh = verifyToken(refreshToken, true);
        SignedJWT signedAccess = verifyToken(token, false);

        List<InvalidToken> blacklist = new ArrayList<>();

        blacklist.add(InvalidToken.builder()
                .id(signedAccess.getJWTClaimsSet().getJWTID())
                .expiryTime(signedAccess.getJWTClaimsSet().getExpirationTime())
                .reason("Old Access Token after Refresh")
                .build());

        blacklist.add(InvalidToken.builder()
                .id(signedRefresh.getJWTClaimsSet().getJWTID())
                .expiryTime(signedRefresh.getJWTClaimsSet().getExpirationTime())
                .reason("Old Refresh Token after Refresh")
                .build());

        invalidTokenRepository.saveAll(blacklist);

        Account account = accountRepository.findById(signedRefresh.getJWTClaimsSet().getSubject())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

        return AuthenticationResponse.builder()
                .token(generateToken(account, VALID_DURATION))
                .refreshToken(generateToken(account, REFRESHABLE_DURATION))
                .success(true)
                .build();
    }

    public boolean checkPhoneExist(String phone) {
        return accountRepository.findByUserName(phone).isPresent();
    }

    public void resetPassword(String phone, String newPassword) {
        Account account = accountRepository.findByUserName(phone)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));
        account.setPassWord(encoder.encode(newPassword));
        accountRepository.save(account);
    }

}
