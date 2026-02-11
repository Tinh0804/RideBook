package com.project.BookCarOnline.Configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {

    @Autowired
    private CustomJwtDecoder jwtDecoder;


    private final String[] AUTH_ENDPOINTS = {"/auth/**","/auth/login","/auth/logout","/auth/refresh-token", "/auth/oauth2/**",
            "/oauth2/**",};
    private final String[] CUSTOMER_ENDPOINTS = {"/customer/register"};
    private final String[] DRIVER_ENDPOINTS = {"/drivers/register"};
    private final String[] SWAGGER_ENDPOINTS = {
            "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**"
    };
    private final String[] SOCKET_ENDPOINTS = {
            "/ws/**", "/topic/**", "/app/**"
    };



    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, AUTH_ENDPOINTS).permitAll()
                        .requestMatchers(CUSTOMER_ENDPOINTS).permitAll()
                        .requestMatchers(SWAGGER_ENDPOINTS).permitAll()
                        .requestMatchers(DRIVER_ENDPOINTS).permitAll()
                        .requestMatchers(SOCKET_ENDPOINTS).permitAll()
                        .anyRequest().authenticated()
                )
                .csrf(AbstractHttpConfigurer::disable);    // tắt CSRF

        http
                .oauth2ResourceServer(oauth2->
                        oauth2.jwt(
                                        jwtConfigurer -> jwtConfigurer.decoder(jwtDecoder)
                                                .jwtAuthenticationConverter(this.jwtAuthenticationConverter())//Convert tên mặt định spring security trong token

                                )
                                .authenticationEntryPoint(new JWTAuthenticationEntryPoint())//được gọi khi chương trình lỗi phân quyền
                )
                .oauth2Login(Customizer.withDefaults());
        ;
        return http.build();
    }


    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter(){

        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter=new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");//mặt định là SCOPE_(do ở phần generate token đã fix ROLE + role.getName() nên không cần set ROLE_)
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("scope"); // Đọc từ scope

        JwtAuthenticationConverter jwtAuthenticationConverter=new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }

}
