package com.securecertificate.api.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.securecertificate.api.entity.Certificate;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

@Service
public class CertificatePdfService {

        private static final Color NAVY = new Color(10, 41, 66);
        private static final Color NAVY_2 = new Color(23, 63, 93);
        private static final Color GOLD = new Color(212, 165, 44);
        private static final Color GOLD_LIGHT = new Color(241, 207, 112);
        private static final Color PAPER = new Color(252, 251, 247);
        private static final Color TEXT = new Color(23, 32, 51);
        private static final Color MUTED = new Color(102, 117, 138);
        private static final Color LIGHT_LINE = new Color(221, 213, 197);

        private static final PDType1Font HELVETICA = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

        private static final PDType1Font HELVETICA_BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

        private static final PDType1Font HELVETICA_OBLIQUE = new PDType1Font(
                        Standard14Fonts.FontName.HELVETICA_OBLIQUE);

        private static final PDType1Font TIMES_ITALIC = new PDType1Font(Standard14Fonts.FontName.TIMES_ITALIC);

        @Value("${app.verification.base-url:http://localhost:3000/verify/}")
        private String verificationBaseUrl;

        /**
         * Generates a print-ready A4 landscape certificate.
         */
        public byte[] generate(Certificate certificate) {

                try (PDDocument document = new PDDocument()) {

                        PDRectangle landscapeA4 = new PDRectangle(
                                        PDRectangle.A4.getHeight(),
                                        PDRectangle.A4.getWidth());

                        PDPage page = new PDPage(landscapeA4);
                        document.addPage(page);

                        drawCertificate(
                                        document,
                                        page,
                                        certificate);

                        ByteArrayOutputStream output = new ByteArrayOutputStream();

                        document.save(output);

                        return output.toByteArray();

                } catch (IOException exception) {

                        throw new IllegalStateException(
                                        "Unable to generate certificate PDF",
                                        exception);
                }
        }

