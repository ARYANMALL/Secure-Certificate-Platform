package com.securecertificate.api.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(CertificateVerificationProperties.class)
public class CertificateVerificationConfig {
}