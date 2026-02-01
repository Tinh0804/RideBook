package com.project.BookCarOnline.Service;

// OAuth2ExchangeService.java
import com.project.BookCarOnline.DTO.Request.ExchangeTokenRequest;
import com.project.BookCarOnline.DTO.Response.AuthenticationResponse;
import com.project.BookCarOnline.DTO.Response.ExchangeTokenResponse;
import com.project.BookCarOnline.Entity.Account;
import com.project.BookCarOnline.Entity.Customer;
import com.project.BookCarOnline.Entity.Enum.PredefinedRole;
import com.project.BookCarOnline.Entity.Role;
import com.project.BookCarOnline.Exception.AppException;
import com.project.BookCarOnline.Exception.ErrorCode;
import com.project.BookCarOnline.Repository.AccountRepository;
import com.project.BookCarOnline.Repository.CustomerRepository;
import com.project.BookCarOnline.Repository.RoleRepository;
import lombok.RequiredArgsConstructor;
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
import java.util.Map;

////import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
//import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
//import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
//import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.stereotype.Service;
//
//import java.util.*;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class OAuth2ExchangeService {
//    CustomerRepository customerRepository;
//    AccountRepository accountRepository;
//    RoleRepository roleRepository;
//    AuthenticationService authenticationService;
//    private final ClientRegistrationRepository clientRegistrationRepository;
//
//    private final RestClientAuthorizationCodeTokenResponseClient tokenClient =
//            new RestClientAuthorizationCodeTokenResponseClient();
//
//    private final DefaultOAuth2UserService oAuth2UserService = new DefaultOAuth2UserService();
//
//    public AuthenticationResponse exchange(ExchangeTokenRequest request) {
//        ClientRegistration cr = ((org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository)
//                clientRegistrationRepository).findByRegistrationId(request.getRegistrationId());
//
//        if (cr == null) {
//            throw new IllegalArgumentException("Unknown provider: " + request.getRegistrationId());
//        }
//        log.info("Exchange with redirectUri = " + request.getRedirectUri());
//
//        // Tạo AuthorizationRequest “ảo” để khớp với code + redirectUri
//        OAuth2AuthorizationRequest authRequest = OAuth2AuthorizationRequest.authorizationCode()
//                .authorizationUri(cr.getProviderDetails().getAuthorizationUri())
//                .clientId(cr.getClientId())
//                .redirectUri(request.getRedirectUri()) // phải KHỚP 100% với lúc xin code
//                .scopes(cr.getScopes())
//                .state("state") // không dùng state trong flow mobile này
//                .build();
//
//        OAuth2AuthorizationResponse authResponse = OAuth2AuthorizationResponse.success(request.getCode())
//                .redirectUri(request.getRedirectUri())
//                .state("state")
//                .build();
//
//        OAuth2AuthorizationExchange exchange = new OAuth2AuthorizationExchange(authRequest, authResponse);
//
//        // Đổi code lấy token
//        OAuth2AuthorizationCodeGrantRequest grantRequest = new OAuth2AuthorizationCodeGrantRequest(cr, exchange);
//        OAuth2AccessTokenResponse tokenResponse = tokenClient.getTokenResponse(grantRequest);
//
//        // Lấy userinfo (Google có thể kèm id_token)
//        OAuth2User oAuth2User = oAuth2UserService.loadUser(
//                new OAuth2UserRequest(cr, tokenResponse.getAccessToken(), tokenResponse.getAdditionalParameters())
//        );
//        OAuth2ExchangeService.ExchangeResult result = new ExchangeResult(tokenResponse, oAuth2User);
//        OAuth2AccessTokenResponse tokenAuth = result.token();
//        OAuth2User user = result.user();
//
//        String token = exchangeToken(request.getRegistrationId(), user);
//        return AuthenticationResponse.builder()
//                .authenticated(true)
//                .token(token)
//                .build();
//
//    }
//
//   private String exchangeToken(String registrationId, OAuth2User oAuth2User){
//       Map<String, Object> attributes = oAuth2User.getAttributes();
//
//       String userId;
//       String email;
//       String name;
//       String picture;
//
//       try{
//           if ("google".equals(registrationId)) {
//               // Google trả về "sub" làm id duy nhất
//               userId = (String) attributes.get("sub");
//               email = (String) attributes.get("email");
//               name = (String) attributes.get("name");
//               picture = (String) attributes.get("picture");
//           } else if ("facebook".equals(registrationId)) {
//               // Facebook thường có "id" làm userId
//               userId = (String) attributes.get("id");
//               email = (String) attributes.get("email");
//               name = (String) attributes.get("name");
//
//               // Facebook avatar thường nằm trong "picture.data.url"
//               Map<String, Object> pictureObj = (Map<String, Object>) attributes.get("picture");
//               Map<String, Object> data = pictureObj != null ? (Map<String, Object>) pictureObj.get("data") : null;
//               picture = data != null ? (String) data.get("url") : null;
//           } else {
//               throw new IllegalArgumentException("Unsupported provider: " + registrationId);
//           }
//       } catch (RuntimeException e) {
//           throw new AppException(ErrorCode.EXCHANGE_TOKEN_FAIL);
//       }
//
//       Set<Role> roles = new HashSet<>();
//       roles.add(Role.builder()
//                       .roleName(PredefinedRole.CUSTOMER.name())
//               .build());
//       Account account = accountRepository.findByUserName(email).orElseGet(
//               ()-> saveCustomer(email,name,picture)
//       );
//       String token = authenticationService.generateToken(account);
//
//      return token;
//
//   }
//
//
//   private Account saveCustomer(String email, String name, String picture){
//       var account = accountRepository.save(
//               Account.builder()
//                       .userName(email)
//                       .createdAt(new Date())
//                       .roleNo(roleRepository.findByRoleId(PredefinedRole.CUSTOMER.getDescription()).orElseThrow(
//                               ()->new AppException(ErrorCode.ROLE_NOT_FOUND)
//                       ))
//                       .accountStatus(true)
//                       .build()
//       );
//
//       customerRepository.save(
//               Customer.builder()
//                       .customerName(name)
//                       .accountNo(account)
//                       .avatar(picture)
//                       .build()
//       );
//
//
//       return account;
//   }
//
//
//    public record ExchangeResult(OAuth2AccessTokenResponse token, OAuth2User user) {}
//}
@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2ExchangeService {
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final AuthenticationService authenticationService;
    private final ClientRegistrationRepository clientRegistrationRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    public AuthenticationResponse exchange(ExchangeTokenRequest request) {
        ClientRegistration cr = ((org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository)
                clientRegistrationRepository).findByRegistrationId(request.getRegistrationId());

        if (cr == null) {
            throw new IllegalArgumentException("Unknown provider: " + request.getRegistrationId());
        }

        log.info("Exchange code for token, provider={}, redirectUri={}", request.getRegistrationId(), request.getRedirectUri());

        // 1. Đổi code lấy access_token
        OAuth2AccessTokenResponse tokenResponse = exchangeCodeForToken(cr, request);

        // 2. Lấy userinfo
        OAuth2User user = loadUserInfo(cr, tokenResponse);

        // 3. Lưu/tìm account trong DB
        String token = exchangeToken(request.getRegistrationId(), user);

        return AuthenticationResponse.builder()
                .token(token)
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

    private String exchangeToken(String registrationId, OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email, name, picture;
        try {
            if ("google".equals(registrationId)) {
                email = (String) attributes.get("email");
                name = (String) attributes.get("name");
                picture = (String) attributes.get("picture");
            } else if ("facebook".equals(registrationId)) {
                email = (String) attributes.get("email");
                name = (String) attributes.get("name");
                Map<String, Object> pictureObj = (Map<String, Object>) attributes.get("picture");
                Map<String, Object> data = pictureObj != null ? (Map<String, Object>) pictureObj.get("data") : null;
                picture = data != null ? (String) data.get("url") : null;
            } else {
                throw new IllegalArgumentException("Unsupported provider: " + registrationId);
            }
        } catch (RuntimeException e) {
            throw new AppException(ErrorCode.EXCHANGE_TOKEN_FAIL);
        }

        Account account = accountRepository.findByUserName(email)
                .orElseGet(() -> saveCustomer(email, name, picture));

        return authenticationService.generateToken(account,VALID_DURATION);
    }

    private Account saveCustomer(String email, String name, String picture) {

        var account = accountRepository.save(
                Account.builder()
                        .userName(email)
                        .createdAt(new Date())
                        .roleNo(roleRepository.findByRoleId(PredefinedRole.CUSTOMER.getDescription())
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


        return account;
    }
}