        private void drawCertificate(
                        PDDocument document,
                        PDPage page,
                        Certificate certificate) throws IOException {

                float width = page.getMediaBox().getWidth();
                float height = page.getMediaBox().getHeight();

                try (PDPageContentStream content = new PDPageContentStream(
                                document,
                                page)) {

                        /*
                         * ==============================================
                         * PAPER BACKGROUND
                         * ==============================================
                         */
                        content.setNonStrokingColor(PAPER);
                        content.addRect(0, 0, width, height);
                        content.fill();

                        drawTexture(content, width, height);
                        drawBorders(content, width, height);
                        drawHeader(content, width, height);
                        drawCornerAccents(content, width, height);

                        /*
                         * ==============================================
                         * HEADER
                         * ==============================================
                         */
                        drawCenteredText(
                                        content,
                                        "CERTIFICATE OF ACHIEVEMENT",
                                        HELVETICA_BOLD,
                                        24,
                                        NAVY,
                                        width / 2,
                                        height - 105);

                        drawCenteredText(
                                        content,
                                        "Digitally Signed & Verifiable Credential",
                                        HELVETICA,
                                        10.5f,
                                        NAVY_2,
                                        width / 2,
                                        height - 124);

                        drawCenteredText(
                                        content,
                                        "This certificate is proudly presented to",
                                        HELVETICA_OBLIQUE,
                                        9.5f,
                                        MUTED,
                                        width / 2,
                                        height - 151);

                        /*
                         * ==============================================
                         * STUDENT NAME
                         * ==============================================
                         */
                        drawCenteredText(
                                        content,
                                        safe(certificate.getStudentName()),
                                        HELVETICA_BOLD,
                                        29,
                                        NAVY,
                                        width / 2,
                                        height - 184);

                        drawCenteredText(
                                        content,
                                        "for successfully completing",
                                        HELVETICA_OBLIQUE,
                                        9.5f,
                                        MUTED,
                                        width / 2,
                                        height - 207);

                        /*
                         * ==============================================
                         * COURSE
                         * ==============================================
                         */
                        drawCenteredWrappedText(
                                        content,
                                        safe(certificate.getCourseName()),
                                        HELVETICA_BOLD,
                                        17,
                                        NAVY_2,
                                        width / 2,
                                        height - 232,
                                        390,
                                        21);

                        drawCenteredText(
                                        content,
                                        safe(certificate.getInstitutionName()),
                                        HELVETICA_BOLD,
                                        11,
                                        TEXT,
                                        width / 2,
                                        height - 270);

                        /*
                         * ==============================================
                         * VERIFIED CREDENTIAL TAG
                         * ==============================================
                         */
                        drawTag(
                                        content,
                                        width - 300,
                                        height - 295,
                                        125,
                                        22,
                                        "DIGITALLY VERIFIED");

                        /*
                         * ==============================================
                         * SEAL
                         * ==============================================
                         */
                        drawSeal(
                                        content,
                                        width - 105,
                                        height - 92);

                        /*
                         * ==============================================
                         * INFORMATION PANEL
                         * ==============================================
                         */
                        float panelLeft = 72;
                        float panelRight = width - 195;
                        float panelTop = height - 315;

                        content.setStrokingColor(GOLD);
                        content.setLineWidth(1.1f);
                        content.moveTo(panelLeft, panelTop);
                        content.lineTo(panelRight, panelTop);
                        content.stroke();

                        drawLabelValue(
                                        content,
                                        "CERTIFICATE ID",
                                        safe(certificate.getCertificateNumber()),
                                        panelLeft,
                                        panelTop - 22);

                        drawLabelValue(
                                        content,
                                        "ISSUED BY",
                                        safe(certificate.getIssuerName()),
                                        panelLeft + 180,
                                        panelTop - 22);

                        String date = certificate.getIssueDate() == null
                                        ? ""
                                        : certificate.getIssueDate().toString();

                        drawLabelValue(
                                        content,
                                        "ISSUE DATE",
                                        date,
                                        panelLeft + 360,
                                        panelTop - 22);

                        /*
                         * ==============================================
                         * SIGNATURE AREA
                         * ==============================================
                         */
                        float signatureY = 86;

                        content.setStrokingColor(LIGHT_LINE);
                        content.setLineWidth(0.8f);
                        content.moveTo(panelLeft, signatureY + 18);
                        content.lineTo(panelLeft + 145, signatureY + 18);
                        content.stroke();

                        drawCenteredText(
                                        content,
                                        safe(certificate.getIssuerName()),
                                        TIMES_ITALIC,
                                        13,
                                        NAVY,
                                        panelLeft + 72.5f,
                                        signatureY + 24);

                        drawLeftText(
                                        content,
                                        "AUTHORIZED ISSUER",
                                        HELVETICA_BOLD,
                                        6.5f,
                                        MUTED,
                                        panelLeft,
                                        signatureY + 5);

                        /*
                         * ==============================================
                         * CRYPTOGRAPHIC FOOTER
                         * ==============================================
                         */
                        drawLeftText(
                                        content,
                                        "CRYPTOGRAPHICALLY SIGNED",
                                        HELVETICA_BOLD,
                                        7,
                                        NAVY,
                                        panelLeft + 190,
                                        signatureY + 18);

                        drawLeftText(
                                        content,
                                        safe(certificate.getSignatureAlgorithm()),
                                        HELVETICA,
                                        7.5f,
                                        MUTED,
                                        panelLeft + 190,
                                        signatureY + 7);

                        drawLeftText(
                                        content,
                                        "Hash integrity and digital signature can be independently verified.",
                                        HELVETICA,
                                        6.5f,
                                        MUTED,
                                        panelLeft + 190,
                                        signatureY - 5);

                        /*
                         * ==============================================
                         * QR CODE
                         * ==============================================
                         */
                        String verificationUrl = buildVerificationUrl(
                                        certificate.getCertificateNumber());

                        PDImageXObject qr = createQrImage(
                                        document,
                                        verificationUrl);

                        float qrSize = 74;
                        float qrX = width - 145;
                        float qrY = 40;

                        content.setNonStrokingColor(Color.WHITE);
                        content.addRect(
                                        qrX - 7,
                                        qrY - 7,
                                        qrSize + 14,
                                        qrSize + 14);
                        content.fill();

                        content.setStrokingColor(GOLD);
                        content.setLineWidth(1);
                        content.addRect(
                                        qrX - 7,
                                        qrY - 7,
                                        qrSize + 14,
                                        qrSize + 14);
                        content.stroke();

                        content.drawImage(
                                        qr,
                                        qrX,
                                        qrY,
                                        qrSize,
                                        qrSize);

                        drawCenteredText(
                                        content,
                                        "SCAN TO VERIFY",
                                        HELVETICA_BOLD,
                                        6.5f,
                                        NAVY,
                                        qrX + qrSize / 2,
                                        qrY - 16);

                        /*
                         * ==============================================
                         * STATUS
                         * ==============================================
                         */
                        String status = certificate.getStatus() == null
                                        ? ""
                                        : certificate.getStatus().name();

                        if ("REVOKED".equals(status)) {
                                drawRevokedStamp(
                                                content,
                                                width / 2,
                                                height / 2 - 5);
                        }
                }
        }

