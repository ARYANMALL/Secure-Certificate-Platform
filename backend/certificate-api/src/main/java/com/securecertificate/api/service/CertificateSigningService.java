package com.securecertificate.api.service;

import com.securecertificate.api.config.CertificateSigningProperties;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.Signature;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class CertificateSigningService {

    private static final String HASH_ALGORITHM = "SHA-256";

    private static final String REQUIRED_SIGNATURE_ALGORITHM = "SHA256withRSA";

    private final KeyPair signingKeyPair;
    private final String signatureAlgorithm;

    public CertificateSigningService(
            KeyPair signingKeyPair,
            CertificateSigningProperties properties) {

        this.signingKeyPair = signingKeyPair;
        this.signatureAlgorithm = properties.signatureAlgorithm();

        if (!REQUIRED_SIGNATURE_ALGORITHM.equals(
                this.signatureAlgorithm)) {

            throw new IllegalStateException(
                    "Unsupported certificate signature algorithm: "
                            + this.signatureAlgorithm);
        }

        if (!"RSA".equalsIgnoreCase(
                signingKeyPair.getPrivate().getAlgorithm())) {

            throw new IllegalStateException(
                    "Certificate signing private key must be RSA");
        }
    }

    /**
     * Calculates a SHA-256 hash of the supplied data.
     *
     * Result:
     * 64 lowercase hexadecimal characters.
     */
    public String calculateSha256(String data) {

        if (data == null) {
            throw new IllegalArgumentException(
                    "Data to hash cannot be null");
        }

        try {

            MessageDigest digest = MessageDigest.getInstance(
                    HASH_ALGORITHM);

            byte[] hash = digest.digest(
                    data.getBytes(
                            StandardCharsets.UTF_8));

            return HexFormat.of()
                    .formatHex(hash);

        } catch (GeneralSecurityException exception) {

            throw new IllegalStateException(
                    "Unable to calculate SHA-256 hash",
                    exception);
        }
    }

    /**
     * Digitally signs the supplied data using
     * the certificate platform's RSA private key.
     *
     * The returned signature is Base64 encoded
     * so it can safely be stored in PostgreSQL.
     */
    public String sign(String data) {

        if (data == null) {
            throw new IllegalArgumentException(
                    "Data to sign cannot be null");
        }

        try {

            Signature signature = Signature.getInstance(
                    signatureAlgorithm);

            signature.initSign(
                    signingKeyPair.getPrivate());

            signature.update(
                    data.getBytes(
                            StandardCharsets.UTF_8));

            byte[] signatureBytes = signature.sign();

            return Base64.getEncoder()
                    .encodeToString(
                            signatureBytes);

        } catch (GeneralSecurityException exception) {

            throw new IllegalStateException(
                    "Unable to digitally sign certificate data",
                    exception);
        }
    }

    /**
     * Verifies a Base64 encoded digital signature
     * against the supplied data using the RSA public key.
     */
    public boolean verify(
            String data,
            String encodedSignature) {

        if (data == null ||
                encodedSignature == null) {

            return false;
        }

        try {

            byte[] signatureBytes = Base64.getDecoder()
                    .decode(encodedSignature);

            Signature signature = Signature.getInstance(
                    signatureAlgorithm);

            signature.initVerify(
                    signingKeyPair.getPublic());

            signature.update(
                    data.getBytes(
                            StandardCharsets.UTF_8));

            return signature.verify(
                    signatureBytes);

        } catch (IllegalArgumentException exception) {

            return false;

        } catch (GeneralSecurityException exception) {

            throw new IllegalStateException(
                    "Unable to verify certificate signature",
                    exception);
        }
    }

    public String getSignatureAlgorithm() {

        return signatureAlgorithm;
    }
}