package com.project.BookCarOnline.Configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private CustomJwtDecoder jwtDecoder;


    private final String[] AUTH_ENDPOINTS = {"/auth/**","/auth/login","/auth/logout","/auth/refresh-token", "/auth/oauth2/**",
            "/oauth2/**",};
    private final String[] CUSTOMER_ENDPOINTS = {"/customers/register"};
    private final String[] DRIVER_ENDPOINTS = {"/drivers/register"};
    private final String[] SWAGGER_ENDPOINTS = {
            "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**"
    };
    private final String[] SOCKET_ENDPOINTS = {
            "/ws/**", "/topic/**", "/app/**"
    };
    private  final String[] PAYMENT_ENDPOINTS={
            "/payments/momo/return","/payments/vnpay/return","/payments/momo/notify","/payments/vnpay/notify",
            "/payments/momo/callback","/payments/vnpay/callback"
    };
    private final String[] ADMIN_ENDPOINTS = {"/admin/**"};



    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())
                        .xssProtection(Customizer.withDefaults())
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(AUTH_ENDPOINTS).permitAll()
                        .requestMatchers(CUSTOMER_ENDPOINTS).permitAll()
                        .requestMatchers(SWAGGER_ENDPOINTS).permitAll()
                        .requestMatchers(DRIVER_ENDPOINTS).permitAll()
                        .requestMatchers(SOCKET_ENDPOINTS).permitAll()
                        .requestMatchers(PAYMENT_ENDPOINTS).permitAll()
                        .requestMatchers(ADMIN_ENDPOINTS).hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .csrf(AbstractHttpConfigurer::disable);    // tắt CSRF

        http
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new JWTAuthenticationEntryPoint()))
                .oauth2ResourceServer(oauth2->
                        oauth2.jwt(
                                        jwtConfigurer -> jwtConfigurer.decoder(jwtDecoder)
                                                .jwtAuthenticationConverter(this.jwtAuthenticationConverter())//Convert tên mặt định spring security trong token

                                )
                                .authenticationEntryPoint(new JWTAuthenticationEntryPoint())//được gọi khi chương trình lỗi phân quyền
                )
                .oauth2Login(Customizer.withDefaults());
        return http.build();
    }


    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter(){

        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter=new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");//mặt định là SCOPE_(do ở phần generate token đã fix ROLE + role.getName() nên không cần set ROLE_)
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("scope"); // Đọc từ scope

        JwtAuthenticationConverter jwtAuthenticationConverter=new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Safari rất khắt khe với CORS khi có Credentials. Việc khai báo đích danh Origin sẽ an toàn hơn dùng Pattern "*".
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000", 
            "http://127.0.0.1:3000", 
            "http://localhost:5173", 
            "http://127.0.0.1:5173",
            "https://ridebook.tinhlelaptrinh.id.vn" // Thêm domain production
        ));
        configuration.setAllowedOriginPatterns(Arrays.asList("*")); // Giữ lại pattern làm fallback
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "x-auth-token"));
        configuration.setExposedHeaders(Arrays.asList("x-auth-token"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