        private void drawTexture(
                        PDPageContentStream content,
                        float width,
                        float height) throws IOException {

                content.setStrokingColor(
                                new Color(238, 232, 219));
                content.setLineWidth(0.25f);

                for (int x = -((int) height); x < width + height; x += 16) {
                        content.moveTo(x, 0);
                        content.lineTo(x + height, height);
                }

                content.stroke();
        }

        private void drawBorders(
                        PDPageContentStream content,
                        float width,
                        float height) throws IOException {

                content.setStrokingColor(NAVY);
                content.setLineWidth(2.5f);
                content.addRect(
                                34,
                                30,
                                width - 68,
                                height - 60);
                content.stroke();

                content.setStrokingColor(GOLD);
                content.setLineWidth(0.9f);
                content.addRect(
                                45,
                                41,
                                width - 90,
                                height - 82);
                content.stroke();
        }

        private void drawHeader(
                        PDPageContentStream content,
                        float width,
                        float height) throws IOException {

                content.setNonStrokingColor(NAVY);
                content.addRect(
                                0,
                                height - 76,
                                width,
                                76);
                content.fill();

                content.setNonStrokingColor(GOLD);
                content.addRect(
                                0,
                                height - 84,
                                width,
                                8);
                content.fill();

                drawLeftText(
                                content,
                                "SECURECERT",
                                HELVETICA_BOLD,
                                12,
                                Color.WHITE,
                                55,
                                height - 38);

                drawLeftText(
                                content,
                                "DIGITAL CERTIFICATE PLATFORM",
                                HELVETICA,
                                6.5f,
                                Color.WHITE,
                                55,
                                height - 55);
        }

        private void drawCornerAccents(
                        PDPageContentStream content,
                        float width,
                        float height) throws IOException {

                content.setNonStrokingColor(NAVY_2);
                drawPolygon(
                                content,
                                new float[][] {
                                                { 34, height - 76 },
                                                { 165, height - 76 },
                                                { 124, height - 30 },
                                                { 34, height - 30 }
                                });
                content.fill();

                content.setNonStrokingColor(GOLD_LIGHT);
                drawPolygon(
                                content,
                                new float[][] {
                                                { 126, height - 76 },
                                                { 190, height - 76 },
                                                { 150, height - 28 },
                                                { 112, height - 28 }
                                });
                content.fill();

                content.setNonStrokingColor(NAVY_2);
                drawPolygon(
                                content,
                                new float[][] {
                                                { width - 225, 30 },
                                                { width - 34, 30 },
                                                { width - 34, 112 },
                                                { width - 180, 112 }
                                });
                content.fill();

                content.setNonStrokingColor(GOLD);
                drawPolygon(
                                content,
                                new float[][] {
                                                { width - 145, 30 },
                                                { width - 100, 30 },
                                                { width - 55, 112 },
                                                { width - 100, 112 }
                                });
                content.fill();
        }

        private void drawPolygon(
                        PDPageContentStream content,
                        float[][] points) throws IOException {

                content.moveTo(points[0][0], points[0][1]);

                for (int i = 1; i < points.length; i++) {
                        content.lineTo(points[i][0], points[i][1]);
                }

                content.closePath();
        }

        private void drawTag(
                        PDPageContentStream content,
                        float x,
                        float y,
                        float width,
                        float height,
                        String text) throws IOException {

                content.setNonStrokingColor(GOLD);
                roundedRect(content, x, y, width, height, 10);
                content.fill();

                drawCenteredText(
                                content,
                                text,
                                HELVETICA_BOLD,
                                6.5f,
                                NAVY,
                                x + width / 2,
                                y + 7);
        }

