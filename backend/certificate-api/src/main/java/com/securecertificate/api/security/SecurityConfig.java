package com.securecertificate.api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        /*
         * =========================================================
         * PASSWORD ENCODER
         * =========================================================
         */
        @Bean
        public PasswordEncoder passwordEncoder() {

                return PasswordEncoderFactories
                                .createDelegatingPasswordEncoder();
        }

        /*
         * =========================================================
         * AUTHENTICATION MANAGER
         * =========================================================
         */
        @Bean
        public AuthenticationManager authenticationManager(
                        CustomUserDetailsService userDetailsService,
                        PasswordEncoder passwordEncoder) {

                DaoAuthenticationProvider provider = new DaoAuthenticationProvider(
                                userDetailsService);

                provider.setPasswordEncoder(
                                passwordEncoder);

                return new ProviderManager(provider);
        }

        /*
         * =========================================================
         * JWT AUTHORITY CONVERTER
         * =========================================================
         *
         * JWT:
         *
         * role = ISSUER
         *
         * becomes:
         *
         * ROLE_ISSUER
         */
        @Bean
        public JwtAuthenticationConverter jwtAuthenticationConverter() {

                JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();

                authoritiesConverter.setAuthoritiesClaimName(
                                "role");

                authoritiesConverter.setAuthorityPrefix(
                                "ROLE_");

                JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

                converter.setJwtGrantedAuthoritiesConverter(
                                authoritiesConverter);

                /*
                 * JWT subject contains user UUID.
                 */
                converter.setPrincipalClaimName(
                                "email");

                return converter;
        }

        /*
         * =========================================================
         * SECURITY FILTER CHAIN
         * =========================================================
         */
        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http,
                        JwtAuthenticationConverter jwtAuthenticationConverter)
                        throws Exception {

                http

                                /*
                                 * CORS
                                 */
                                .cors(cors -> {
                                })

                                /*
                                 * REST API is stateless.
                                 */
                                .csrf(csrf -> csrf.disable())

                                /*
                                 * No server-side sessions.
                                 */
                                .sessionManagement(session -> session.sessionCreationPolicy(
                                                SessionCreationPolicy.STATELESS))

                                /*
                                 * =================================================
                                 * AUTHORIZATION RULES
                                 * =================================================
                                 */
                                .authorizeHttpRequests(authorize -> authorize

                                                /*
                                                 * ---------------------------------
                                                 * PUBLIC ENDPOINTS
                                                 * ---------------------------------
                                                 */
                                                .requestMatchers(
                                                                "/api/health",
                                                                "/api/auth/**",
                                                                "/api/public/**")
                                                .permitAll()

                                                /*
                                                 * ---------------------------------
                                                 * ADMIN
                                                 * ---------------------------------
                                                 */
                                                .requestMatchers(
                                                                "/api/institutions/**")
                                                .hasRole("ADMIN")

                                                .requestMatchers(
                                                                "/api/admin/**")
                                                .hasRole("ADMIN")

                                                /*
                                                 * ---------------------------------
                                                 * CERTIFICATE REQUESTS
                                                 * ---------------------------------
                                                 *
                                                 * STUDENT:
                                                 * Create request
                                                 * View own requests
                                                 */

                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/certificate-requests")
                                                .hasRole("STUDENT")

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/certificate-requests/my")
                                                .hasRole("STUDENT")

                                                /*
                                                 * ISSUER:
                                                 * View pending requests
                                                 * Accept requests
                                                 */

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/certificate-requests/issuer")
                                                .hasRole("ISSUER")

                                                .requestMatchers(
                                                                HttpMethod.PATCH,
                                                                "/api/certificate-requests/*/accept")
                                                .hasRole("ISSUER")

                                                /*
                                                 * ADMIN:
                                                 * View requests awaiting approval
                                                 */

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/certificate-requests/admin")
                                                .hasRole("ADMIN")

                                                /*
                                                 * ADMIN:
                                                 * Approve request
                                                 */

                                                .requestMatchers(
                                                                HttpMethod.PATCH,
                                                                "/api/certificate-requests/*/approve")
                                                .hasRole("ADMIN")

                                                /*
                                                 * ADMIN:
                                                 * Reject request
                                                 */

                                                .requestMatchers(
                                                                HttpMethod.PATCH,
                                                                "/api/certificate-requests/*/reject")
                                                .hasRole("ADMIN")

                                                /*
                                                 * ---------------------------------
                                                 * DIRECT CERTIFICATE MANAGEMENT
                                                 * ---------------------------------
                                                 *
                                                 * Existing issuer functionality.
                                                 */
                                                .requestMatchers(
                                                                "/api/certificates/**")
                                                .hasRole("ISSUER")

                                                /*
                                                 * ---------------------------------
                                                 * STUDENT
                                                 * ---------------------------------
                                                 */
                                                .requestMatchers(
                                                                "/api/student/**")
                                                .hasRole("STUDENT")

                                                /*
                                                 * ---------------------------------
                                                 * ISSUER
                                                 * ---------------------------------
                                                 */
                                                .requestMatchers(
                                                                "/api/issuer/**")
                                                .hasRole("ISSUER")

                                                /*
                                                 * ---------------------------------
                                                 * CURRENT USER
                                                 * ---------------------------------
                                                 */
                                                .requestMatchers(
                                                                "/api/users/me")
                                                .authenticated()

                                                /*
                                                 * ---------------------------------
                                                 * EVERYTHING ELSE
                                                 * ---------------------------------
                                                 *
                                                 * Secure by default.
                                                 */
                                                .anyRequest()
                                                .denyAll())

                                /*
                                 * =================================================
                                 * JWT RESOURCE SERVER
                                 * =================================================
                                 */
                                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(
                                                jwtAuthenticationConverter)))

                                /*
                                 * Disable browser login.
                                 */
                                .formLogin(form -> form.disable())

                                /*
                                 * We use JWT, not HTTP Basic.
                                 */
                                .httpBasic(basic -> basic.disable());

                return http.build();
        }
}