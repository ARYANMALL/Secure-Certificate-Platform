package com.securecertificate.api.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(CertificateSigningProperties.class)
public class CertificateSigningConfig {

    @Bean
    public KeyPair certificateSigningKeyPair(
            CertificateSigningProperties properties) {

        Path keystorePath = Path.of(properties.keystorePath())
                .toAbsolutePath()
                .normalize();

        if (!Files.isRegularFile(keystorePath)) {

            throw new IllegalStateException(
                    "Certificate signing keystore not found: "
                            + keystorePath);
        }

        char[] password = properties.keystorePassword()
                .toCharArray();

        try (InputStream inputStream = Files.newInputStream(keystorePath)) {

            KeyStore keyStore = KeyStore.getInstance("PKCS12");

            keyStore.load(
                    inputStream,
                    password);

            String alias = properties.keyAlias();

            if (!keyStore.containsAlias(alias)) {

                throw new IllegalStateException(
                        "Certificate signing key alias not found: "
                                + alias);
            }

            Key key = keyStore.getKey(
                    alias,
                    password);

            if (!(key instanceof PrivateKey privateKey)) {

                throw new IllegalStateException(
                        "Keystore entry is not a private key: "
                                + alias);
            }

            java.security.cert.Certificate certificate = keyStore.getCertificate(alias);

            if (certificate == null) {

                throw new IllegalStateException(
                        "No certificate found for key alias: "
                                + alias);
            }

            PublicKey publicKey = certificate.getPublicKey();

            if (!"RSA".equalsIgnoreCase(
                    privateKey.getAlgorithm())) {

                throw new IllegalStateException(
                        "Certificate signing key must use RSA");
            }

            return new KeyPair(
                    publicKey,
                    privateKey);

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Unable to load certificate signing keystore",
                    exception);

        } finally {

            Arrays.fill(password, '\0');
        }
    }
}