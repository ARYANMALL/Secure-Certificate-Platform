package com.securecertificate.api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
         * Password hashing.
         */
        @Bean
        public PasswordEncoder passwordEncoder() {

                return PasswordEncoderFactories
                                .createDelegatingPasswordEncoder();
        }

        /*
         * Authentication manager used during login.
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
         * Converts the "role" claim from our JWT into
         * Spring Security authorities.
         *
         * Example:
         *
         * JWT:
         * role = ISSUER
         *
         * Spring Security:
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
                 * Our JWT subject contains the user UUID.
                 */
                converter.setPrincipalClaimName(
                                "email");

                return converter;
        }

        /*
         * Main Spring Security configuration.
         */
        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http,
                        JwtAuthenticationConverter jwtAuthenticationConverter)
                        throws Exception {

                http

                                /*
                                 * Allow requests from our React frontend.
                                 *
                                 * The actual CORS rules are provided by
                                 * CorsConfig.java.
                                 */
                                .cors(cors -> {
                                })

                                /*
                                 * REST API is stateless, so CSRF protection
                                 * based on browser sessions is disabled.
                                 */
                                .csrf(csrf -> csrf.disable())

                                /*
                                 * No server-side login sessions.
                                 */
                                .sessionManagement(session -> session.sessionCreationPolicy(
                                                SessionCreationPolicy.STATELESS))

                                /*
                                 * Authorization rules.
                                 */
                                .authorizeHttpRequests(authorize -> authorize

                                                .requestMatchers(
                                                                "/api/health",
                                                                "/api/auth/**",
                                                                "/api/public/**")
                                                .permitAll()

                                                .requestMatchers(
                                                                "/api/institutions/**")
                                                .hasRole("ADMIN")

                                                .requestMatchers(
                                                                "/api/admin/**")
                                                .hasRole("ADMIN")

                                                .requestMatchers(
                                                                "/api/certificates/**")
                                                .hasRole("ISSUER")

                                                .requestMatchers(
                                                                "/api/student/**")
                                                .hasRole("STUDENT")

                                                .requestMatchers(
                                                                "/api/users/me")
                                                .authenticated()

                                                .requestMatchers(
                                                                "/api/issuer/**")
                                                .hasRole("ISSUER")
                                                
                                                .anyRequest()
                                                .denyAll())

                                /*
                                 * JWT authentication.
                                 */
                                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(
                                                jwtAuthenticationConverter)))

                                /*
                                 * Disable browser login page.
                                 */
                                .formLogin(form -> form.disable())

                                /*
                                 * We use JWT instead of HTTP Basic.
                                 */
                                .httpBasic(basic -> basic.disable());

                return http.build();
        }
}