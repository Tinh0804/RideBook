package com.project.BookCarOnline.Service;

// OAuth2ExchangeService.java
import com.project.BookCarOnline.DTO.Request.ExchangeTokenRequest;
import com.project.BookCarOnline.DTO.Response.AuthenticationResponse;
import com.project.BookCarOnline.DTO.Response.ExchangeTokenResponse;
import com.project.BookCarOnline.Entity.Account;
import com.project.BookCarOnline.Entity.Customer;
import com.project.BookCarOnline.Entity.Enum.PredefinedRole;
import com.project.BookCarOnline.Entity.Enum.Provider;
import com.project.BookCarOnline.Entity.Role;
import com.project.BookCarOnline.Exception.AppException;
import com.project.BookCarOnline.Exception.ErrorCode;
import com.project.BookCarOnline.Mapper.AccountMapper;
import com.project.BookCarOnline.Repository.AccountRepository;
import com.project.BookCarOnline.Repository.CustomerRepository;
import com.project.BookCarOnline.Repository.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.*;

import com.project.BookCarOnline.Entity.Customer;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class OAuth2ExchangeService {
    CustomerRepository customerRepository;
    AccountRepository accountRepository;
    RoleRepository roleRepository;
    AuthenticationService authenticationService;
    ClientRegistrationRepository clientRegistrationRepository;

    AccountMapper accountMapper;

    RestTemplate restTemplate = new RestTemplate();

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    public AuthenticationResponse exchange(ExchangeTokenRequest request) {
        ClientRegistration cr = ((org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository)
                clientRegistrationRepository).findByRegistrationId(request.getProvider());

        if (cr == null) {
            throw new IllegalArgumentException("Unknown provider: " + request.getProvider());
        }

        log.info("Exchange code for token, provider={}, redirectUri={}", request.getProvider(), request.getRedirectUri());

        // 1. Đổi code lấy access_token
        OAuth2AccessTokenResponse tokenResponse = exchangeCodeForToken(cr, request);

        // 2. Lấy userinfo
        OAuth2User user = loadUserInfo(cr, tokenResponse);

        // 3. Lưu/tìm account trong DB
        Account account = exchangeToken(request.getProvider(), user);

        String token = authenticationService.generateToken(account, VALID_DURATION);
        String refreshToken = authenticationService.generateToken(
                accountRepository.findByProviderAndProviderId(request.getProvider(), user.getName())
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED)),REFRESHABLE_DURATION
        );

        return AuthenticationResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .account(accountMapper.toAccountResponse(account))
                .build();
    }

    private OAuth2AccessTokenResponse exchangeCodeForToken(ClientRegistration cr, ExchangeTokenRequest request) {
        String tokenUri = cr.getProviderDetails().getTokenUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", request.getCode());
        params.add("client_id", cr.getClientId());
        params.add("client_secret", cr.getClientSecret());
        params.add("redirect_uri", request.getRedirectUri()); // phải khớp 100% với iOS
        params.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> httpRequest = new HttpEntity<>(params, headers);

        Map<String, Object> response = restTemplate.postForObject(tokenUri, httpRequest, Map.class);

        if (response == null || !response.containsKey("access_token")) {
            throw new AppException(ErrorCode.EXCHANGE_TOKEN_FAIL);
        }

        return OAuth2AccessTokenResponse.withToken((String) response.get("access_token"))
                .tokenType(OAuth2AccessToken.TokenType.BEARER)
                .expiresIn(((Number) response.getOrDefault("expires_in", 3600)).longValue())
                .scopes(cr.getScopes())
                .refreshToken((String) response.get("refresh_token"))
                .additionalParameters(response)
                .build();
    }

    private OAuth2User loadUserInfo(ClientRegistration cr, OAuth2AccessTokenResponse tokenResponse) {
        OAuth2UserRequest userRequest =
                new OAuth2UserRequest(cr, tokenResponse.getAccessToken(), tokenResponse.getAdditionalParameters());

        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        return delegate.loadUser(userRequest);
    }

    private Account exchangeToken(String provider, OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email, name, picture;
        try {
            if (Provider.GOOGLE.name().toLowerCase(Locale.ROOT).equals(provider)) {
                email = (String) attributes.get("email");
                name = (String) attributes.get("name");
                picture = (String) attributes.get("picture");
            } else if (Provider.FACEBOOK.name().toLowerCase(Locale.ROOT).equals(provider)) {
                email = (String) attributes.get("email");
                name = (String) attributes.get("name");
                Map<String, Object> pictureObj = (Map<String, Object>) attributes.get("picture");
                Map<String, Object> data = pictureObj != null ? (Map<String, Object>) pictureObj.get("data") : null;
                picture = data != null ? (String) data.get("url") : null;
            } else {
                throw new IllegalArgumentException("Unsupported provider: " + provider);
            }
        } catch (RuntimeException e) {
            throw new AppException(ErrorCode.EXCHANGE_TOKEN_FAIL);
        }

        return accountRepository.findByProviderAndProviderId(provider, oAuth2User.getName())
                .orElseGet(() -> {
                    return accountRepository.findByUserName(email)
                            .map(existingAccount -> {
                                // Cập nhật thông tin Provider cho tài khoản sẵn có
                                existingAccount.setProvider(provider);
                                existingAccount.setProviderId(oAuth2User.getName());
                                return accountRepository.save(existingAccount);
                            })
                            // BƯỚC 3: Nếu Email cũng chưa có -> Tạo mới hoàn toàn
                            .orElseGet(() -> saveCustomer(email, name, picture, provider, oAuth2User.getName()));
                });


    }

    private Account saveCustomer(String email, String name, String picture,String provider, String providerId) {

        var account = accountRepository.save(
                Account.builder()
                        .userName(email)
                        .provider(provider)
                        .providerId(providerId)  // Lưu 'sub' hoặc 'id'
                        .createdAt(new Date())
                        .roleNo(roleRepository.findByRoleId(PredefinedRole.CUSTOMER.getRoleName())
                                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND)))
                        .accountStatus(true)
                        .build());

        var customer = customerRepository.save(
                Customer.builder()
                        .customerName(name)
                        .avatar(picture)
                        .account(account)
                        .build()
        );
        customerRepository.save(customer);


        return account;
    }
}
