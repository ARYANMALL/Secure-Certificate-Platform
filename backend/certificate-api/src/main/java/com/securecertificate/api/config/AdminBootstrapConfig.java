package com.securecertificate.api.config;

import com.securecertificate.api.entity.User;
import com.securecertificate.api.entity.UserRole;
import com.securecertificate.api.repository.UserRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Locale;

@Configuration(proxyBeanMethods = false)
@ConditionalOnBooleanProperty(prefix = "app.bootstrap.admin", name = "enabled")
@EnableConfigurationProperties(AdminBootstrapProperties.class)
public class AdminBootstrapConfig {

    @Bean
    ApplicationRunner adminBootstrap(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AdminBootstrapProperties properties) {

        return args -> {

            String email = properties.email()
                    .trim()
                    .toLowerCase(Locale.ROOT);

            var existingUser = userRepository
                    .findByEmailIgnoreCase(email);

            /*
             * If the admin already exists,
             * don't create another one.
             */
            if (existingUser.isPresent()) {

                if (existingUser.get().getRole() != UserRole.ADMIN) {

                    throw new IllegalStateException(
                            "Bootstrap admin email belongs "
                                    + "to a non-admin user");
                }

                return;
            }

            /*
             * Hash the bootstrap password.
             */
            String passwordHash = passwordEncoder.encode(
                    properties.password());

            /*
             * Create administrator.
             */
            User admin = new User(
                    properties.fullName().trim(),
                    email,
                    passwordHash,
                    UserRole.ADMIN,
                    null);

            userRepository.save(admin);
        };
    }
}