        private void drawSeal(
                        PDPageContentStream content,
                        float cx,
                        float cy) throws IOException {

                content.setNonStrokingColor(NAVY);
                circle(content, cx, cy, 54);
                content.fill();

                content.setNonStrokingColor(GOLD);
                circle(content, cx, cy, 43);
                content.fill();

                content.setNonStrokingColor(PAPER);
                circle(content, cx, cy, 33);
                content.fill();

                content.setStrokingColor(NAVY);
                content.setLineWidth(1);
                circle(content, cx, cy, 33);
                content.stroke();

                content.setNonStrokingColor(GOLD);
                star(content, cx, cy + 3, 18, 8, 10);
                content.fill();

                drawCenteredText(
                                content,
                                "SECURECERT",
                                HELVETICA_BOLD,
                                7,
                                NAVY,
                                cx,
                                cy - 26);

                drawCenteredText(
                                content,
                                "AUTHENTIC",
                                HELVETICA_BOLD,
                                5.5f,
                                NAVY,
                                cx,
                                cy - 36);
        }

        private void star(
                        PDPageContentStream content,
                        float cx,
                        float cy,
                        float outer,
                        float inner,
                        int points) throws IOException {

                double step = Math.PI / points;
                double start = Math.PI / 2;

                double a = start;
                content.moveTo(
                                cx + (float) (outer * Math.cos(a)),
                                cy + (float) (outer * Math.sin(a)));

                for (int i = 1; i < points * 2; i++) {
                        a = start + i * step;
                        float r = i % 2 == 0 ? outer : inner;
                        content.lineTo(
                                        cx + (float) (r * Math.cos(a)),
                                        cy + (float) (r * Math.sin(a)));
                }

                content.closePath();
        }

        private void drawLabelValue(
                        PDPageContentStream content,
                        String label,
                        String value,
                        float x,
                        float y) throws IOException {

                drawLeftText(
                                content,
                                label,
                                HELVETICA_BOLD,
                                7.2f,
                                MUTED,
                                x,
                                y);

                drawLeftText(
                                content,
                                value,
                                HELVETICA,
                                9.5f,
                                TEXT,
                                x,
                                y - 14);
        }

        private void circle(
                        PDPageContentStream content,
                        float cx,
                        float cy,
                        float radius) throws IOException {

                final float k = 0.5522848f;

                content.moveTo(cx + radius, cy);
                content.curveTo(
                                cx + radius, cy + k * radius,
                                cx + k * radius, cy + radius,
                                cx, cy + radius);
                content.curveTo(
                                cx - k * radius, cy + radius,
                                cx - radius, cy + k * radius,
                                cx - radius, cy);
                content.curveTo(
                                cx - radius, cy - k * radius,
                                cx - k * radius, cy - radius,
                                cx, cy - radius);
                content.curveTo(
                                cx + k * radius, cy - radius,
                                cx + radius, cy - k * radius,
                                cx + radius, cy);
                content.closePath();
        }

        private void roundedRect(
                        PDPageContentStream content,
                        float x,
                        float y,
                        float width,
                        float height,
                        float radius) throws IOException {

                final float k = 0.5522848f;
                float r = Math.min(radius, Math.min(width, height) / 2);

                content.moveTo(x + r, y);
                content.lineTo(x + width - r, y);
                content.curveTo(
                                x + width - r + k * r, y,
                                x + width, y + r - k * r,
                                x + width, y + r);
                content.lineTo(x + width, y + height - r);
                content.curveTo(
                                x + width, y + height - r + k * r,
                                x + width - r + k * r, y + height,
                                x + width - r, y + height);
                content.lineTo(x + r, y + height);
                content.curveTo(
                                x + r - k * r, y + height,
                                x, y + height - r + k * r,
                                x, y + height - r);
                content.lineTo(x, y + r);
                content.curveTo(
                                x, y + r - k * r,
                                x + r - k * r, y,
                                x + r, y);
                content.closePath();
        }

