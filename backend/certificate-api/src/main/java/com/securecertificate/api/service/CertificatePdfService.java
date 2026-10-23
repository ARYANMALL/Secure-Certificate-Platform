package com.securecertificate.api.service;

import com.securecertificate.api.config.CertificateVerificationProperties;
import com.securecertificate.api.entity.Certificate;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class CertificatePdfService {

    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();

    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();

    private final QrCodeService qrCodeService;

    private final CertificateVerificationProperties verificationProperties;

    public CertificatePdfService(
            QrCodeService qrCodeService,
            CertificateVerificationProperties verificationProperties) {

        this.qrCodeService = qrCodeService;

        this.verificationProperties = verificationProperties;
    }

    public byte[] generate(Certificate certificate) {

        String verificationUrl = verificationProperties.baseUrl()
                + certificate.getCertificateNumber();

        byte[] qrCode = qrCodeService.generateQrCode(
                verificationUrl,
                180,
                180);

        try (
                PDDocument document = new PDDocument();

                ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);

            document.addPage(page);

            try (
                    PDPageContentStream content = new PDPageContentStream(
                            document,
                            page)) {

                drawBorder(content);

                drawCentered(
                        content,
                        "SECURE CERTIFICATE",
                        26,
                        440,
                        true);

                drawCentered(
                        content,
                        "Certificate of Achievement",
                        15,
                        410,
                        false);

                drawCentered(
                        content,
                        "This certificate is proudly presented to",
                        12,
                        355,
                        false);

                drawCentered(
                        content,
                        certificate.getStudentName(),
                        24,
                        315,
                        true);

                drawCentered(
                        content,
                        "for successfully completing",
                        12,
                        275,
                        false);

                drawCentered(
                        content,
                        certificate.getCourseName(),
                        20,
                        240,
                        true);

                drawCentered(
                        content,
                        certificate.getInstitutionName(),
                        14,
                        195,
                        false);

                drawText(
                        content,
                        "Certificate ID",
                        70,
                        110,
                        10,
                        true);

                drawText(
                        content,
                        certificate.getCertificateNumber(),
                        70,
                        92,
                        12,
                        false);

                drawText(
                        content,
                        "Issue Date",
                        70,
                        65,
                        10,
                        true);

                drawText(
                        content,
                        certificate.getIssueDate()
                                .format(
                                        DateTimeFormatter.ISO_DATE),
                        70,
                        47,
                        12,
                        false);

                drawText(
                        content,
                        "Issued by",
                        330,
                        110,
                        10,
                        true);

                drawText(
                        content,
                        certificate.getIssuerName(),
                        330,
                        92,
                        12,
                        false);

                PDImageXObject qrImage = PDImageXObject.createFromByteArray(
                        document,
                        qrCode,
                        "certificate-qr");

                content.drawImage(
                        qrImage,
                        PAGE_WIDTH - 180,
                        45,
                        110,
                        110);

                drawText(
                        content,
                        "Scan to verify",
                        PAGE_WIDTH - 175,
                        30,
                        9,
                        false);
            }

            document.save(output);

            return output.toByteArray();

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Unable to generate certificate PDF",
                    exception);
        }
    }

    private void drawBorder(
            PDPageContentStream content)
            throws java.io.IOException {

        content.setLineWidth(2);

        content.addRect(
                35,
                35,
                PAGE_WIDTH - 70,
                PAGE_HEIGHT - 70);

        content.stroke();

        content.setLineWidth(0.8f);

        content.addRect(
                48,
                48,
                PAGE_WIDTH - 96,
                PAGE_HEIGHT - 96);

        content.stroke();
    }

    private void drawCentered(
            PDPageContentStream content,
            String text,
            float fontSize,
            float y,
            boolean bold)
            throws java.io.IOException {

        PDType1Font font = bold
                ? new PDType1Font(
                        Standard14Fonts.FontName.HELVETICA_BOLD)
                : new PDType1Font(
                        Standard14Fonts.FontName.HELVETICA);

        float textWidth = font.getStringWidth(text)
                / 1000
                * fontSize;

        float x = (PAGE_WIDTH - textWidth) / 2;

        content.beginText();

        content.setFont(
                font,
                fontSize);

        content.newLineAtOffset(
                x,
                y);

        content.showText(text);

        content.endText();
    }

    private void drawText(
            PDPageContentStream content,
            String text,
            float x,
            float y,
            float fontSize,
            boolean bold)
            throws java.io.IOException {

        PDType1Font font = bold
                ? new PDType1Font(
                        Standard14Fonts.FontName.HELVETICA_BOLD)
                : new PDType1Font(
                        Standard14Fonts.FontName.HELVETICA);

        content.beginText();

        content.setFont(
                font,
                fontSize);

        content.newLineAtOffset(
                x,
                y);

        content.showText(
                text == null ? "" : text);

        content.endText();
    }
}