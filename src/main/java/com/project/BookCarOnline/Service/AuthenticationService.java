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

        Account account = accountRepository.findByUserName(request.getUserName()).orElseThrow(()->new AppException(ErrorCode.USER_NOT_EXITED));

        if (!encoder.matches(request.getPassWord(), account.getPassWord())) {
            throw new AppException(ErrorCode.UNAUTHENTACATED);
        }
        if(!account.getRoleNo().getRoleId().equalsIgnoreCase(request.getRoleName()) || !account.getAccountStatus())
            throw  new AppException(ErrorCode.ACCOUNT_NOT_FOUND);

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
        String roleId = account.getRoleNo().getRoleId();
        String userId = null;
        if(roleId.equalsIgnoreCase(PredefinedRole.CUSTOMER.getDescription()))
        {
             customer = customerRepository.findByAccountId(account.getAccountId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));
                userId = customer.getCustomerId();
        }
        else if(roleId.equalsIgnoreCase(PredefinedRole.DRIVER.getDescription()))
        {
             driver = driverRepository.findByAccountId(account.getAccountId()).orElseThrow(
                     () -> new AppException(ErrorCode.DRIVER_NOT_FOUND)
             );
                userId = driver.getDriverId();
        }
        else{
            log.info(account.toString());
            throw new AppException(ErrorCode.ROLE_NOT_EXISTS);
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
        StringJoiner stringJoiner = new StringJoiner(" ");

        Role role = account.getRoleNo(); // đây là một object
        if (role != null) {
            stringJoiner.add("ROLE_" + role.getRoleId().toUpperCase());

        }

        log.info(stringJoiner.toString());
        return stringJoiner.toString();
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
    public void logout(String token) throws ParseException, JOSEException {
        SignedJWT signedJWT = verifyToken(token, true);
        String jit = signedJWT.getJWTClaimsSet().getJWTID();
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        InvalidToken invalidatedToken = InvalidToken.builder()
                .id(jit)
                .expiryTime(expiryTime)
                .reason("Logout")
                .build();
        invalidTokenRepository.save(invalidatedToken);
    }

    public String refresToken(String token) throws ParseException, JOSEException {
        SignedJWT signedJWT = verifyToken(token, true);
        String jit = signedJWT.getJWTClaimsSet().getJWTID();
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        InvalidToken invalidatedToken = InvalidToken.builder()
                .id(jit)
                .expiryTime(expiryTime)
                .reason("Refresh Token")
                .build();
        invalidTokenRepository.save(invalidatedToken);
        Account account = accountRepository.findById(signedJWT.getJWTClaimsSet().getSubject())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String newToken = generateToken(account,VALID_DURATION);
        log.info("New token generated: {}", newToken);
        return newToken;
    }


}