        private void drawRevokedStamp(
                        PDPageContentStream content,
                        float cx,
                        float cy) throws IOException {

                content.saveGraphicsState();
                content.setLineWidth(3);
                content.setStrokingColor(new Color(180, 40, 40));
                content.setNonStrokingColor(new Color(180, 40, 40));
                content.addRect(cx - 82, cy - 22, 164, 44);
                content.stroke();

                drawCenteredText(
                                content,
                                "REVOKED",
                                HELVETICA_BOLD,
                                20,
                                new Color(180, 40, 40),
                                cx,
                                cy - 7);

                content.restoreGraphicsState();
        }

        private void drawLeftText(
                        PDPageContentStream content,
                        String text,
                        PDType1Font font,
                        float size,
                        Color color,
                        float x,
                        float y) throws IOException {

                content.beginText();
                content.setFont(font, size);
                content.setNonStrokingColor(color);
                content.newLineAtOffset(x, y);
                content.showText(safe(text));
                content.endText();
        }

        private void drawCenteredText(
                        PDPageContentStream content,
                        String text,
                        PDType1Font font,
                        float size,
                        Color color,
                        float centerX,
                        float y) throws IOException {

                String safeText = safe(text);
                float textWidth = font.getStringWidth(safeText) / 1000f * size;

                drawLeftText(
                                content,
                                safeText,
                                font,
                                size,
                                color,
                                centerX - textWidth / 2,
                                y);
        }

        private void drawCenteredWrappedText(
                        PDPageContentStream content,
                        String text,
                        PDType1Font font,
                        float size,
                        Color color,
                        float centerX,
                        float startY,
                        float maxWidth,
                        float lineHeight) throws IOException {

                String[] words = safe(text).split("\\s+");
                StringBuilder line = new StringBuilder();
                float y = startY;

                for (String word : words) {

                        String candidate = line.length() == 0
                                        ? word
                                        : line + " " + word;

                        float candidateWidth = font.getStringWidth(candidate) / 1000f * size;

                        if (candidateWidth > maxWidth &&
                                        line.length() > 0) {

                                drawCenteredText(
                                                content,
                                                line.toString(),
                                                font,
                                                size,
                                                color,
                                                centerX,
                                                y);

                                y -= lineHeight;
                                line = new StringBuilder(word);

                        } else {
                                line = new StringBuilder(candidate);
                        }
                }

                if (line.length() > 0) {
                        drawCenteredText(
                                        content,
                                        line.toString(),
                                        font,
                                        size,
                                        color,
                                        centerX,
                                        y);
                }
        }

        private PDImageXObject createQrImage(
                        PDDocument document,
                        String url) throws IOException {

                try {
                        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);

                        hints.put(
                                        EncodeHintType.ERROR_CORRECTION,
                                        ErrorCorrectionLevel.H);

                        hints.put(
                                        EncodeHintType.MARGIN,
                                        2);

                        BitMatrix matrix = new QRCodeWriter().encode(
                                        url,
                                        BarcodeFormat.QR_CODE,
                                        320,
                                        320,
                                        hints);

                        BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);

                        ByteArrayOutputStream output = new ByteArrayOutputStream();

                        ImageIO.write(
                                        image,
                                        "PNG",
                                        output);

                        return PDImageXObject.createFromByteArray(
                                        document,
                                        output.toByteArray(),
                                        "verification-qr");

                } catch (Exception exception) {

                        throw new IOException(
                                        "Unable to create verification QR code",
                                        exception);
                }
        }

        private String buildVerificationUrl(
                        String certificateNumber) {

                String base = verificationBaseUrl == null
                                ? "http://localhost:3000/verify/"
                                : verificationBaseUrl.trim();

                if (!base.endsWith("/")) {
                        base += "/";
                }

                return base + certificateNumber;
        }

        private String safe(String value) {

                if (value == null) {
                        return "";
                }

                // Built-in PDF fonts are limited to WinAnsi.
                // Replace unsupported characters rather than failing PDF generation.
                return value
                                .replace("\u2013", "-")
                                .replace("\u2014", "-")
                                .replace("\u2018", "'")
                                .replace("\u2019", "'")
                                .replace("\u201C", "\"")
                                .replace("\u201D", "\"")
                                .replace("\u2022", "-");
        }
}
