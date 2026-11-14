
package com.roadReady.roadready_backend.config;

import com.roadReady.roadready_backend.security.JwtAuthenticationFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecureConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecureConfig(
            UserDetailsService userDetailsService,
            JwtAuthenticationFilter jwtAuthFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();

        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .cors(cors ->
                        cors.configurationSource(corsConfigurationSource())
                )
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authenticationProvider(authenticationProvider())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .authorizeHttpRequests(auth -> auth

                        // Public authentication endpoints
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/auth/login"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()

//                        // Swagger
//                        .requestMatchers(
//                                "/v3/api-docs/**",
//                                "/swagger-ui/**",
//                                "/swagger-ui.html"
//                        ).permitAll()

                        // Public registration
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/customers/add",
                                "/api/v1/rental-agents/register",
                                "/api/v1/users"
                        ).permitAll()

                        // Admin management
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/admin/add"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/admin/all",
                                "/api/v1/admin/users/**",
                                "/api/v1/admin/*"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/admin/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/v1/admin/**"
                        ).hasRole("ADMIN")

                        // Customer management
                        // ==========================================
                        // CUSTOMER MANAGEMENT (ORDER MATTERS!)
                        // ==========================================

                        // 1. First, allow Customers to access their OWN profile
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/customers/me"
                        ).hasRole("CUSTOMER")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/customers/me"
                        ).hasRole("CUSTOMER")

                        // 2. Then, restrict the rest of the customer endpoints to Admin
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/customers/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/v1/customers/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/customers/**"
                        ).hasAnyRole("CUSTOMER", "ADMIN")

                        // Rental agent management
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/rental-agents/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/v1/rental-agents/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/rental-agents/**"
                        ).hasAnyRole("RENTAL_AGENT", "ADMIN")

                        // User account management
                        .requestMatchers(
                                "/api/v1/users",
                                "/api/v1/users/**"
                        ).hasRole("ADMIN")
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/users"
                        ).permitAll() // Allow signup

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/v1/users/**"
                        ).hasRole("ADMIN")

                        // Public car search
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/cars/search",
                                "/api/v1/cars/available",
                                "/api/v1/cars/all",
                                "/api/v1/cars/*"
                        ).permitAll()

                        // Admin car management
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/cars"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/cars/**"
                        ).hasAnyRole("ADMIN", "RENTAL_AGENT")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/v1/cars/**"
                        ).hasRole("ADMIN")

                        // Reservations
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/reservations/book"
                        ).hasRole("CUSTOMER")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/reservations/my-bookings"
                        ).hasRole("CUSTOMER")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/reservations/*/details"
                        ).hasAnyRole("CUSTOMER", "ADMIN","RENTAL_AGENT")

                        // Pricing estimate
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/pricing/estimate"
                        ).permitAll()

                        // Payments
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/payments/*/pay"
                        ).hasRole("CUSTOMER")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/payments/*/status",
                                "/api/v1/payments/my-history"
                        ).hasAnyRole("CUSTOMER", "ADMIN")

                        // Vehicle pickup and return
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/check-in-out/*/pickup",
                                "/api/v1/check-in-out/*/return"
                        ).hasRole("RENTAL_AGENT")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/check-in-out/*/report"
                        ).hasAnyRole("RENTAL_AGENT", "ADMIN")

                        // Cancellations
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/cancellations/*/eligibility"
                        ).hasAnyRole("CUSTOMER", "ADMIN")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/reservations/*/cancel"
                        ).hasAnyRole("CUSTOMER", "ADMIN")
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/reservations/all" // <--- ADD THIS
                        ).hasRole("ADMIN")

                        // Reviews and ratings
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/reviews"
                        ).hasRole("CUSTOMER")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/cars/*/reviews",
                                "/api/v1/cars/*/rating"
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/auth/user-details",
                                "/api/v1/auth/profile"
                        ).authenticated()
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/auth/profile"
                        ).authenticated()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/reviews/*/reply"
                        ).hasRole("ADMIN")
                        // Customer management
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/customers/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/customers/me" // <--- ADD THIS
                        ).hasRole("CUSTOMER")

                        // Customer management
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/customers/me" // <--- Ensure this is here
                        ).hasRole("CUSTOMER")

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of("http://localhost:5173")
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type"
                )
        );

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
