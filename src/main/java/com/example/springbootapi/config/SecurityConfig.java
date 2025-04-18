package com.example.springbootapi.config;

import com.example.springbootapi.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/products").permitAll()
                        .requestMatchers("/api/products/{id}").permitAll()
                        .requestMatchers("/api/products/category/{categoryId}").permitAll()
                        .requestMatchers("/api/categories").permitAll()
                        .requestMatchers("/api/categories/{id}").permitAll()
                        .requestMatchers("/api/currencies").permitAll()
                        .requestMatchers("/favicon.ico", "/error").permitAll()
                        .requestMatchers("/api/auth/**", "/api/public/**").permitAll()
                        .requestMatchers("/api/currencies/{id}").permitAll()
                        .requestMatchers("/api/system-config/**").permitAll()
                        .requestMatchers("/api/orders/callback").permitAll()
                        .requestMatchers("/api/orders/success").permitAll()
                        .requestMatchers("/api/orders/callback/success").permitAll()// Thêm endpoint /api/orders/success vào danh sách công khai
                        .requestMatchers("/api/users/register",
                                "/api/users/register/initiate",
                                "/api/users/register/complete",
                                "/api/users/login",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password").permitAll()
                        .requestMatchers("/api/reviews/add").authenticated()
                        .requestMatchers("/api/reviews/**").permitAll()
                        .requestMatchers("/api/banners").permitAll()
                        .requestMatchers("/api/banners/{id}").permitAll()
                        // Đặt quy tắc cụ thể trước quy tắc chung cho /api/price-history
                        .requestMatchers("/api/statistics/**").hasRole("ADMIN")
                        .requestMatchers("/api/price-history/update").hasRole("ADMIN")
                        .requestMatchers("/api/price-history/**").permitAll()
                        .requestMatchers("/api/users/password").hasRole("CUSTOMER")
                        .requestMatchers("/api/users/me").hasAnyRole("CUSTOMER","ADMIN")
                        .requestMatchers("/api/users/profile/**").hasRole("CUSTOMER")
                        .requestMatchers("/api/users/list").hasRole("ADMIN")
                        .requestMatchers("/api/users/{id}").hasRole("ADMIN")
                        .requestMatchers("/api/cart/**").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/api/orders/{id}", "/api/orders/user/{userId}", "/api/orders/{id}/details").hasAnyRole("CUSTOMER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/orders").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/orders").hasRole("CUSTOMER")
                        .requestMatchers("/api/orders/**").hasRole("ADMIN")
                        .requestMatchers("/api/loyalty-points/add").authenticated()
                        .requestMatchers("/api/loyalty-points/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/banners").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/banners/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/banners/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/categories").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/categories/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/categories/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/products").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/products/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/currencies").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/currencies/{id}").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e.authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"" + authException.getMessage() + "\"}");
                }))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of("*"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}