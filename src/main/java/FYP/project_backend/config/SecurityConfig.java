package FYP.project_backend.config;

import FYP.project_backend.auth.jwt.JwtAuthenticationEntryPoint;
import FYP.project_backend.auth.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final AuthenticationProvider authenticationProvider;

    private final JwtAuthenticationEntryPoint authenticationEntryPoint;


    @Bean
    AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();

    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of("http://localhost:4200"));

        configuration.setAllowedMethods(
                List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));

        configuration.setAllowedHeaders(
                List.of("*"));

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;

    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http

                .csrf(csrf -> csrf.disable())

                .cors(cors -> cors.configurationSource(
                        corsConfigurationSource()))

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))

                .authenticationProvider(authenticationProvider)

                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(
                                authenticationEntryPoint))

                .authorizeHttpRequests(auth -> auth

                        // ================= AUTH =================

                        .requestMatchers("/api/auth/**")
                        .permitAll()

                        .requestMatchers("/api/users/**")
                        .hasRole("ADMIN")

                        // ================= LICENSE =================

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/licenses/apply"
                        )
                        .hasRole("BUSINESS_OWNER")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/licenses/my"
                        )
                        .hasRole("BUSINESS_OWNER")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/licenses/calculate-fee"
                        )
                        .hasRole("BUSINESS_OWNER")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/licenses/*/renew"
                        )
                        .hasRole("BUSINESS_OWNER")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/licenses/pending"
                        )
                        .hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/licenses/*/approve"
                        )
                        .hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/licenses/*/reject"
                        )
                        .hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/licenses/*"
                        )
                        .hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/licenses"
                        )
                        .hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/licenses/*"
                        )
                        .authenticated()

                        // ================= INSPECTIONS =================
                        .requestMatchers("/api/inspections/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/api/inspections/**")
                        .hasRole("ADMIN")

                        // ================= COMPLAINTS =================
                        .requestMatchers(HttpMethod.POST,"/api/complaints")
                        .hasRole("TOURIST")

                        .requestMatchers("/api/complaints/my")
                        .hasRole("TOURIST")

                        .requestMatchers("/api/complaints/pending")
                        .hasRole("ADMIN")

                        .requestMatchers("/api/complaints/**")
                        .authenticated()

                        // ================= PAYMENT =================
                        .requestMatchers(HttpMethod.POST,"/api/payments")
                        .hasRole("BUSINESS_OWNER")

                        .requestMatchers("/api/payments/my")
                        .hasRole("BUSINESS_OWNER")

                        .requestMatchers("/api/payments/pending")
                        .hasRole("ADMIN")

                        .requestMatchers("/api/payments/**")
                        .authenticated()

                        // ================= ANALYTIC =================
                        .requestMatchers("/api/analytics/admin")
                        .hasRole("ADMIN")

                        .requestMatchers("/api/analytics/business-owner")
                        .hasRole("BUSINESS_OWNER")

                        .requestMatchers("/api/analytics/tourist")
                        .hasRole("TOURIST")

                        .requestMatchers("/api/analytics/reports")
                        .hasRole("ADMIN")

                        .requestMatchers("/api/analytics/monthly-revenue")
                        .hasRole("ADMIN")

                        .requestMatchers("/api/analytics/monthly-licenses")
                        .hasRole("ADMIN")

                        .requestMatchers("/api/analytics/monthly-complaints")
                        .hasRole("ADMIN")

                        // ================= NOTIFICATIONS =================
                        .requestMatchers("/api/notifications/my")
                        .authenticated()

                        .requestMatchers("/api/notifications/unread-count")
                        .authenticated()

                        .requestMatchers("/api/notifications/read-all")
                        .authenticated()

                        .requestMatchers("/api/notifications/*/read")
                        .authenticated()

                        .requestMatchers("/api/notifications/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST,"/api/users")
                        .hasRole("ADMIN")

                        .anyRequest()
                        .authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();

    }

}